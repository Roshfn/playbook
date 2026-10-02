package com.playbook.auth.api.controller;

import com.playbook.auth.api.request.LoginRequest;
import com.playbook.auth.api.response.AuthResponse;
import com.playbook.auth.application.usecase.LoginUseCase;
import com.playbook.auth.domain.model.AuthenticationCommand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        var command = new AuthenticationCommand(
                req.provider(), req.idToken(), req.phoneNumber(), req.otp(),
                req.deviceId(), http.getRemoteAddr(), http.getHeader("User-Agent"));
        return AuthResponse.from(loginUseCase.execute(command));
    }
}