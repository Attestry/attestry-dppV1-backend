package com.attestry.dpp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandReleaseRequest {
    @NotBlank
    private String passportId;
}
