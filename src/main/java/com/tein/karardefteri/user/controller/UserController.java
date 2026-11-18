
package com.tein.karardefteri.user.controller;

import com.tein.karardefteri.common.exception.InvalidRequestException;
import com.tein.karardefteri.user.dto.UserRequestDTO;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService , UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Page<UserResponseDTO> getAllUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1
                || pageable.getPageSize() > 100) {
            throw new InvalidRequestException(
                    "INVALID_PAGINATION",
                    "Sayfa numarası 0 veya daha büyük, sayfa boyutu 1 ile 100 arasında olmalıdır"
            );
        }

        return userService.getAllUsers(q, active, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(
            @Valid @RequestBody UserRequestDTO request
    ) {
        return userService.createUser(request);
    }

    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new
                        org.springframework.web.server.ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Kullanıcı bulunamadı"
                        )
                );
        return UserResponseDTO.from(user);
    }

    @PatchMapping("/{id}/active")
    public UserResponseDTO updateUserActiveStatus(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                    new
                        org.springframework.web.server.ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                            "Giriş yapan kullanıcı bulunamadı."
                    )
                );
        return userService.updateUserActiveStatus(
                id,
                currentUser.getId()
        );
    }
}
