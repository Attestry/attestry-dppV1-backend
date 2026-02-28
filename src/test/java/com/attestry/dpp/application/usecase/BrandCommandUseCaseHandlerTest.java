package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.request.BrandMintRequest;
import com.attestry.dpp.application.dto.request.BrandReleaseRequest;
import com.attestry.dpp.application.dto.result.BrandMintResult;
import com.attestry.dpp.application.usecase.command.BrandCommandUseCaseHandler;
import com.attestry.dpp.domain.exception.UnauthorizedException;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.AssetStatus;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.AssetRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
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
class BrandCommandUseCaseHandlerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private PassportRepository passportRepository;
    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private BrandCommandUseCaseHandler handler;

    @Test
    @DisplayName("mintAsset: 브랜드 계정은 민팅에 성공한다")
    void mintAsset_successForBrandRole() {
        User brand = User.builder().id("B1").role(User.Role.BRAND).build();
        BrandMintRequest req = new BrandMintRequest();
        req.setModelName("M");
        req.setSerialNumber("S");
        when(userRepository.findById("B1")).thenReturn(Optional.of(brand));
        when(assetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BrandMintResult result = handler.mintAsset(req, "B1");

        assertThat(result.getQrCode()).startsWith("QR");
        verify(ledgerService).recordEntry(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("mintAsset: 브랜드가 아니면 UnauthorizedException")
    void mintAsset_failsForNonBrandRole() {
        User owner = User.builder().id("U1").role(User.Role.OWNER).build();
        when(userRepository.findById("U1")).thenReturn(Optional.of(owner));

        BrandMintRequest req = new BrandMintRequest();
        req.setModelName("M");
        req.setSerialNumber("S");

        assertThatThrownBy(() -> handler.mintAsset(req, "U1"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("releaseAsset: 민팅 주체 브랜드만 출고할 수 있다")
    void releaseAsset_onlyMintedByCanRelease() {
        User brandA = User.builder().id("B1").role(User.Role.BRAND).build();
        User brandB = User.builder().id("B2").role(User.Role.BRAND).build();
        Asset asset = Asset.builder()
                .id("A1")
                .status(AssetStatus.MINTED)
                .mintedBy(brandA)
                .build();
        DigitalPassport passport = DigitalPassport.builder().id("P1").asset(asset).build();
        when(passportRepository.findById("P1")).thenReturn(Optional.of(passport));

        BrandReleaseRequest req = new BrandReleaseRequest();
        req.setPassportId("P1");

        assertThatThrownBy(() -> handler.releaseAsset(req, brandB.getId()))
                .isInstanceOf(UnauthorizedException.class);
    }
}

