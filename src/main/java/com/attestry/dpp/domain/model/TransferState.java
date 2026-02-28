package com.attestry.dpp.domain.model;

/**
 * 소유권 이전 토큰의 상태를 나타내는 열거형.
 *
 * INITIATED → 이전 요청이 생성됨 (대기 중)
 * COMPLETED → 수신자가 수락하여 이전 완료
 * CANCELLED → 발신자가 취소함
 */
public enum TransferState {
    INITIATED,
    COMPLETED,
    CANCELLED
}
