package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.ServiceSubmitRequest;
import com.attestry.dpp.application.dto.result.ServiceSubmitResult;
import com.attestry.dpp.domain.exception.BadRequestException;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.exception.UnauthorizedException;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.ServiceCase;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.AssetRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
import com.attestry.dpp.domain.repository.ServiceRepository;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.domain.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceCaseCommandUseCaseHandler implements ServiceCaseCommandUseCase {

    private final ServiceRepository serviceRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final PassportRepository passportRepository;
    private final LedgerService ledgerService;

    /**
     * 서비스 케이스를 접수합니다.
     * 자산/제공자 존재 검증 후 도메인 규칙(ServiceCase.submit)으로 케이스를 생성합니다.
     */
    @Transactional
    public ServiceSubmitResult submitService(ServiceSubmitRequest request, String providerId) {
        Asset asset = findAsset(request.getAssetId());
        User provider = findUser(providerId, "서비스 제공자");

        ServiceCase svcCase = ServiceCase.submit(asset, provider, request.getKind());
        serviceRepository.save(svcCase);

        return ServiceSubmitResult.from(svcCase.getId());
    }

    @Transactional
    public void completeService(String caseId, String providerId) {
        ServiceCase svcCase = findServiceCase(caseId);
        validateProviderOwnership(svcCase, providerId);
        completeServiceCase(svcCase);
        serviceRepository.save(svcCase);
    }

    /**
     * 서비스 완료 승인.
     * 소유자 승인 시점에 SERVICE_CONFIRMED 이벤트를 원장에 기록합니다.
     */
    @Transactional
    public void approveService(String caseId, String ownerId) {
        ServiceCase svcCase = findServiceCase(caseId);
        User owner = findUser(ownerId, "소유자");
        approveServiceCase(svcCase, owner);
        serviceRepository.save(svcCase);

        DigitalPassport passport = findPassportByAsset(svcCase.getAsset().getId());
        ledgerService.recordEntry(passport, LedgerAction.SERVICE_CONFIRMED, "OWNER", owner.getId(), null, caseId);
    }

    private Asset findAsset(String assetId) {
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("자산을 찾을 수 없습니다: " + assetId));
    }

    private ServiceCase findServiceCase(String caseId) {
        return serviceRepository.findById(caseId)
                .orElseThrow(() -> new NotFoundException("서비스 케이스를 찾을 수 없습니다: " + caseId));
    }

    private User findUser(String userId, String label) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(label + "를 찾을 수 없습니다."));
    }

    private DigitalPassport findPassportByAsset(String assetId) {
        return passportRepository.findByAssetId(assetId)
                .orElseThrow(() -> new NotFoundException("자산에 대한 여권을 찾을 수 없습니다: " + assetId));
    }

    private void validateProviderOwnership(ServiceCase svcCase, String providerId) {
        // 권한 검증: 케이스를 생성한 동일 제공자만 완료 처리 가능
        if (!svcCase.getProvider().getId().equals(providerId)) {
            throw new UnauthorizedException("해당 서비스 케이스의 담당 제공자가 아닙니다.");
        }
    }

    private void completeServiceCase(ServiceCase svcCase) {
        try {
            svcCase.complete();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private void approveServiceCase(ServiceCase svcCase, User owner) {
        try {
            svcCase.approve(owner);
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
