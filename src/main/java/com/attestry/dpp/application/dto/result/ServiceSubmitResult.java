package com.attestry.dpp.application.dto.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "from")
public class ServiceSubmitResult {
    private final String caseId;
}
