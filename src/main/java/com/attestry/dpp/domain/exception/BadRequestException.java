package com.attestry.dpp.domain.exception;

public class BadRequestException extends CustomException {
    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
