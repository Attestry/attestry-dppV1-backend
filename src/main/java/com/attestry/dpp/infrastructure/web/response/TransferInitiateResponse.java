package com.attestry.dpp.infrastructure.web.response;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "of")
public class TransferInitiateResponse {
    private final String transferToken;
    private final String code;
    private final java.time.LocalDateTime expiresAt;
}
