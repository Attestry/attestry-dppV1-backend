package com.attestry.dpp.domain.exception;

public class NotFoundException extends CustomException {
    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
