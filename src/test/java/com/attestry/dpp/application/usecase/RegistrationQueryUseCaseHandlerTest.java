package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.result.RegistrationRequestResult;
import com.attestry.dpp.application.port.FileReadUrlPort;
import com.attestry.dpp.application.usecase.query.RegistrationQueryUseCaseHandler;
import com.attestry.dpp.domain.model.RegistrationRequest;
import com.attestry.dpp.domain.model.RegistrationStatus;
import com.attestry.dpp.domain.repository.RegistrationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationQueryUseCaseHandlerTest {

    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private FileReadUrlPort fileReadUrlPort;

    @InjectMocks
    private RegistrationQueryUseCaseHandler handler;

    @Test
    @DisplayName("listAllRequests: PENDING 요청만 페이지 조회 후 DTO로 변환한다")
    void listAllRequests_mapsPendingPage() {
        RegistrationRequest request = RegistrationRequest.builder()
                .requestId("REQ-1")
                .modelName("Model A")
                .serialNumber("SN-A")
                .evidenceUrls("[]")
                .requesterId("U1")
                .status(RegistrationStatus.PENDING)
                .createdAt(LocalDateTime.of(2026, 2, 1, 10, 0))
                .build();
        PageRequest pageable = PageRequest.of(0, 20);

        when(registrationRepository.findByStatus(RegistrationStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(request), pageable, 1));

        Page<RegistrationRequestResult> result = handler.listAllRequests(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getRequestId()).isEqualTo("REQ-1");
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("PENDING");
    }
}
