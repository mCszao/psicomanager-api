package com.psicomanager.api.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psicomanager.api.auth.TokenService;
import com.psicomanager.api.auth.TokenService.ValidationResult;
import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.user.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class FilterSecurity extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private String getToken(HttpServletRequest req) {
        if (req.getCookies() != null) {
            return Arrays.stream(req.getCookies())
                    .filter(c -> "authToken".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        var authorization = req.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.split(" ")[1];
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new BaseResponse<>(false, message));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getToken(request);
        ValidationResult result = tokenService.validateJWT(token);
        if (result.isExpired()) {
            writeUnauthorized(response, "Token expirado. Faça login novamente.");
            return;
        }
        if (result.subject() != null) {
            var user = myUserDetailsService.loadUserByUsername(result.subject());
            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
