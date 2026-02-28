package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.TransferInitiateRequest;
import com.attestry.dpp.application.dto.result.TransferInitiateResult;
import com.attestry.dpp.domain.exception.BadRequestException;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.Ownership;
import com.attestry.dpp.domain.model.TransferState;
import com.attestry.dpp.domain.model.TransferToken;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.OwnershipRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.domain.service.LedgerService;
import com.attestry.dpp.infrastructure.persistence.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferCommandUseCaseHandler implements TransferCommandUseCase {

    private final TransferRepository transferRepository;
    private final PassportRepository passportRepository;
    private final UserRepository userRepository;
    private final OwnershipRepository ownershipRepository;
    private final LedgerService ledgerService;

    /**
     * 소유자가 이전 토큰(또는 1회용 코드)을 발급합니다.
     * 호출자 소유권을 먼저 검증해 타인 자산 이전 발급을 차단합니다.
     */
    @Transactional
    public TransferInitiateResult initiateTransfer(TransferInitiateRequest request, String fromUserId) {
        DigitalPassport passport = findPassport(request.getPassportId());
        User fromUser = findUser(fromUserId);
        validateOwnership(passport.getId(), fromUserId);
        TransferToken transfer = TransferToken.create(
                passport, fromUser, request.getMethod(), request.getReceiptNumber(), request.getEvidenceUrls());
        transferRepository.save(transfer);
        return toInitiateResponse(transfer);
    }

    /**
     * 토큰/코드를 수락하고 소유권 및 원장을 함께 갱신합니다.
     * 세 단계(토큰 상태 변경 -> 소유권 이전 -> 원장 기록)는 하나의 트랜잭션으로 처리됩니다.
     */
    @Transactional
    public void acceptTransfer(String tokenOrCode, String toUserId) {
        TransferToken transfer = findInitiatedTransfer(tokenOrCode);
        User toUser = findUser(toUserId);
        acceptTransferState(transfer, toUser);
        transferOwnership(transfer, toUser);
        recordTransferLedger(transfer, toUser);
    }

    @Transactional
    public void cancelTransfer(String tokenId) {
        TransferToken transfer = transferRepository.findById(tokenId)
                .orElseThrow(() -> new NotFoundException("이전 토큰을 찾을 수 없습니다."));

        try {
            transfer.cancel();
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
        transferRepository.save(transfer);
    }

    private TransferToken findInitiatedTransfer(String tokenOrCode) {
        return transferRepository.findByIdOrCodeAndState(tokenOrCode, TransferState.INITIATED)
                .orElseThrow(() -> new BadRequestException("유효하지 않은 이전 토큰/코드입니다."));
    }

    private DigitalPassport findPassport(String passportId) {
        return passportRepository.findById(passportId)
                .orElseThrow(() -> new NotFoundException("여권을 찾을 수 없습니다."));
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    private void validateOwnership(String passportId, String ownerId) {
        Ownership ownership = ownershipRepository.findById(passportId)
                .orElseThrow(() -> new BadRequestException("해당 여권의 소유권 기록이 없습니다."));
        if (!ownership.isOwnedBy(ownerId)) {
            throw new BadRequestException("해당 여권의 소유자가 아닙니다.");
        }
    }

    private void acceptTransferState(TransferToken transfer, User toUser) {
        try {
            transfer.accept(toUser);
        } catch (IllegalStateException e) {
            // 실패 시도 횟수 누적 등 도메인 내부 상태 변화를 DB에 반영
            transferRepository.save(transfer);
            throw new BadRequestException(e.getMessage());
        }
        transferRepository.save(transfer);
    }

    private void transferOwnership(TransferToken transfer, User toUser) {
        String passportId = transfer.getPassport().getId();
        Ownership ownership = ownershipRepository.findById(passportId)
                .orElseGet(() -> Ownership.establish(transfer.getPassport(), toUser));
        ownership.transferTo(toUser);
        ownershipRepository.save(ownership);
    }

    private void recordTransferLedger(TransferToken transfer, User toUser) {
        // 최초 클레임(판매처 -> 첫 소유자)과 일반 양도(소유자 -> 소유자)를 원장에서 구분
        LedgerAction action = transfer.isFirstClaim() ? LedgerAction.CLAIMED : LedgerAction.TRANSFER_COMPLETED;
        String actorRole = action == LedgerAction.CLAIMED ? "OWNER" : "SYSTEM";
        ledgerService.recordEntry(transfer.getPassport(), action, actorRole, toUser.getId(), null, transfer.getId());
    }

    private TransferInitiateResult toInitiateResponse(TransferToken transfer) {
        return TransferInitiateResult.of(transfer.getId(), transfer.getCode());
    }
}
