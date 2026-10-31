package com.playbook.auth.api.controller;

import com.playbook.auth.api.request.*;
import com.playbook.auth.api.response.AuthResponse;
import com.playbook.auth.application.usecase.LoginUseCase;
import com.playbook.auth.application.usecase.LogoutUseCase;
import com.playbook.auth.application.usecase.RefreshTokenUseCase;
import com.playbook.auth.application.usecase.RequestOtpUseCase;
import com.playbook.auth.domain.model.AuthenticationCommand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RequestOtpUseCase requestOtpUseCase;

    public AuthController(LoginUseCase loginUseCase, RefreshTokenUseCase refreshUseCase,
                          LogoutUseCase logoutUseCase, RequestOtpUseCase requestOtpUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.requestOtpUseCase = requestOtpUseCase;
    }

    @PostMapping("/otp/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestOtp(@Valid @RequestBody OtpRequest req) {
        requestOtpUseCase.execute(req.phoneNumber());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        var command = new AuthenticationCommand(
                req.provider(), req.idToken(), req.phoneNumber(), req.otp(),
                req.deviceId(), http.getRemoteAddr(), http.getHeader("User-Agent"));
        return AuthResponse.from(loginUseCase.execute(command));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest http) {
        return AuthResponse.from(refreshUseCase.execute(
                req.refreshToken(), req.deviceId(), http.getRemoteAddr(), http.getHeader("User-Agent")));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest req) {
        logoutUseCase.execute(req.refreshToken());
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return Map.of("userId", jwt.getSubject(), "roles", roles == null ? List.of() : roles);
    }
}