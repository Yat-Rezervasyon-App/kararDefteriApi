
package com.tein.karardefteri.user.controller;

import com.tein.karardefteri.common.dto.PageResponse;
import com.tein.karardefteri.common.dto.PageResponse;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.tein.karardefteri.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getAllUsers_whenNoUsersExist_returnsEmptyPage() throws Exception {
        when(userService.getAllUsers(null, null, 0, 10))
                .thenReturn(new PageResponse<>(
                        List.of(),
                        0,
                        10,
                        0,
                        0
                ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
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

        when(userService.getAllUsers(null, null, 0, 10))
                .thenReturn(new PageResponse<>(
                        List.of(user),
                        0,
                        10,
                        1,
                        1
                ));

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email")
                        .value("test@example.com"))
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllUsers_filtersBySearchAndActiveStatus() throws Exception{
        when(userService.getAllUsers("test",true,0,10))
                .thenReturn(new PageResponse<>(
                        List.of(),
                        0,
                        10,
                        0,
                        0
                ));

        mockMvc.perform(get("/api/users")
                .param("q","test")
                .param("active","true")
                .param("page","0")
                .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(userService).getAllUsers("test",true,0,10);
    }
}
