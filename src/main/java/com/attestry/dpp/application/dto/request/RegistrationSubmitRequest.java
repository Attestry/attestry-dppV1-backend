package com.attestry.dpp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationSubmitRequest {
    @NotBlank
    private String modelName;

    @NotBlank
    private String serialNumber;

    @NotBlank
    private String evidenceUrls;
}
