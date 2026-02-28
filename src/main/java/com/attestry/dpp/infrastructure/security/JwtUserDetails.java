package com.attestry.dpp.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Holds JWT-extracted user info for use via @AuthenticationPrincipal in
 * controllers.
 */
@Getter
@AllArgsConstructor
public class JwtUserDetails {
    private final String userId;
    private final String email;
    private final String role;
}
