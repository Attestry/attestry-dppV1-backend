package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.RegistrationSubmitRequest;
import com.attestry.dpp.application.dto.result.RegistrationRequestResult;
import com.attestry.dpp.domain.exception.BadRequestException;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.LedgerEntry;
import com.attestry.dpp.domain.model.Ownership;
import com.attestry.dpp.domain.model.RegistrationRequest;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.AssetRepository;
import com.attestry.dpp.domain.repository.OwnershipRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
import com.attestry.dpp.domain.repository.RegistrationRepository;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.domain.service.LedgerService;
import com.attestry.dpp.domain.util.GenesisHashBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationCommandUseCaseHandler implements RegistrationCommandUseCase {

    private final RegistrationRepository registrationRepository;
    private final AssetRepository assetRepository;
    private final PassportRepository passportRepository;
    private final UserRepository userRepository;
    private final OwnershipRepository ownershipRepository;
    private final LedgerService ledgerService;

    @Transactional
    public RegistrationRequestResult submitRequest(RegistrationSubmitRequest request, String requesterId) {
        findUser(requesterId, "요청자");
        RegistrationRequest entity = RegistrationRequest.submit(
                request.getModelName(),
                request.getSerialNumber(),
                request.getEvidenceUrls(),
                requesterId);

        RegistrationRequest saved = registrationRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public void approveRequest(String requestId, String adminId) {
        RegistrationRequest request = findRequest(requestId);
        User requester = findUser(request.getRequesterId(), "요청자");
        User admin = findUser(adminId, "관리자");
        approvePendingRequest(request);
        MintedPassport minted = mintNewPassport(request, admin);
        establishOwnership(minted.passport(), requester, minted.mintedLedgerId());
        registrationRepository.save(request);
    }

    @Transactional
    public void rejectRequest(String requestId) {
        RegistrationRequest request = findRequest(requestId);
        rejectPendingRequest(request);
        registrationRepository.save(request);
    }

    private void approvePendingRequest(RegistrationRequest request) {
        try {
            request.approve();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private void rejectPendingRequest(RegistrationRequest request) {
        try {
            request.reject();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private RegistrationRequest findRequest(String requestId) {
        return registrationRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("요청을 찾을 수 없습니다: " + requestId));
    }

    private User findUser(String userId, String label) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(label + "를 찾을 수 없습니다: " + userId));
    }

    private MintedPassport mintNewPassport(RegistrationRequest request, User admin) {
        Asset asset = Asset.createForRegistration(request.getModelName(), request.getSerialNumber(), admin);
        assetRepository.save(asset);

        DigitalPassport passport = DigitalPassport.issue(asset);
        DigitalPassport savedPassport = passportRepository.save(passport);

        String dataJson = GenesisHashBuilder.buildDataJson(asset, savedPassport);
        LedgerEntry mintedLedger = ledgerService.recordEntry(
                savedPassport, LedgerAction.MINTED, "SYSTEM", "SYSTEM", dataJson, null);

        return new MintedPassport(savedPassport, mintedLedger.getId());
    }

    private void establishOwnership(DigitalPassport passport, User owner, String correlationId) {
        Ownership ownership = Ownership.establish(passport, owner);
        ownershipRepository.save(ownership);

        // 등록 승인 경로의 최초 소유권 부여를 원장에 남김
        String claimData = String.format(
                "{\"eventType\":\"%s\",\"fromOwnerId\":null,\"toOwnerId\":\"%s\",\"registrationMethod\":\"APP_ISSUED\",\"evidenceVerified\":true}",
                LedgerAction.CLAIMED.name(),
                owner.getId());
        ledgerService.recordEntry(passport, LedgerAction.CLAIMED, "OWNER", owner.getId(), claimData, correlationId);
    }

    private RegistrationRequestResult toResponse(RegistrationRequest entity) {
        return RegistrationRequestResult.of(
                entity.getRequestId(),
                entity.getModelName(),
                entity.getSerialNumber(),
                entity.getEvidenceUrls(),
                entity.getRequesterId(),
                entity.getStatus().name(),
                entity.getCreatedAt());
    }

    private record MintedPassport(DigitalPassport passport, String mintedLedgerId) {
    }
}
