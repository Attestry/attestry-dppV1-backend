package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.usecase.command.AdminUserCommandUseCase;
import com.attestry.dpp.application.usecase.query.AdminUserQueryUseCase;
import com.attestry.dpp.application.dto.result.AdminPendingUserResult;
import com.attestry.dpp.infrastructure.web.mapper.AdminUserResponseMapper;
import com.attestry.dpp.infrastructure.web.response.AdminPendingUserResponse;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 전용 사용자 관리 API 컨트롤러.
 */
@RestController
@RequestMapping(value = "/api/v1/admin/users", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserQueryUseCase adminUserQueryUseCase;
    private final AdminUserCommandUseCase adminUserCommandUseCase;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminPendingUserResponse>>> getPendingUsers() {
        List<AdminPendingUserResult> result = adminUserQueryUseCase.listPendingUsers();
        List<AdminPendingUserResponse> response = result.stream()
                .map(AdminUserResponseMapper::toAdminPendingUserResponse)
                .toList();
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
