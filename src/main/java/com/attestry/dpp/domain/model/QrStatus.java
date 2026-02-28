package com.attestry.dpp.domain.model;

/**
 * QR 코드의 활성 상태를 나타내는 열거형.
 *
 * ACTIVE → 정상 사용 가능
 * REVOKED → 관리자에 의해 무효화됨
 */
public enum QrStatus {
    ACTIVE,
    REVOKED
}
