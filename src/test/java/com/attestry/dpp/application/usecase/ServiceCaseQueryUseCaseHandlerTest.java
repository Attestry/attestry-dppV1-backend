package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.result.ServiceCaseResult;
import com.attestry.dpp.application.usecase.query.ServiceCaseQueryUseCaseHandler;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.ServiceCase;
import com.attestry.dpp.domain.model.ServiceKind;
import com.attestry.dpp.domain.model.ServiceState;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.ServiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceCaseQueryUseCaseHandlerTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceCaseQueryUseCaseHandler handler;

    @Test
    @DisplayName("getServiceCase: 도메인 엔티티를 조회 DTO로 변환한다")
    void getServiceCase_mapsEntityToResult() {
        User provider = User.builder().id("P1").build();
        Asset asset = Asset.builder().id("A1").build();
        ServiceCase serviceCase = ServiceCase.builder()
                .id("SC1")
                .asset(asset)
                .provider(provider)
                .kind(ServiceKind.REPAIR)
                .state(ServiceState.REQUESTED)
                .build();
        when(serviceRepository.findById("SC1")).thenReturn(Optional.of(serviceCase));

        ServiceCaseResult result = handler.getServiceCase("SC1");

        assertThat(result.getCaseId()).isEqualTo("SC1");
        assertThat(result.getAssetId()).isEqualTo("A1");
        assertThat(result.getProviderId()).isEqualTo("P1");
        assertThat(result.getKind()).isEqualTo("REPAIR");
    }
}

