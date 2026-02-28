package com.attestry.dpp.application.usecase;
import com.attestry.dpp.domain.service.LedgerService;
import com.attestry.dpp.application.usecase.command.RegistrationCommandUseCaseHandler;
import com.attestry.dpp.application.usecase.query.RegistrationQueryUseCaseHandler;


import com.attestry.dpp.application.dto.request.RegistrationSubmitRequest;
import com.attestry.dpp.application.dto.result.RegistrationRequestResult;
import com.attestry.dpp.domain.exception.BadRequestException;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.*;
import com.attestry.dpp.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationCommandUseCaseHandlerTest {

    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private PassportRepository passportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OwnershipRepository ownershipRepository;
    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private RegistrationCommandUseCaseHandler registrationCommandUseCaseHandler;

    @InjectMocks
    private RegistrationQueryUseCaseHandler registrationQueryUseCaseHandler;

    private User ownerUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder().id("U_OWNER").email("owner@test.com").role(User.Role.OWNER).build();
        adminUser = User.builder().id("U_ADMIN").email("admin@test.com").role(User.Role.ADMIN).build();
    }

    @Test
    @DisplayName("submitRequest: 등록 요청을 제출하면 PENDING 상태로 저장된다")
    void submitRequestCreatesPendingRecord() {
        RegistrationSubmitRequest req = new RegistrationSubmitRequest();
        req.setModelName("TestModel");
        req.setSerialNumber("SN-123");
        req.setEvidenceUrls("[]");
        when(userRepository.findById("U_OWNER")).thenReturn(Optional.of(ownerUser));
        when(registrationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrationRequestResult resp = registrationCommandUseCaseHandler.submitRequest(req, "U_OWNER");

        assertThat(resp.getStatus()).isEqualTo("PENDING");
        assertThat(resp.getModelName()).isEqualTo("TestModel");
        assertThat(resp.getRequestId()).startsWith("REQ-");
        verify(registrationRepository).save(any(RegistrationRequest.class));
    }

    @Test
    @DisplayName("관리자가 등록 요청을 승인하면 Asset, Passport, Ownership, Ledger가 생성된다")
    void approveRequestCreatesFullChain() {
        RegistrationRequest request = RegistrationRequest.builder()
                .requestId("REQ-1")
                .modelName("Model X")
                .serialNumber("SN-001")
                .requesterId("U_OWNER")
                .status(RegistrationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(registrationRepository.findById("REQ-1")).thenReturn(Optional.of(request));
        when(userRepository.findById("U_OWNER")).thenReturn(Optional.of(ownerUser));
        when(userRepository.findById("U_ADMIN")).thenReturn(Optional.of(adminUser));
        when(assetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ownershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(registrationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ledgerService.recordEntry(any(), any(), any(), any(), any(), any()))
                .thenReturn(LedgerEntry.builder().id("LE1").build());

        registrationCommandUseCaseHandler.approveRequest("REQ-1", "U_ADMIN");

        verify(assetRepository).save(any(Asset.class));
        verify(passportRepository).save(any(DigitalPassport.class));
        verify(ownershipRepository).save(any(Ownership.class));
        verify(ledgerService, times(2)).recordEntry(any(), any(), any(), any(), any(), any());
        assertThat(request.getStatus()).isEqualTo(RegistrationStatus.APPROVED);
    }

    @Test
    @DisplayName("이미 처리된 요청을 승인하면 BadRequestException")
    void cannotApproveAlreadyProcessedRequest() {
        RegistrationRequest request = RegistrationRequest.builder()
                .requestId("REQ-1")
                .requesterId("U_OWNER")
                .status(RegistrationStatus.APPROVED)
                .build();

        when(registrationRepository.findById("REQ-1")).thenReturn(Optional.of(request));
        when(userRepository.findById("U_OWNER")).thenReturn(Optional.of(ownerUser));
        when(userRepository.findById("U_ADMIN")).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> registrationCommandUseCaseHandler.approveRequest("REQ-1", "U_ADMIN"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이미 처리된 요청");
    }

    @Test
    @DisplayName("존재하지 않는 요청을 승인하면 NotFoundException")
    void approveNonExistentRequestThrows() {
        when(registrationRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationCommandUseCaseHandler.approveRequest("INVALID", "U_ADMIN"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("등록 요청을 반려하면 REJECTED 상태로 변경된다")
    void rejectRequestChangesStatusToRejected() {
        RegistrationRequest request = RegistrationRequest.builder()
                .requestId("REQ-1").status(RegistrationStatus.PENDING).build();

        when(registrationRepository.findById("REQ-1")).thenReturn(Optional.of(request));
        when(registrationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        registrationCommandUseCaseHandler.rejectRequest("REQ-1");

        assertThat(request.getStatus()).isEqualTo(RegistrationStatus.REJECTED);
        verify(registrationRepository).save(request);
    }

    @Test
    @DisplayName("요청자별 목록 조회가 정상 동작한다")
    void listRequestsByRequesterWorks() {
        RegistrationRequest r1 = RegistrationRequest.builder()
                .requestId("REQ-1").modelName("A").serialNumber("S1")
                .requesterId("U_OWNER").status(RegistrationStatus.PENDING).createdAt(LocalDateTime.now()).build();

        when(registrationRepository.findByRequesterId("U_OWNER")).thenReturn(List.of(r1));

        List<RegistrationRequestResult> result = registrationQueryUseCaseHandler.listRequestsByRequester("U_OWNER");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelName()).isEqualTo("A");
    }
}
