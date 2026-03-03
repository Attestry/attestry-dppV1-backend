package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.dto.result.PassportPublicViewResult;
import com.attestry.dpp.application.usecase.query.PassportQueryUseCase;
import com.attestry.dpp.infrastructure.security.JwtUserDetails;
import com.attestry.dpp.infrastructure.web.mapper.PassportResponseMapper;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.PassportMyPassportResponse;
import com.attestry.dpp.infrastructure.web.response.PassportPublicViewResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/passports", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
@Validated
public class PassportController {
    private static final int MAX_PAGE_SIZE = 100;

    private final PassportQueryUseCase passportQueryUseCase;

    @GetMapping("/{qrPublicCode}")
    public ResponseEntity<ApiResponse<PassportPublicViewResponse>> getPublicPassport(
            @PathVariable @NotBlank(message = "qrPublicCode는 비어 있을 수 없습니다.") String qrPublicCode) {
        PassportPublicViewResult result = passportQueryUseCase.getPublicPassport(qrPublicCode);
        return ResponseEntity.ok(ApiResponse.success(PassportResponseMapper.toPassportPublicViewResponse(result)));
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Page<PassportMyPassportResponse>>> getMyPassports(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = MAX_PAGE_SIZE, message = "size는 100 이하여야 합니다.") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PassportMyPassportResponse> response = passportQueryUseCase.getMyPassports(user.getUserId(), pageable)
                .map(PassportResponseMapper::toPassportMyPassportResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
