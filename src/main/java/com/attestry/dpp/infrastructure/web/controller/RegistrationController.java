package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.dto.request.RegistrationSubmitRequest;
import com.attestry.dpp.application.dto.result.RegistrationRequestResult;
import com.attestry.dpp.application.usecase.command.RegistrationCommandUseCase;
import com.attestry.dpp.application.usecase.query.RegistrationQueryUseCase;
import com.attestry.dpp.application.usecase.query.RegistrationUploadQueryUseCase;
import com.attestry.dpp.infrastructure.security.JwtUserDetails;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.RegistrationRequestResponse;
import com.attestry.dpp.infrastructure.web.response.ResponseMessage;
import com.attestry.dpp.infrastructure.web.mapper.RegistrationResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/registrations", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationCommandUseCase registrationCommandUseCase;
    private final RegistrationQueryUseCase registrationQueryUseCase;
    private final RegistrationUploadQueryUseCase registrationUploadQueryUseCase;

    @GetMapping("/upload-url")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<String>> getUploadUrl(@RequestParam String filename) {
        return ResponseEntity.ok(ApiResponse.success(registrationUploadQueryUseCase.getUploadUrl(filename)));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<RegistrationRequestResponse>> submit(
            @Valid @RequestBody RegistrationSubmitRequest request,
            @AuthenticationPrincipal JwtUserDetails user) {
        RegistrationRequestResult result = registrationCommandUseCase.submitRequest(request, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(RegistrationResponseMapper.toRegistrationRequestResponse(result)));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<RegistrationRequestResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RegistrationRequestResponse> response = registrationQueryUseCase.listAllRequests(pageable)
                .map(RegistrationResponseMapper::toRegistrationRequestResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<RegistrationRequestResponse>>> listMyRequests(
            @AuthenticationPrincipal JwtUserDetails user) {
        List<RegistrationRequestResponse> response = registrationQueryUseCase.listRequestsByRequester(user.getUserId()).stream()
                .map(RegistrationResponseMapper::toRegistrationRequestResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/approve/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable String requestId,
            @AuthenticationPrincipal JwtUserDetails user) {
        registrationCommandUseCase.approveRequest(requestId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_REGISTRATION_APPROVED));
    }

    @PostMapping("/reject/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable String requestId) {
        registrationCommandUseCase.rejectRequest(requestId);
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_REGISTRATION_REJECTED));
    }
}
