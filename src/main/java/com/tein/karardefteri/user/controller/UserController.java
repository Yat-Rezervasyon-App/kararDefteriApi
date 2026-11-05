
package com.tein.karardefteri.user.controller;

import com.tein.karardefteri.user.dto.UserRequestDTO;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
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
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
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
