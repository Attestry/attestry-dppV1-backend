package com.attestry.dpp.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "password")
public class AuthLoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
