package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.usecase.command.AdminUserCommandUseCaseHandler;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserCommandUseCaseHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserCommandUseCaseHandler handler;

    @Test
    @DisplayName("approveUser: PENDING 사용자를 승인 상태로 변경한다")
    void approveUser_changesStatusToActive() {
        User pending = User.builder().id("U1").status(User.Status.PENDING).build();
        when(userRepository.findById("U1")).thenReturn(Optional.of(pending));

        handler.approveUser("U1");

        assertThat(pending.getStatus()).isEqualTo(User.Status.ACTIVE);
        verify(userRepository).save(pending);
    }

    @Test
    @DisplayName("rejectUser: PENDING 사용자를 반려 상태로 변경한다")
    void rejectUser_changesStatusToRejected() {
        User pending = User.builder().id("U1").status(User.Status.PENDING).build();
        when(userRepository.findById("U1")).thenReturn(Optional.of(pending));

        handler.rejectUser("U1");

        assertThat(pending.getStatus()).isEqualTo(User.Status.REJECTED);
        verify(userRepository).save(pending);
    }

    @Test
    @DisplayName("approveUser: 사용자가 없으면 NotFoundException")
    void approveUser_throwsWhenNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.approveUser("missing"))
                .isInstanceOf(NotFoundException.class);
    }
}

