package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.AdminPendingUserResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserQueryUseCase {
    Page<AdminPendingUserResult> listPendingUsers(Pageable pageable);
}
