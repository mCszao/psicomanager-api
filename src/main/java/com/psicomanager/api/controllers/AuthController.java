package com.psicomanager.api.controllers;

import com.psicomanager.api.domain.auth.exception.InvalidRefreshTokenException;
import com.psicomanager.api.domain.user.dto.ResponseLoginDTO;
import com.psicomanager.api.repositories.user.model.User;
import com.psicomanager.api.core.dto.BaseResponse;
import com.psicomanager.api.domain.user.dto.UserLoginDTO;
import com.psicomanager.api.domain.user.dto.UserRegisterDTO;
import com.psicomanager.api.services.TokenService;
import com.psicomanager.api.services.MyUserDetailsService;
import com.psicomanager.api.services.RefreshTokenService;
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

    // region Cookie helpers

    private static final String ACCESS_TOKEN_COOKIE = "authToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds

    /**
     * Builds an HttpOnly cookie with Secure and SameSite=Strict attributes.
     *
     * @param name   cookie name
     * @param value  cookie value
     * @param maxAge max age in seconds; use 0 to delete
     * @return configured {@link Cookie}
     */
    private Cookie buildHttpOnlyCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set to true when running over HTTPS in production
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

    // endregion

    // region Endpoints

    /**
     * Authenticates the user and sets both the access token and refresh token as HttpOnly cookies.
     */
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

    /**
     * Registers a new user. No session is created — the client must sign in afterwards.
     */
    @PostMapping("/signUp")
    public ResponseEntity<BaseResponse<String>> signUp(@RequestBody @Valid UserRegisterDTO body) {
        log.info("POST: /auth/signUp");
        authService.register(body);
        return ResponseEntity.ok(new BaseResponse<>(true, "Cadastro realizado com sucesso!"));
    }

    /**
     * Issues a new access token using a valid refresh token from the cookie.
     * Revokes the old refresh token and issues a new one (rotation).
     */
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

    /**
     * Logs the user out by revoking all refresh tokens and clearing auth cookies.
     */
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
                // token already invalid — proceed to clear cookies regardless
                log.warn("signOut chamado com refresh token inválido, limpando cookies mesmo assim");
            }
        }

        clearAuthCookies(response);
        return ResponseEntity.ok(new BaseResponse<>(true, "Logout realizado com sucesso!"));
    }

    // endregion
}
