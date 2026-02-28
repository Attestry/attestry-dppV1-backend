package com.attestry.dpp.infrastructure.web.mapper;

import com.attestry.dpp.application.dto.result.TransferDetailsResult;
import com.attestry.dpp.application.dto.result.TransferInitiateResult;
import com.attestry.dpp.infrastructure.web.response.TransferDetailsResponse;
import com.attestry.dpp.infrastructure.web.response.TransferInitiateResponse;

public final class TransferResponseMapper {

    private TransferResponseMapper() {
    }

    public static TransferInitiateResponse toTransferInitiateResponse(TransferInitiateResult result) {
        return TransferInitiateResponse.of(result.getTransferToken(), result.getCode());
    }

    public static TransferDetailsResponse toTransferDetailsResponse(TransferDetailsResult result) {
        return TransferDetailsResponse.builder()
                .transferToken(result.getTransferToken())
                .passportId(result.getPassportId())
                .modelName(result.getModelName())
                .serialNumber(result.getSerialNumber())
                .receiptNumber(result.getReceiptNumber())
                .evidenceUrls(result.getEvidenceUrls())
                .method(result.getMethod())
                .status(result.getStatus())
                .fromUserName(result.getFromUserName())
                .build();
    }
}
