package com.attestry.dpp.infrastructure.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferDetailsResponse {
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
