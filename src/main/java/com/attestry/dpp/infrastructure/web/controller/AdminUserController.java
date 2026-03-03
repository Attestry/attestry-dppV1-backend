package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.usecase.command.AdminUserCommandUseCase;
import com.attestry.dpp.application.usecase.query.AdminUserQueryUseCase;
import com.attestry.dpp.application.dto.result.AdminPendingUserResult;
import com.attestry.dpp.infrastructure.web.mapper.AdminUserResponseMapper;
import com.attestry.dpp.infrastructure.web.response.AdminPendingUserResponse;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.ResponseMessage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 전용 사용자 관리 API 컨트롤러.
 */
@RestController
@RequestMapping(value = "/api/v1/admin/users", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
@Validated
public class AdminUserController {
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminUserQueryUseCase adminUserQueryUseCase;
    private final AdminUserCommandUseCase adminUserCommandUseCase;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AdminPendingUserResponse>>> getPendingUsers(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = MAX_PAGE_SIZE, message = "size는 100 이하여야 합니다.") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<AdminPendingUserResult> result = adminUserQueryUseCase.listPendingUsers(pageable);
        Page<AdminPendingUserResponse> response = result.map(AdminUserResponseMapper::toAdminPendingUserResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/approve/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approveUser(@PathVariable String userId) {
        adminUserCommandUseCase.approveUser(userId);
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_ADMIN_USER_APPROVED));
    }

    @PostMapping("/reject/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectUser(@PathVariable String userId) {
        adminUserCommandUseCase.rejectUser(userId);
        return ResponseEntity.ok(ApiResponse.successMessage(ResponseMessage.SUCCESS_ADMIN_USER_REJECTED));
    }
}
