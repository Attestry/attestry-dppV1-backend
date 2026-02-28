package com.attestry.dpp.infrastructure.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class AuthSignupResponse {
    private final String userId;
    private final String email;
    private final AuthRole role;
    private final AuthStatus status;
}
