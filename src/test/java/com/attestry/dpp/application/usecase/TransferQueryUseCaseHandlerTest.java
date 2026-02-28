package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.result.TransferDetailsResult;
import com.attestry.dpp.application.usecase.query.TransferQueryUseCaseHandler;
import com.attestry.dpp.domain.model.AcceptMethod;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.TransferState;
import com.attestry.dpp.domain.model.TransferToken;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.infrastructure.persistence.TransferRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferQueryUseCaseHandlerTest {

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private TransferQueryUseCaseHandler handler;

    @Test
    @DisplayName("getTokenDetails: 토큰/코드로 INITIATED 이전 정보를 조회한다")
    void getTokenDetails_returnsPendingStatusForInitiated() {
        Asset asset = Asset.builder().id("A1").modelName("ModelX").serialNumber("SN1").build();
        DigitalPassport passport = DigitalPassport.builder().id("P1").asset(asset).build();
        User fromUser = User.builder().id("U1").email("seller@test.com").build();
        TransferToken token = TransferToken.builder()
                .id("tr_1")
                .passport(passport)
                .fromUser(fromUser)
                .state(TransferState.INITIATED)
                .acceptMethod(AcceptMethod.ONE_TIME_CODE)
                .code("ABC123")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(transferRepository.findByIdOrCodeAndState("ABC123", TransferState.INITIATED))
                .thenReturn(Optional.of(token));

        TransferDetailsResult result = handler.getTokenDetails("ABC123");

        assertThat(result.getTransferToken()).isEqualTo("tr_1");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getModelName()).isEqualTo("ModelX");
    }
}

