package com.attestry.dpp.application.usecase;
import com.attestry.dpp.domain.service.LedgerService;
import com.attestry.dpp.application.usecase.command.TransferCommandUseCaseHandler;


import com.attestry.dpp.application.dto.request.TransferInitiateRequest;
import com.attestry.dpp.application.dto.result.TransferInitiateResult;
import com.attestry.dpp.domain.exception.BadRequestException;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.AcceptMethod;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.AssetStatus;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.LedgerEntry;
import com.attestry.dpp.domain.model.Ownership;
import com.attestry.dpp.domain.model.TransferState;
import com.attestry.dpp.domain.model.TransferToken;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.OwnershipRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.infrastructure.persistence.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferCommandUseCaseHandlerTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private PassportRepository passportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OwnershipRepository ownershipRepository;
    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private TransferCommandUseCaseHandler transferCommandUseCaseHandler;

    private User ownerUser;
    private User buyerUser;
    private User nonOwnerUser;
    private Asset asset;
    private DigitalPassport passport;
    private Ownership ownership;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder().id("U_OWNER").email("owner@test.com").role(User.Role.OWNER).build();
        buyerUser = User.builder().id("U_BUYER").email("buyer@test.com").role(User.Role.OWNER).build();
        nonOwnerUser = User.builder().id("U_OTHER").email("other@test.com").role(User.Role.OWNER).build();
        asset = Asset.builder().id("ASSET-1").modelName("Model X").serialNumber("SN-001").status(AssetStatus.ACTIVE).build();
        passport = DigitalPassport.builder().id("P100").asset(asset).qrPublicCode("QR123").build();
        ownership = Ownership.builder().passportId("P100").passport(passport).owner(ownerUser).version(1)
                .sinceAt(LocalDateTime.now()).build();
    }

    @Nested
    @DisplayName("initiateTransfer")
    class InitiateTransferTests {

        @Test
        @DisplayName("소유자가 양도를 시작하면 성공한다")
        void ownerCanInitiateTransfer() {
            TransferInitiateRequest req = new TransferInitiateRequest();
            req.setPassportId("P100");
            req.setMethod(AcceptMethod.ONE_TIME_CODE);

            when(passportRepository.findById("P100")).thenReturn(Optional.of(passport));
            when(userRepository.findById("U_OWNER")).thenReturn(Optional.of(ownerUser));
            when(ownershipRepository.findById("P100")).thenReturn(Optional.of(ownership));
            when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransferInitiateResult resp = transferCommandUseCaseHandler.initiateTransfer(req, "U_OWNER");

            assertThat(resp.getTransferToken()).startsWith("tr_");
            assertThat(resp.getCode()).isNotNull().hasSize(6);
            verify(transferRepository).save(any(TransferToken.class));
        }

        @Test
        @DisplayName("소유자가 아닌 사용자가 양도를 시작하면 BadRequestException")
        void nonOwnerCannotInitiateTransfer() {
            TransferInitiateRequest req = new TransferInitiateRequest();
            req.setPassportId("P100");
            req.setMethod(AcceptMethod.ONE_TIME_CODE);

            when(passportRepository.findById("P100")).thenReturn(Optional.of(passport));
            when(userRepository.findById("U_OTHER")).thenReturn(Optional.of(nonOwnerUser));
            when(ownershipRepository.findById("P100")).thenReturn(Optional.of(ownership));

            assertThatThrownBy(() -> transferCommandUseCaseHandler.initiateTransfer(req, "U_OTHER"))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("존재하지 않는 여권으로 양도 시작하면 NotFoundException")
        void passportNotFound() {
            TransferInitiateRequest req = new TransferInitiateRequest();
            req.setPassportId("INVALID");
            req.setMethod(AcceptMethod.ONE_TIME_CODE);

            when(passportRepository.findById("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transferCommandUseCaseHandler.initiateTransfer(req, "U_OWNER"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("acceptTransfer")
    class AcceptTransferTests {

        @Test
        @DisplayName("유효한 토큰으로 양도 수락이 성공한다")
        void validTokenAcceptsTransfer() {
            TransferToken validToken = TransferToken.builder()
                    .id("tr_test123")
                    .passport(passport)
                    .fromUser(ownerUser)
                    .state(TransferState.INITIATED)
                    .acceptMethod(AcceptMethod.ONE_TIME_CODE)
                    .code("ABC123")
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .failedAttempts(0)
                    .build();

            when(transferRepository.findByIdOrCodeAndState("ABC123", TransferState.INITIATED))
                    .thenReturn(Optional.of(validToken));
            when(userRepository.findById("U_BUYER")).thenReturn(Optional.of(buyerUser));
            when(ownershipRepository.findById("P100")).thenReturn(Optional.of(ownership));
            when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(ownershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(ledgerService.recordEntry(any(), any(), any(), any(), any(), any()))
                    .thenReturn(LedgerEntry.builder().id("LE1").build());

            assertThatCode(() -> transferCommandUseCaseHandler.acceptTransfer("ABC123", "U_BUYER"))
                    .doesNotThrowAnyException();

            verify(transferRepository).save(any(TransferToken.class));
            verify(ownershipRepository).save(argThat(o -> "U_BUYER".equals(o.getOwner().getId())));
            verify(ledgerService).recordEntry(any(), eq(LedgerAction.TRANSFER_COMPLETED), any(), eq("U_BUYER"), any(), any());
        }

        @Test
        @DisplayName("만료된 토큰으로 양도 수락하면 BadRequestException")
        void expiredTokenRejected() {
            TransferToken expiredToken = TransferToken.builder()
                    .id("tr_expired")
                    .passport(passport)
                    .fromUser(ownerUser)
                    .state(TransferState.INITIATED)
                    .acceptMethod(AcceptMethod.ONE_TIME_CODE)
                    .code("ABC123")
                    .expiresAt(LocalDateTime.now().minusMinutes(5))
                    .failedAttempts(0)
                    .build();

            when(transferRepository.findByIdOrCodeAndState("ABC123", TransferState.INITIATED))
                    .thenReturn(Optional.of(expiredToken));
            when(userRepository.findById("U_BUYER")).thenReturn(Optional.of(buyerUser));
            when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatThrownBy(() -> transferCommandUseCaseHandler.acceptTransfer("ABC123", "U_BUYER"))
                    .isInstanceOf(BadRequestException.class);
            verify(ownershipRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 토큰/코드면 BadRequestException")
        void invalidCodeRejected() {
            when(transferRepository.findByIdOrCodeAndState("WRONG", TransferState.INITIATED))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> transferCommandUseCaseHandler.acceptTransfer("WRONG", "U_BUYER"))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("cancelTransfer")
    class CancelTransferTests {

        @Test
        @DisplayName("INITIATED 상태의 양도를 취소할 수 있다")
        void canCancelInitiatedTransfer() {
            TransferToken token = TransferToken.builder()
                    .id("tr_1")
                    .state(TransferState.INITIATED)
                    .fromUser(ownerUser)
                    .build();
            when(transferRepository.findById("tr_1")).thenReturn(Optional.of(token));
            when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            transferCommandUseCaseHandler.cancelTransfer("tr_1", "U_OWNER");

            verify(transferRepository).save(argThat(t -> TransferState.CANCELLED.equals(t.getState())));
        }

        @Test
        @DisplayName("COMPLETED 상태의 양도를 취소하면 BadRequestException")
        void cannotCancelCompletedTransfer() {
            TransferToken token = TransferToken.builder()
                    .id("tr_1")
                    .state(TransferState.COMPLETED)
                    .fromUser(ownerUser)
                    .build();
            when(transferRepository.findById("tr_1")).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> transferCommandUseCaseHandler.cancelTransfer("tr_1", "U_OWNER"))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("발신자가 아닌 사용자는 양도를 취소할 수 없다")
        void nonInitiatorCannotCancelTransfer() {
            TransferToken token = TransferToken.builder()
                    .id("tr_1")
                    .state(TransferState.INITIATED)
                    .fromUser(ownerUser)
                    .build();
            when(transferRepository.findById("tr_1")).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> transferCommandUseCaseHandler.cancelTransfer("tr_1", "U_OTHER"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("취소 권한");
        }
    }
}
