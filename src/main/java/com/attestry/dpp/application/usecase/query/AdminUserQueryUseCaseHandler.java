package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.AdminPendingUserResult;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserQueryUseCaseHandler implements AdminUserQueryUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AdminPendingUserResult> listPendingUsers(Pageable pageable) {
        return userRepository.findByStatus(User.Status.PENDING, pageable)
                .map(this::toPendingUserResponse);
    }

    private AdminPendingUserResult toPendingUserResponse(User user) {
        return AdminPendingUserResult.of(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getPhone(),
                user.getBusinessNumber(),
                user.getBrandName(),
                user.getStatus().name());
    }
}
