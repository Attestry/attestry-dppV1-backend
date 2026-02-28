package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserCommandUseCaseHandler implements AdminUserCommandUseCase {

    private final UserRepository userRepository;

    /**
     * 관리자 승인 처리.
     * 대상 사용자가 PENDING 상태가 아니면 도메인 메서드에서 예외가 발생합니다.
     */
    @Transactional
    public void approveUser(String userId) {
        User user = findUser(userId);
        user.approve();
        userRepository.save(user);
    }

    /**
     * 관리자 반려 처리.
     * 대상 사용자가 PENDING 상태가 아니면 도메인 메서드에서 예외가 발생합니다.
     */
    @Transactional
    public void rejectUser(String userId) {
        User user = findUser(userId);
        user.reject();
        userRepository.save(user);
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
