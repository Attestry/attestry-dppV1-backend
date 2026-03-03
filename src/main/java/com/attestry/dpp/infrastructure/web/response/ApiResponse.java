package com.attestry.dpp.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.Objects;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private static final String SUCCESS_CODE = "SUCCESS";

    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String code, String message, T data) {
        this.code = Objects.requireNonNull(code, "응답 코드(code)는 null일 수 없습니다.");
        this.message = Objects.requireNonNull(message, "응답 메시지(message)는 null일 수 없습니다.");
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(ResponseMessage.SUCCESS_DEFAULT, data);
    }

    public static ApiResponse<Void> successMessage(String message) {
        return success(message, null);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return of(code, message, null);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return of(SUCCESS_CODE, message, data);
    }

    private static <T> ApiResponse<T> of(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
