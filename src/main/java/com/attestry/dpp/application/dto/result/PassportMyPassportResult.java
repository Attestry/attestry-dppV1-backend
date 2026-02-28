package com.attestry.dpp.application.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PassportMyPassportResult {
    private final String passportId;
    private final String assetId;
    private final String modelName;
    private final String serialNumber;
    private final String qrPublicCode;
    private final String sinceAt;
    private final String imageUrl;
}
