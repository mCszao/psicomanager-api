package com.psicomanager.api.auth;

import com.psicomanager.api.auth.exception.InvalidRefreshTokenException;
import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.user.MyUserDetailsService;
import com.psicomanager.api.user.dto.ResponseLoginDTO;
import com.psicomanager.api.user.dto.UserLoginDTO;
import com.psicomanager.api.user.dto.UserRegisterDTO;
import com.psicomanager.api.user.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private MyUserDetailsService authService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private static final String ACCESS_TOKEN_COOKIE = "authToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60;

    private Cookie buildHttpOnlyCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String rawRefreshToken) {
        response.addCookie(buildHttpOnlyCookie(ACCESS_TOKEN_COOKIE, accessToken, 15 * 60));
        response.addCookie(buildHttpOnlyCookie(REFRESH_TOKEN_COOKIE, rawRefreshToken, REFRESH_TOKEN_MAX_AGE));
    }

    private void clearAuthCookies(HttpServletResponse response) {
        response.addCookie(buildHttpOnlyCookie(ACCESS_TOKEN_COOKIE, "", 0));
        response.addCookie(buildHttpOnlyCookie(REFRESH_TOKEN_COOKIE, "", 0));
    }

    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @PostMapping("/signIn")
    public ResponseEntity<BaseResponse<ResponseLoginDTO>> signIn(
            @RequestBody @Valid UserLoginDTO body,
            HttpServletResponse response
    ) {
        log.info("POST: /auth/signIn — usuário: {}", body.username());
        var authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.username(), body.password())
        );
        User user = (User) authentication.getPrincipal();
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        setAuthCookies(response, accessToken, refreshToken);
        return ResponseEntity.ok(new BaseResponse<>(true, authService.login(user)));
    }

    @PostMapping("/signUp")
    public ResponseEntity<BaseResponse<String>> signUp(@RequestBody @Valid UserRegisterDTO body) {
        log.info("POST: /auth/signUp");
        authService.register(body);
        return ResponseEntity.ok(new BaseResponse<>(true, "Cadastro realizado com sucesso!"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<String>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("POST: /auth/refresh");
        String rawRefreshToken = extractCookieValue(request, REFRESH_TOKEN_COOKIE);
        if (rawRefreshToken == null) {
            clearAuthCookies(response);
            throw new InvalidRefreshTokenException();
        }
        User user = refreshTokenService.validateRefreshToken(rawRefreshToken);
        String newAccessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.createRefreshToken(user);
        setAuthCookies(response, newAccessToken, newRefreshToken);
        return ResponseEntity.ok(new BaseResponse<>(true, "Token renovado com sucesso!"));
    }

    @PostMapping("/signOut")
    public ResponseEntity<BaseResponse<String>> signOut(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("POST: /auth/signOut");
        String rawRefreshToken = extractCookieValue(request, REFRESH_TOKEN_COOKIE);
        if (rawRefreshToken != null) {
            try {
                User user = refreshTokenService.validateRefreshToken(rawRefreshToken);
                refreshTokenService.revokeAllByUser(user);
            } catch (InvalidRefreshTokenException e) {
                log.warn("signOut chamado com refresh token inválido, limpando cookies mesmo assim");
            }
        }
        clearAuthCookies(response);
        return ResponseEntity.ok(new BaseResponse<>(true, "Logout realizado com sucesso!"));
    }
}
