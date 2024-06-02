package com.psicomanager.api.infra.security;

import com.psicomanager.api.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FilterSecurity extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private TokenService tokenService;

    private String getToken(HttpServletRequest req){
        var authorization = req.getHeader("Authorization");
        if(authorization != null) return authorization.split(" ")[1];
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);
        String subject = tokenService.validateJWT(token);
        if(subject != null) {
            var user = authService.loadUserByUsername(subject);
            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
