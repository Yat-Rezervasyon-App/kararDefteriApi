
package com.tein.karardefteri.user.controller;

import com.tein.karardefteri.common.dto.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

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
    public PageResponse<UserResponseDTO> getAllUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page ,
            @RequestParam(defaultValue = "10") int size
    ){
        if (page < 0){
            throw new InvalidRequestException(
                    "INVALID_PAGE",
                    "Sayfa numaranız 0 veya daha büyük olmalıdır"
            );
        }

        if (size < 1 || size >100) {
            throw new InvalidRequestException(
                    "INVALID_SIZE",
                    "Sayfa boyutu 1 ile 100 arasında olmalıdır"
            );
        }
        return userService.getAllUsers(q, active, page, size);
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
