package com.tein.karardefteri.user.service;

import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }
    public User getUserById(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Kullanıcı bulunamadu:" +id));
    }
}