package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.dto.request.TransferAcceptRequest;
import com.attestry.dpp.application.dto.request.TransferInitiateRequest;
import com.attestry.dpp.application.dto.result.TransferDetailsResult;
import com.attestry.dpp.application.dto.result.TransferInitiateResult;
import com.attestry.dpp.application.usecase.command.TransferCommandUseCase;
import com.attestry.dpp.application.usecase.query.TransferQueryUseCase;
import com.attestry.dpp.infrastructure.security.JwtUserDetails;
import com.attestry.dpp.infrastructure.web.mapper.TransferResponseMapper;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.ResponseMessage;
import com.attestry.dpp.infrastructure.web.response.TransferDetailsResponse;
import com.attestry.dpp.infrastructure.web.response.TransferInitiateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/transfers", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class TransferController {

    private final TransferCommandUseCase transferCommandUseCase;
    private final TransferQueryUseCase transferQueryUseCase;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<TransferInitiateResponse>> initiateTransfer(
            @Valid @RequestBody TransferInitiateRequest request,
            @AuthenticationPrincipal JwtUserDetails user) {
        TransferInitiateResult result = transferCommandUseCase.initiateTransfer(request, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(TransferResponseMapper.toTransferInitiateResponse(result)));
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Void>> acceptTransfer(
            @Valid @RequestBody TransferAcceptRequest request,
            @AuthenticationPrincipal JwtUserDetails user) {
        transferCommandUseCase.acceptTransfer(request.getTokenOrCode(), user.getUserId());
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_TRANSFER_ACCEPTED));
    }

    @GetMapping("/token/{tokenId}")
    public ResponseEntity<ApiResponse<TransferDetailsResponse>> getTokenDetails(@PathVariable String tokenId) {
        TransferDetailsResult result = transferQueryUseCase.getTokenDetails(tokenId);
        return ResponseEntity.ok(ApiResponse.success(TransferResponseMapper.toTransferDetailsResponse(result)));
    }

    @PostMapping("/cancel/{tokenId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> cancelTransfer(
            @PathVariable String tokenId,
            @AuthenticationPrincipal JwtUserDetails user) {
        transferCommandUseCase.cancelTransfer(tokenId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_TRANSFER_CANCELED));
    }
}
