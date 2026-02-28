package com.attestry.dpp.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class AuthSignupResult {
    private final String userId;
    private final String email;
    private final AuthRoleResult role;
    private final AuthStatusResult status;
}
