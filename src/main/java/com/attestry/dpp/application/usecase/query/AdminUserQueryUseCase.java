package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.AdminPendingUserResult;

import java.util.List;

public interface AdminUserQueryUseCase {
    List<AdminPendingUserResult> listPendingUsers();
}
