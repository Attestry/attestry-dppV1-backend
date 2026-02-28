package com.attestry.dpp.domain.model;

/**
 * 자가등록 요청의 처리 상태를 나타내는 열거형.
 *
 * PENDING → 관리자 승인 대기 중
 * APPROVED → 승인 완료
 * REJECTED → 거절됨
 */
public enum RegistrationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
