package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.TransferDetailsResult;

public interface TransferQueryUseCase {
    TransferDetailsResult getTokenDetails(String tokenId);
}
