package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.result.AdminPendingUserResult;
import com.attestry.dpp.application.usecase.query.AdminUserQueryUseCaseHandler;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserQueryUseCaseHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserQueryUseCaseHandler handler;

    @Test
    @DisplayName("listPendingUsers: 대기 사용자 목록을 응답 DTO로 매핑한다")
    void listPendingUsers_mapsToResult() {
        PageRequest pageable = PageRequest.of(0, 20);
        User pending = User.builder()
                .id("U1")
                .email("brand@test.com")
                .role(User.Role.BRAND)
                .status(User.Status.PENDING)
                .phone(null)
                .businessNumber(null)
                .brandName("Brand A")
                .build();
        when(userRepository.findByStatus(User.Status.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(pending), pageable, 1));

        Page<AdminPendingUserResult> result = handler.listPendingUsers(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo("U1");
        assertThat(result.getContent().get(0).getPhone()).isNull();
        assertThat(result.getContent().get(0).getBusinessNumber()).isNull();
        assertThat(result.getContent().get(0).getBrandName()).isEqualTo("Brand A");
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("PENDING");
    }
}
