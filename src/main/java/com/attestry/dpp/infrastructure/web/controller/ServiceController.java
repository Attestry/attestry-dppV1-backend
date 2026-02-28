package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.dto.request.ServiceSubmitRequest;
import com.attestry.dpp.application.dto.result.ServiceCaseResult;
import com.attestry.dpp.application.dto.result.ServiceSubmitResult;
import com.attestry.dpp.application.usecase.command.ServiceCaseCommandUseCase;
import com.attestry.dpp.application.usecase.query.ServiceCaseQueryUseCase;
import com.attestry.dpp.infrastructure.security.JwtUserDetails;
import com.attestry.dpp.infrastructure.web.mapper.ServiceResponseMapper;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.ResponseMessage;
import com.attestry.dpp.infrastructure.web.response.ServiceCaseResponse;
import com.attestry.dpp.infrastructure.web.response.ServiceSubmitResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/services", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceCaseCommandUseCase serviceCaseCommandUseCase;
    private final ServiceCaseQueryUseCase serviceCaseQueryUseCase;

    @GetMapping("/{caseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ServiceCaseResponse>> getServiceCase(@PathVariable String caseId) {
        ServiceCaseResult result = serviceCaseQueryUseCase.getServiceCase(caseId);
        return ResponseEntity.ok(ApiResponse.success(ServiceResponseMapper.toServiceCaseResponse(result)));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<ServiceSubmitResponse>> submitService(
            @Valid @RequestBody ServiceSubmitRequest request,
            @AuthenticationPrincipal JwtUserDetails user) {
        ServiceSubmitResult result = serviceCaseCommandUseCase.submitService(request, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(ServiceResponseMapper.toServiceSubmitResponse(result)));
    }

    @PostMapping("/{caseId}/complete")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> completeService(
            @PathVariable String caseId,
            @AuthenticationPrincipal JwtUserDetails user) {
        serviceCaseCommandUseCase.completeService(caseId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_SERVICE_COMPLETED));
    }

    @PostMapping("/{caseId}/approve")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> approveService(
            @PathVariable String caseId,
            @AuthenticationPrincipal JwtUserDetails user) {
        serviceCaseCommandUseCase.approveService(caseId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_SERVICE_APPROVED));
    }
}
