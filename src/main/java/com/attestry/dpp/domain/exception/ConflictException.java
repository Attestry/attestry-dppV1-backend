package com.attestry.dpp.domain.exception;

public class ConflictException extends CustomException {
    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
