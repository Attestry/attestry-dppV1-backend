package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.TransferInitiateRequest;
import com.attestry.dpp.application.dto.result.TransferInitiateResult;

public interface TransferCommandUseCase {
    TransferInitiateResult initiateTransfer(TransferInitiateRequest request, String fromUserId);
    void acceptTransfer(String tokenOrCode, String toUserId);
    void cancelTransfer(String tokenId);
}
