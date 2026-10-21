
package com.tein.karardefteri.user.controller;

import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getAllUsers_whenNoUsersExist_returnsEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllUsers_doesNotExposePasswordHash() throws Exception {
        UserResponseDTO user = new UserResponseDTO(
                UUID.randomUUID(),
                "Test Kullanıcı",
                "test@example.com",
                UserRole.MEMBER,
                true,
                Instant.parse("2026-01-01T10:00:00Z")
        );

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email")
                        .value("test@example.com"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }
}
