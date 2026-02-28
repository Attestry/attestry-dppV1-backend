package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.AuthLoginRequest;
import com.attestry.dpp.application.dto.request.AuthSignupRequest;
import com.attestry.dpp.application.dto.result.AuthLoginResult;
import com.attestry.dpp.application.dto.result.AuthSignupResult;

public interface AuthCommandUseCase {
    AuthSignupResult signup(AuthSignupRequest request);
    AuthLoginResult login(AuthLoginRequest request);
}
