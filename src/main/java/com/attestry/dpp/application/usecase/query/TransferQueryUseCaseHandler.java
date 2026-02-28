package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.TransferDetailsResult;
import com.attestry.dpp.application.usecase.query.TransferQueryUseCase;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.TransferState;
import com.attestry.dpp.domain.model.TransferToken;
import com.attestry.dpp.domain.util.NameMaskingUtil;
import com.attestry.dpp.infrastructure.persistence.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferQueryUseCaseHandler implements TransferQueryUseCase {

    private final TransferRepository transferRepository;

    /**
     * 토큰 ID 또는 1회용 코드로 이전 상세를 조회합니다.
     * 취소/완료된 토큰은 제외하고 INITIATED 상태만 노출합니다.
     */
    @Transactional(readOnly = true)
    public TransferDetailsResult getTokenDetails(String tokenOrCode) {
        TransferToken transfer = transferRepository
                .findByIdOrCodeAndState(tokenOrCode, TransferState.INITIATED)
                .orElseThrow(() -> new NotFoundException("이전 토큰을 찾을 수 없습니다."));

        return TransferDetailsResult.builder()
                .transferToken(transfer.getId())
                .passportId(transfer.getPassport().getId())
                .modelName(transfer.getPassport().getAsset().getModelName())
                .serialNumber(transfer.getPassport().getAsset().getSerialNumber())
                .receiptNumber(transfer.getReceiptNumber())
                .evidenceUrls(transfer.getEvidenceUrls())
                .method(transfer.getAcceptMethod().name())
                .status(transfer.getState() == TransferState.INITIATED ? "PENDING" : transfer.getState().name())
                .fromUserName(transfer.getFromUser() != null
                        ? NameMaskingUtil.maskEmail(transfer.getFromUser().getEmail())
                        : "공식 판매처")
                .build();
    }
}
