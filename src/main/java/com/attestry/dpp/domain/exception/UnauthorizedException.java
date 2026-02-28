package com.attestry.dpp.domain.exception;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
