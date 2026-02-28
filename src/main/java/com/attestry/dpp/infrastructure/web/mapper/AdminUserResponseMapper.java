package com.attestry.dpp.infrastructure.web.mapper;

import com.attestry.dpp.application.dto.result.AdminPendingUserResult;
import com.attestry.dpp.infrastructure.web.response.AdminPendingUserResponse;

public final class AdminUserResponseMapper {

    private AdminUserResponseMapper() {
    }

    public static AdminPendingUserResponse toAdminPendingUserResponse(AdminPendingUserResult result) {
        return AdminPendingUserResponse.of(
                result.getUserId(),
                result.getEmail(),
                result.getRole(),
                result.getPhone(),
                result.getBusinessNumber(),
                result.getBrandName(),
                result.getStatus());
    }
}
