package com.attestry.dpp.application.dto.request;

import com.attestry.dpp.domain.model.ServiceKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceSubmitRequest {
    @NotBlank
    private String assetId;

    @NotNull
    private ServiceKind kind;
}
