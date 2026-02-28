package com.attestry.dpp.domain.model;

/**
 * 서비스 케이스의 처리 상태를 나타내는 열거형.
 *
 * REQUESTED → 서비스 요청 접수됨
 * COMPLETED → 서비스 수행 완료 (제공자)
 * APPROVED → 소유자가 서비스 확인/승인
 * REJECTED → 소유자가 서비스 거절
 */
public enum ServiceState {
    REQUESTED,
    COMPLETED,
    APPROVED,
    REJECTED
}
