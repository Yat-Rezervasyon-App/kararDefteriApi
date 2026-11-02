package com.tein.karardefteri.user.dto;

import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.entity.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO (
        UUID id,
        String fullName,
        String email,
        UserRole role,
        boolean active,
        Instant createdAt

){
    public static UserResponseDTO from(User user){
        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
