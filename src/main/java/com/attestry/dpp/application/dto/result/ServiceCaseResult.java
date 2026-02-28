package com.attestry.dpp.application.dto.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
public class ServiceCaseResult {
    private final String caseId;
    private final String assetId;
    private final String providerId;
    private final String approvedById;
    private final String kind;
    private final String state;
    private final LocalDateTime submittedAt;
    private final LocalDateTime approvedAt;
}
