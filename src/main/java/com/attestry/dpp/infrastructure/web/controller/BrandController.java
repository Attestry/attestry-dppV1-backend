package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.dto.request.BrandMintRequest;
import com.attestry.dpp.application.dto.request.BrandReleaseRequest;
import com.attestry.dpp.application.dto.result.BrandMintResult;
import com.attestry.dpp.application.usecase.command.BrandCommandUseCase;
import com.attestry.dpp.infrastructure.security.JwtUserDetails;
import com.attestry.dpp.infrastructure.web.mapper.BrandResponseMapper;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.BrandMintResponse;
import com.attestry.dpp.infrastructure.web.response.ResponseMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 브랜드 전용 API 컨트롤러.
 * mint(민팅)과 release(출고) 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping(value = "/api/v1/brands", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class BrandController {
    private final BrandCommandUseCase brandCommandUseCase;

    @PostMapping("/mint")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<ApiResponse<BrandMintResponse>> mint(
            @Valid @RequestBody BrandMintRequest request,
            @AuthenticationPrincipal JwtUserDetails user) {
        BrandMintResult result = brandCommandUseCase.mintAsset(request, user.getUserId());
        BrandMintResponse response = BrandResponseMapper.toBrandMintResponse(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/release")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<ApiResponse<Void>> release(
            @Valid @RequestBody BrandReleaseRequest request,
            @AuthenticationPrincipal JwtUserDetails user) {
        brandCommandUseCase.releaseAsset(request, user.getUserId());
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_BRAND_RELEASED));
    }
}
