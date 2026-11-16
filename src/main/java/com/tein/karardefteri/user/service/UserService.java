
package com.tein.karardefteri.user.service;

import com.tein.karardefteri.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.tein.karardefteri.common.exception.BusinessRuleViolationException;
import com.tein.karardefteri.user.dto.UserRequestDTO;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.tein.karardefteri.common.exception.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResponse<UserResponseDTO> getAllUsers(
            String q,
            Boolean active,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (q != null && !q.isBlank()) {
                String search = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("fullName")),
                                        search
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("email")),
                                        search
                                )
                        )
                );
            }
            if (active != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("active"), active)
                );
            }
            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };

        Page<UserResponseDTO> result = userRepository
                .findAll(specification, pageable)
                .map(UserResponseDTO::from);

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public UserResponseDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "USER_NOT_FOUND",
                        "Kullanıcı bulunamadı."
                ));

        return UserResponseDTO.from(user);
    }
    public boolean userExists(UUID id) {
        return userRepository.existsById(id);
    }

    public boolean isUserActive(UUID id) {
        return userRepository.existsByIdAndActiveTrue(id);
    }

    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleViolationException(
                    "EMAIL_ALREADY_EXISTS",
                    "Bu e-posta adresi zaten kullanılıyor."
            );
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(request.role());

        User savedUser = userRepository.save(user);

        return UserResponseDTO.from(savedUser);
    }

    public UserResponseDTO updateUserActiveStatus(
            UUID targetUserId,
            UUID currentUserId
    ) {
        if (targetUserId.equals(currentUserId)) {
            throw new BusinessRuleViolationException(
                    "SELF_DEACTIVATION_NOT_ALLOWED",
                    "Admin kendi hesabını pasifleştiremez."
            );
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "USER_NOT_FOUND",
                        "Kullanıcı bulunamadı."
                ));

        user.setActive(!user.isActive());

        User savedUser = userRepository.save(user);

        return UserResponseDTO.from(savedUser);
    }
}
