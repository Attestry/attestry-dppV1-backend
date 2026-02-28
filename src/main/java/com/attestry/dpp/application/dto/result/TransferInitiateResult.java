package com.attestry.dpp.application.dto.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "of")
public class TransferInitiateResult {
    private final String transferToken;
    private final String code;
}
