package com.attestry.dpp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BrandMintRequest {
    @NotBlank
    private String modelName;

    @NotBlank
    private String serialNumber;

    private Map<String, Object> attributes;
}
