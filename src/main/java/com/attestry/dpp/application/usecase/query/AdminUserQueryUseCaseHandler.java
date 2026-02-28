package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.AdminPendingUserResult;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserQueryUseCaseHandler implements AdminUserQueryUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AdminPendingUserResult> listPendingUsers() {
        return userRepository.findByStatus(User.Status.PENDING).stream()
                .map(this::toPendingUserResponse)
                .toList();
    }

    private AdminPendingUserResult toPendingUserResponse(User user) {
        return AdminPendingUserResult.of(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getPhone() != null ? user.getPhone() : "",
                user.getBusinessNumber() != null ? user.getBusinessNumber() : "",
                user.getBrandName() != null ? user.getBrandName() : "",
                user.getStatus().name());
    }
}
