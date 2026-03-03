package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.result.PassportMyPassportResult;
import com.attestry.dpp.application.dto.result.PassportPublicViewResult;
import com.attestry.dpp.application.usecase.query.PassportQueryUseCaseHandler;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.LedgerEntry;
import com.attestry.dpp.domain.model.Ownership;
import com.attestry.dpp.domain.model.RegistrationRequest;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.LedgerRepository;
import com.attestry.dpp.domain.repository.OwnershipRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
import com.attestry.dpp.domain.repository.RegistrationRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PassportQueryUseCaseHandlerTest {

    @Mock
    private PassportRepository passportRepository;
    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private OwnershipRepository ownershipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private PassportQueryUseCaseHandler handler;

    @Test
    @DisplayName("getPublicPassport: 공개 여권 조회 시 소유자/증빙/원장 이벤트를 반환한다")
    void getPublicPassport_returnsPublicView() {
        Asset asset = Asset.builder().id("A1").modelName("Model X").serialNumber("SN-001").build();
        DigitalPassport passport = DigitalPassport.builder().id("P1").asset(asset).qrPublicCode("QR111").build();
        User owner = User.builder().id("U_OWNER").email("owner@test.com").build();
        Ownership ownership = Ownership.builder()
                .passportId("P1")
                .passport(passport)
                .owner(owner)
                .sinceAt(LocalDateTime.of(2026, 1, 10, 12, 0))
                .version(1)
                .build();
        LedgerEntry event = LedgerEntry.builder()
                .id("LE1")
                .passport(passport)
                .seq(1)
                .eventAction(LedgerAction.MINTED)
                .actorRole("SYSTEM")
                .actorId("U_OWNER")
                .hash("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                .occurredAt(LocalDateTime.of(2026, 1, 10, 13, 0))
                .build();
        when(passportRepository.findByQrPublicCodeIgnoreCase("QR111")).thenReturn(Optional.of(passport));
        when(ownershipRepository.findById("P1")).thenReturn(Optional.of(ownership));
        when(ledgerRepository.findByPassportIdOrderBySeqAsc("P1")).thenReturn(List.of(event));
        when(userRepository.findById("U_OWNER")).thenReturn(Optional.of(owner));

        PassportPublicViewResult result = handler.getPublicPassport("QR111");

        assertThat(result.getPassportId()).isEqualTo("P1");
        assertThat(result.getModelName()).isEqualTo("Model X");
        assertThat(result.getImageUrl()).isNull();
        assertThat(result.getCurrentOwnerName()).startsWith("o");
        assertThat(result.getLedgerEvents()).hasSize(1);
        assertThat(result.getLedgerEvents().get(0).getAction()).isEqualTo("MINTED");
    }

    @Test
    @DisplayName("getPublicPassport: QR 코드가 없으면 NotFoundException")
    void getPublicPassport_throwsWhenNotFound() {
        when(passportRepository.findByQrPublicCodeIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.getPublicPassport("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("getMyPassports: 보유 여권 페이지를 응답 DTO로 변환한다")
    void getMyPassports_mapsPage() {
        Asset asset = Asset.builder().id("A1").modelName("Model Y").serialNumber("SN-777").build();
        DigitalPassport passport = DigitalPassport.builder().id("P9").asset(asset).qrPublicCode("QR999").build();
        User owner = User.builder().id("U1").build();
        Ownership ownership = Ownership.builder()
                .passportId("P9")
                .passport(passport)
                .owner(owner)
                .sinceAt(LocalDateTime.of(2026, 2, 1, 9, 30))
                .version(2)
                .build();
        RegistrationRequest request = RegistrationRequest.builder()
                .requestId("REQ9")
                .serialNumber("SN-777")
                .modelName("Model Y")
                .evidenceUrls("https://img.example.com/one.jpg")
                .build();

        when(ownershipRepository.findByOwnerId("U1", PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(ownership), PageRequest.of(0, 5), 1));
        when(registrationRepository.findBySerialNumberAndModelName("SN-777", "Model Y")).thenReturn(List.of(request));

        Page<PassportMyPassportResult> page = handler.getMyPassports("U1", PageRequest.of(0, 5));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getPassportId()).isEqualTo("P9");
        assertThat(page.getContent().get(0).getImageUrl()).isEqualTo("https://img.example.com/one.jpg");
    }

    @Test
    @DisplayName("getPublicPassport: 공개 여권 URL 형태 입력에서도 qrPublicCode를 추출해 조회한다")
    void getPublicPassport_extractsCodeFromUrlPayload() {
        Asset asset = Asset.builder().id("A1").modelName("Model X").serialNumber("SN-001").build();
        DigitalPassport passport = DigitalPassport.builder().id("P1").asset(asset).qrPublicCode("QR111").build();

        when(passportRepository.findByQrPublicCodeIgnoreCase("QR111")).thenReturn(Optional.of(passport));
        when(ownershipRepository.findById("P1")).thenReturn(Optional.empty());
        when(ledgerRepository.findByPassportIdOrderBySeqAsc("P1")).thenReturn(List.of());

        PassportPublicViewResult result = handler.getPublicPassport("https://localhost:5174/p/QR111?x=1");

        assertThat(result.getQrPublicCode()).isEqualTo("QR111");
    }
}
