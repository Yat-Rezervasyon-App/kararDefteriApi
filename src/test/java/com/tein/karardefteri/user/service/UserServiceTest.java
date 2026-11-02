
package com.tein.karardefteri.user.service;

import com.tein.karardefteri.common.exception.BusinessRuleViolationException;
import com.tein.karardefteri.user.dto.UserRequestDTO;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUser_hashesPasswordBeforeSaving() {
        UserRequestDTO request = new UserRequestDTO(
                "Test Kullanıcı",
                "test@example.com",
                "GucluParola123",
                UserRole.MEMBER
        );

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        var response = userService.createUser(request);

        verify(userRepository).save(argThat(user ->
                !user.getPasswordHash().equals("GucluParola123")
                        && passwordEncoder.matches(
                        "GucluParola123",
                        user.getPasswordHash()
                )
        ));

        assertEquals("test@example.com", response.email());
    }

    @Test
    void createUser_whenEmailAlreadyExists_throwsConflictException() {
        UserRequestDTO request = new UserRequestDTO(
                "Test Kullanıcı",
                "test@example.com",
                "GucluParola123",
                UserRole.MEMBER
        );

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> userService.createUser(request)
        );

        assertEquals("EMAIL_ALREADY_EXISTS", exception.getCode());
        verify(userRepository, never()).save(any(User.class));
    }
}
