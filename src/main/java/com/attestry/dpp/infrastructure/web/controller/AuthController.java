package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.dto.request.AuthLoginRequest;
import com.attestry.dpp.application.dto.request.AuthSignupRequest;
import com.attestry.dpp.application.dto.result.AuthLoginResult;
import com.attestry.dpp.application.dto.result.AuthSignupResult;
import com.attestry.dpp.application.usecase.command.AuthCommandUseCase;
import com.attestry.dpp.infrastructure.web.mapper.AuthResponseMapper;
import com.attestry.dpp.infrastructure.web.response.AuthLoginResponse;
import com.attestry.dpp.infrastructure.web.response.AuthSignupResponse;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCommandUseCase authCommandUseCase;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthSignupResponse>> signup(@Valid @RequestBody AuthSignupRequest request) {
        AuthSignupResult result = authCommandUseCase.signup(request);
        return ResponseEntity.ok(ApiResponse.success(AuthResponseMapper.toAuthSignupResponse(result)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthLoginResponse>> login(@Valid @RequestBody AuthLoginRequest request) {
        AuthLoginResult result = authCommandUseCase.login(request);
        return ResponseEntity.ok(ApiResponse.success(AuthResponseMapper.toAuthLoginResponse(result)));
    }
}
