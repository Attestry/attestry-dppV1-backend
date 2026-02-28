package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.dto.result.PassportMyPassportResult;
import com.attestry.dpp.application.dto.result.PassportPublicViewResult;
import com.attestry.dpp.application.usecase.query.PassportQueryUseCase;
import com.attestry.dpp.infrastructure.security.JwtUserDetails;
import com.attestry.dpp.infrastructure.web.mapper.PassportResponseMapper;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.PassportMyPassportResponse;
import com.attestry.dpp.infrastructure.web.response.PassportPublicViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/passports", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class PassportController {

    private final PassportQueryUseCase passportQueryUseCase;

    @GetMapping("/{qrPublicCode}")
    public ResponseEntity<ApiResponse<PassportPublicViewResponse>> getPublicPassport(@PathVariable String qrPublicCode) {
        PassportPublicViewResult result = passportQueryUseCase.getPublicPassport(qrPublicCode);
        return ResponseEntity.ok(ApiResponse.success(PassportResponseMapper.toPassportPublicViewResponse(result)));
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Page<PassportMyPassportResponse>>> getMyPassports(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // userId는 JWT에서 추출 — 쿼리 파라미터 위변조 방지
        Pageable pageable = PageRequest.of(page, size);
        Page<PassportMyPassportResponse> response = passportQueryUseCase.getMyPassports(user.getUserId(), pageable)
                .map(PassportResponseMapper::toPassportMyPassportResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
