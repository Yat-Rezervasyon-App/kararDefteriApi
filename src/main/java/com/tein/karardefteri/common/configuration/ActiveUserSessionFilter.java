package com.tein.karardefteri.common.configuration;

import com.tein.karardefteri.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ActiveUserSessionFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public ActiveUserSessionFilter(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException{

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if ( authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof
                AnonymousAuthenticationToken)){

            String email = authentication.getName();

            boolean active = userRepository.existsByEmailAndActiveTrue(email);

            if(!active){
                SecurityContextHolder.clearContext();

                var session = request.getSession(false);

                if (session !=null){
                    session.invalidate();
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"error\":\"Hesabınız pasifleştirildi. Lütfen tekrar giriş yapın.\"}"
                );
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
