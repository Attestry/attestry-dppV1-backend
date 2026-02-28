package com.attestry.dpp.domain.model;

/**
 * 소유권 이전 수락 방식을 나타내는 열거형.
 *
 * ONE_TIME_CODE → 1회용 숫자 코드 입력 (택배 거래용)
 * IN_PERSON_QR → 대면 QR 스캔 (매장 거래용)
 */
public enum AcceptMethod {
    ONE_TIME_CODE,
    IN_PERSON_QR
}
