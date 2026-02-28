package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.BrandMintRequest;
import com.attestry.dpp.application.dto.request.BrandReleaseRequest;
import com.attestry.dpp.application.dto.result.BrandMintResult;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.exception.UnauthorizedException;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.AssetRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.domain.service.LedgerService;
import com.attestry.dpp.domain.util.GenesisHashBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 브랜드 전용 서비스.
 * 제품 민팅(최초 등록)과 유통처 출고를 담당합니다.
 * PassportService에서 분리됨 — SRP 준수.
 */
@Service
@RequiredArgsConstructor
public class BrandCommandUseCaseHandler implements BrandCommandUseCase {

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final PassportRepository passportRepository;
    private final LedgerService ledgerService;

    /**
     * 브랜드가 새로운 제품을 민팅합니다.
     * Asset → DigitalPassport → Genesis Ledger Entry 순서로 생성됩니다.
     *
     * @return 생성된 민팅 결과
     */
    @Transactional
    public BrandMintResult mintAsset(BrandMintRequest request, String brandId) {
        User brand = userRepository.findById(brandId)
                .orElseThrow(() -> new NotFoundException("브랜드를 찾을 수 없습니다."));
        if (brand.getRole() != User.Role.BRAND) {
            throw new UnauthorizedException("브랜드 계정만 민팅할 수 있습니다.");
        }

        // 1. Asset 생성 — 팩토리 메서드 사용
        Asset asset = Asset.mint(request.getModelName(), request.getSerialNumber(), brand);
        assetRepository.save(asset);

        // 2. DigitalPassport 발행 — 팩토리 메서드 사용
        DigitalPassport passport = DigitalPassport.issue(asset);
        passportRepository.save(passport);

        // 3. Genesis 원장 기록
        String dataJson = GenesisHashBuilder.buildDataJson(asset, passport);
        ledgerService.recordEntry(passport, LedgerAction.MINTED, "BRAND",
                brand.getId(), dataJson, null);

        return BrandMintResult.from(passport.getQrPublicCode());
    }

    /**
     * 브랜드가 제품을 유통처에 출고합니다.
     * Asset 상태가 RELEASED로 변경되고, 원장에 RELEASED 이벤트가 기록됩니다.
     */
    @Transactional
    public void releaseAsset(BrandReleaseRequest request, String brandId) {
        DigitalPassport passport = passportRepository.findById(request.getPassportId())
                .orElseThrow(() -> new NotFoundException("여권을 찾을 수 없습니다."));

        if (!passport.getAsset().getMintedBy().getId().equals(brandId)) {
            throw new UnauthorizedException("해당 자산의 출고 권한이 없습니다.");
        }

        // Asset 도메인 메서드로 상태 전환 (규칙 검증 포함)
        passport.getAsset().release();
        assetRepository.save(passport.getAsset());

        // 원장 기록
        ledgerService.recordEntry(passport, LedgerAction.RELEASED, "BRAND",
                brandId, null, null);
    }
}
