
package com.tein.karardefteri.user.service;

import com.tein.karardefteri.common.dto.PageResponse;
import com.tein.karardefteri.common.exception.ResourceNotFoundException;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.common.exception.BusinessRuleViolationException;
import com.tein.karardefteri.user.dto.UserRequestDTO;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

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

    @Test
    void updateUserActiveStatus_shouldDeactivateUser(){
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User user = new User();
        user.setFullName("Test Kullanıcı");
        user.setEmail("test@example.com");
        user.setActive(true);

        when(userRepository.findById(targetUserId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUserActiveStatus(targetUserId, adminId);

        assertEquals(false, user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserActiveStatus_shouldThrow404WhenUserNotFound(){
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(userRepository.findById(targetUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                ()-> userService.updateUserActiveStatus(targetUserId, adminId)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_returnsPaginatedResult() {
        User user = new User();
        user.setFullName("Test Kullanici");
        user.setEmail("test@example.com");
        user.setActive(true);

        Page<User> userPage = new PageImpl<>(
                List.of(user),
                PageRequest.of(0,10),
                1
        );

        when(userRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(userPage);

        PageResponse<UserResponseDTO> result =
                userService.getAllUsers("test",true,0,10);

        assertEquals(1,result.content().size());
        assertEquals("test@example.com",result.content().get(0).email());
        assertEquals(0, result.page());
        assertEquals(10,result.size());
        assertEquals(1,result.totalElements());
        assertEquals(1,result.totalPages());

    }

    @Test
    void getAllUsers_returnsEmptyPageWhenNoUsersMatch(){
        Page<User> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0,10),
                0
        );
        when(userRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(emptyPage);

        PageResponse<UserResponseDTO> result =
                userService.getAllUsers("unkown",false,0,10);

        assertTrue(result.content().isEmpty());
        assertEquals(0,result.totalElements());
        assertEquals(0,result.totalPages());
    }
}
