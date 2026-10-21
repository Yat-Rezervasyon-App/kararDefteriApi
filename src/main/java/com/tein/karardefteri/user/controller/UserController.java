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

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers(){
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO createUser(
            @Valid @RequestBody UserRequestDTO request
    ){
        return userService.createUser(request);
    }
}

