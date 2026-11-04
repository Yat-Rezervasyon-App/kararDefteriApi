
package com.tein.karardefteri.common.configuration;

import com.tein.karardefteri.user.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import com.tein.karardefteri.user.repository.UserRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
public class SecurityConfig {


    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            UserRepository userRepository
    ) throws Exception {

        http
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(
                        new ActiveUserSessionFilter(userRepository),
                        UsernamePasswordAuthenticationFilter.class
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/csrf").permitAll()
                        .requestMatchers("/login", "/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/active")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .usernameParameter("email")
                        .successHandler((request, response, authentication) ->
                                writeJson(
                                        response,
                                        HttpServletResponse.SC_OK,
                                        "{\"message\":\"Giriş başarılı\"}"
                                )
                        )
                        .failureHandler((request, response, exception) ->
                                writeJson(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "{\"error\":\"E-posta veya şifre hatalı\"}"
                                )
                        )
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                writeJson(
                                        response,
                                        HttpServletResponse.SC_OK,
                                        "{\"message\":\"Çıkış başarılı\"}"
                                )
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeJson(
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "{\"error\":\"Giriş yapmanız gerekiyor\"}"
                                )
                        )
                        .accessDeniedHandler((request, response, exception) ->
                                writeJson(
                                        response,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "{\"error\":\"Bu işlem için yetkiniz yok\"}"
                                )
                        )
                );

        return http.build();
    }

    private void writeJson(
            HttpServletResponse response,
            int status,
            String json
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }
}
