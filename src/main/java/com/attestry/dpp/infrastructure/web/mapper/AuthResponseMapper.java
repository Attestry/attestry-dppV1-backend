package com.attestry.dpp.infrastructure.web.mapper;

import com.attestry.dpp.application.dto.result.AuthLoginResult;
import com.attestry.dpp.application.dto.result.AuthRoleResult;
import com.attestry.dpp.application.dto.result.AuthSignupResult;
import com.attestry.dpp.application.dto.result.AuthStatusResult;
import com.attestry.dpp.infrastructure.web.response.AuthLoginResponse;
import com.attestry.dpp.infrastructure.web.response.AuthRole;
import com.attestry.dpp.infrastructure.web.response.AuthSignupResponse;
import com.attestry.dpp.infrastructure.web.response.AuthStatus;

public final class AuthResponseMapper {

    private AuthResponseMapper() {
    }

    public static AuthSignupResponse toAuthSignupResponse(AuthSignupResult result) {
        return AuthSignupResponse.of(
                result.getUserId(),
                result.getEmail(),
                toAuthRole(result.getRole()),
                toAuthStatus(result.getStatus()));
    }

    public static AuthLoginResponse toAuthLoginResponse(AuthLoginResult result) {
        return AuthLoginResponse.of(
                result.getUserId(),
                result.getEmail(),
                toAuthRole(result.getRole()),
                result.getAccessToken());
    }

    public static AuthRole toAuthRole(AuthRoleResult role) {
        return switch (role) {
            case BRAND -> AuthRole.BRAND;
            case RETAIL -> AuthRole.RETAIL;
            case OWNER -> AuthRole.OWNER;
            case PROVIDER -> AuthRole.PROVIDER;
            case ADMIN -> AuthRole.ADMIN;
        };
    }

    public static AuthStatus toAuthStatus(AuthStatusResult status) {
        return switch (status) {
            case PENDING -> AuthStatus.PENDING;
            case ACTIVE -> AuthStatus.ACTIVE;
            case REJECTED -> AuthStatus.REJECTED;
        };
    }
}
