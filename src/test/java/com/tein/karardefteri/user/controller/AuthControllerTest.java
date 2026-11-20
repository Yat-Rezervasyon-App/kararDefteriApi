
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    private User createUser(boolean active, UserRole role){
        User user = new User();

        user.setFullName("Auth Test");
        user.setEmail(EMAIL);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        user.setActive(active);

        return userRepository.save(user);
    }

    private MockHttpSession loginAs(String email, String password)
        throws Exception{
        MvcResult result = mockMvc.perform(post("/login")
                .param("email",email)
                .param("password",password)
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);

        assertNotNull(session);

        return session;
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
        createUser(true, UserRole.MEMBER);

       MvcResult result = mockMvc.perform(post("/login")
               .param("email" , EMAIL)
               .param("password" , PASSWORD)
               .with(csrf()))
               .andExpect(status().isOk())
               .andReturn();

       MockHttpSession session =
               (MockHttpSession) result.getRequest().getSession(false);

       org.junit.jupiter.api.Assertions.assertNotNull(session);
    }

    @Test
    void loginShouldFailWithInvalidPassword() throws Exception {
        createUser(true, UserRole.MEMBER);
        mockMvc.perform(post("/login")
                        .param("email", EMAIL)
                        .param("password", "WrongPassword123")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveUserShouldNotLogin() throws Exception{
        createUser(false, UserRole.MEMBER);

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
        userRepository.findByEmail("created-by-admin@example.com")
                .ifPresent(userRepository::delete);
    }

    @Test
    void authenticatedUserShouldAccessCurrentUserEndpoint() throws Exception {
        createUser(true, UserRole.MEMBER);

        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("email",EMAIL)
                .param("password",PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        assertNotNull(session);

        mockMvc.perform(get("/api/users/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void logoutShouldInvalidateSession() throws Exception {

        createUser(true, UserRole.MEMBER);

        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("email", EMAIL)
                .param("password" , PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        assertNotNull(session);

        mockMvc.perform(post("/logout")
                .session(session)
                .with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/users/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deactivatedUserShouldLoseExistingSession() throws Exception {

        User user =createUser(true, UserRole.MEMBER);

        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("email" , EMAIL)
                .param("password", PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        assertNotNull(session);

        user.setActive(false);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/users/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void memberShouldNotCreateUser() throws Exception {

        createUser(true, UserRole.MEMBER);

        MockHttpSession session = loginAs(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/users")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "fullName": "New User",
                                "email": "created-by-admin@example.com",
                                "password": "TestPassword123",
                                "role": "MEMBER"
                            }
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldCreateUser() throws Exception {

        createUser(true, UserRole.ADMIN);

        MockHttpSession session = loginAs(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/users")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "fullName": "New User",
                                "email": "created-by-admin@example.com",
                                "password": "TestPassword123",
                                "role": "MEMBER"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("created-by-admin@example.com"));
    }

    @Test
    void shouldNotCreateUserWithDuplicateEmail() throws Exception{
        createUser(true, UserRole.ADMIN);

        MockHttpSession session = loginAs(EMAIL, PASSWORD);

        mockMvc.perform(post("/api/users")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "fullName": "Duplicate User",
                    "email": "auth-test@example.com",
                    "password": "AnotherPassword123",
                    "role": "MEMBER"
                }
                """))
                .andExpect(status().isConflict());
    }
}
