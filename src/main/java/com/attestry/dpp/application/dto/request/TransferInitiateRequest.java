package com.attestry.dpp.application.dto.request;

import com.attestry.dpp.domain.model.AcceptMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferInitiateRequest {
    @NotBlank
    private String passportId;

    @NotNull
    private AcceptMethod method;

    private String receiptNumber;
    private String evidenceUrls;
}
