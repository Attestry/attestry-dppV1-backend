package com.attestry.dpp.infrastructure.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PassportMyPassportResponse {
    private final String passportId;
    private final String assetId;
    private final String modelName;
    private final String serialNumber;
    private final String qrPublicCode;
    private final String sinceAt;
    private final String imageUrl;
}
