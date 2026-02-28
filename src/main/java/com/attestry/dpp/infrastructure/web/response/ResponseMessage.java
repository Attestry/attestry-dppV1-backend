package com.attestry.dpp.infrastructure.web.response;

public final class ResponseMessage {

    private ResponseMessage() {
    }

    public static final String SUCCESS_DEFAULT = "요청이 성공적으로 처리되었습니다.";

    public static final String SUCCESS_ADMIN_USER_APPROVED = "사용자 계정 승인 처리가 완료되었습니다.";
    public static final String SUCCESS_ADMIN_USER_REJECTED = "사용자 계정 반려 처리가 완료되었습니다.";

    public static final String SUCCESS_REGISTRATION_APPROVED = "등록 요청 승인 처리가 완료되었습니다.";
    public static final String SUCCESS_REGISTRATION_REJECTED = "등록 요청 반려 처리가 완료되었습니다.";

    public static final String SUCCESS_TRANSFER_ACCEPTED = "소유권 이전 수락이 완료되었습니다.";
    public static final String SUCCESS_TRANSFER_CANCELED = "소유권 이전 취소가 완료되었습니다.";

    public static final String SUCCESS_SERVICE_COMPLETED = "서비스 완료 처리가 반영되었습니다.";
    public static final String SUCCESS_SERVICE_APPROVED = "서비스 승인 처리가 완료되었습니다.";

    public static final String SUCCESS_BRAND_RELEASED = "출고 처리가 완료되었습니다.";
}
