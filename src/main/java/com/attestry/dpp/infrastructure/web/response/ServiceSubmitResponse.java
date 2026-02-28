package com.attestry.dpp.infrastructure.web.response;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "from")
public class ServiceSubmitResponse {
    private final String caseId;
}
