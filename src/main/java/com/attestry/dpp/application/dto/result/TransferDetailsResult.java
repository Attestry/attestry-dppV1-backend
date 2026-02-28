package com.attestry.dpp.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferDetailsResult {
    private final String transferToken;
    private final String passportId;
    private final String modelName;
    private final String serialNumber;
    private final String receiptNumber;
    private final String evidenceUrls;
    private final String method;
    private final String status;
    private final String fromUserName;
}
