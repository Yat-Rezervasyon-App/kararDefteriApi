
package com.tein.karardefteri.user.controller;

import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String EMAIL = "auth-test@example.com";

    private static final String PASSWORD =  "TestPassword123";

    private User createUser(boolean active){
        User user = new User();

        user.setFullName("Auth Test");
        user.setEmail(EMAIL);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(UserRole.MEMBER);
        user.setActive(active);

        return userRepository.save(user);
    }


    @Test
    void csrfEndpointShouldReturnToken() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.headerName").exists())
                .andExpect(jsonPath("$.parameterName").exists());
    }

    @Test
    void loginShouldSucceedWithValidCredentials() throws Exception {
        createUser(true);

        mockMvc.perform(post("/login")
                .param("email" , EMAIL)
                .param("password" , PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void loginShouldFailWithInvalidPassword() throws Exception {
        createUser(true);

        mockMvc.perform(post("/login")
                        .param("email", EMAIL)
                        .param("password", "WrongPassword123")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveUserShouldNotLogin() throws Exception{
        createUser(false);

        mockMvc.perform(post("/login")
                .param("email" , EMAIL)
                .param("password" , PASSWORD)
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @AfterEach
    void cleanUp() {
        userRepository.findByEmail(EMAIL)
                .ifPresent(userRepository::delete);
    }
}
