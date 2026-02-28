package com.attestry.dpp.domain.model;

/**
 * 원장(Ledger) 이벤트 액션 유형을 나타내는 열거형.
 *
 * MINTED → 디지털 여권 최초 생성 (제네시스)
 * RELEASED → 브랜드가 유통처로 출고
 * CLAIMED → 소비자가 소유권 최초 등록
 * TRANSFER_COMPLETED → 소유권 이전 완료
 * SERVICE_CONFIRMED → 서비스/수리 확인 완료
 */
public enum LedgerAction {
    MINTED,
    RELEASED,
    CLAIMED,
    TRANSFER_COMPLETED,
    SERVICE_CONFIRMED
}
