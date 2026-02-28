package com.attestry.dpp.infrastructure.web.response;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
public class ServiceCaseResponse {
    private final String caseId;
    private final String assetId;
    private final String providerId;
    private final String approvedById;
    private final String kind;
    private final String state;
    private final LocalDateTime submittedAt;
    private final LocalDateTime approvedAt;
}
