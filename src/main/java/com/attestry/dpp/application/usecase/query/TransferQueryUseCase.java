package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.TransferDetailsResult;

import com.attestry.dpp.application.dto.result.TransferInitiateResult;

public interface TransferQueryUseCase {
    TransferDetailsResult getTokenDetails(String tokenId);

    TransferInitiateResult getActiveTransfer(String passportId);
}
