package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.request.ServiceSubmitRequest;
import com.attestry.dpp.application.dto.result.ServiceSubmitResult;
import com.attestry.dpp.application.usecase.command.ServiceCaseCommandUseCaseHandler;
import com.attestry.dpp.domain.exception.UnauthorizedException;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.ServiceCase;
import com.attestry.dpp.domain.model.ServiceKind;
import com.attestry.dpp.domain.model.ServiceState;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.AssetRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
import com.attestry.dpp.domain.repository.ServiceRepository;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.domain.service.LedgerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceCaseCommandUseCaseHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PassportRepository passportRepository;
    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private ServiceCaseCommandUseCaseHandler handler;

    @Test
    @DisplayName("submitService: 서비스 케이스를 생성하고 caseId를 반환한다")
    void submitService_createsServiceCase() {
        Asset asset = Asset.builder().id("A1").build();
        User provider = User.builder().id("P1").role(User.Role.PROVIDER).build();
        when(assetRepository.findById("A1")).thenReturn(Optional.of(asset));
        when(userRepository.findById("P1")).thenReturn(Optional.of(provider));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ServiceSubmitRequest req = new ServiceSubmitRequest();
        req.setAssetId("A1");
        req.setKind(ServiceKind.REPAIR);

        ServiceSubmitResult result = handler.submitService(req, "P1");
        assertThat(result.getCaseId()).startsWith("SC-");
    }

    @Test
    @DisplayName("completeService: 담당 제공자가 아니면 UnauthorizedException")
    void completeService_failsForDifferentProvider() {
        User provider = User.builder().id("P1").build();
        User other = User.builder().id("P2").build();
        Asset asset = Asset.builder().id("A1").build();
        ServiceCase serviceCase = ServiceCase.builder()
                .id("SC1")
                .asset(asset)
                .provider(provider)
                .state(ServiceState.REQUESTED)
                .build();
        when(serviceRepository.findById("SC1")).thenReturn(Optional.of(serviceCase));

        assertThatThrownBy(() -> handler.completeService("SC1", other.getId()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("approveService: 승인 시 원장 기록이 남는다")
    void approveService_recordsLedger() {
        User provider = User.builder().id("P1").build();
        User owner = User.builder().id("O1").role(User.Role.OWNER).build();
        Asset asset = Asset.builder().id("A1").build();
        ServiceCase serviceCase = ServiceCase.builder()
                .id("SC1")
                .asset(asset)
                .provider(provider)
                .state(ServiceState.COMPLETED)
                .build();
        DigitalPassport passport = DigitalPassport.builder().id("PPT1").asset(asset).build();

        when(serviceRepository.findById("SC1")).thenReturn(Optional.of(serviceCase));
        when(userRepository.findById("O1")).thenReturn(Optional.of(owner));
        when(passportRepository.findByAssetId("A1")).thenReturn(Optional.of(passport));
        when(serviceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        handler.approveService("SC1", "O1");

        verify(ledgerService).recordEntry(any(), any(), any(), any(), any(), any());
    }
}

