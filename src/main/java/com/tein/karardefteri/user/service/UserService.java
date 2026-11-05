
package com.tein.karardefteri.user.service;

import com.tein.karardefteri.common.exception.BusinessRuleViolationException;
import com.tein.karardefteri.user.dto.UserRequestDTO;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.tein.karardefteri.common.exception.ResourceNotFoundException;

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

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::from)
                .toList();
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
