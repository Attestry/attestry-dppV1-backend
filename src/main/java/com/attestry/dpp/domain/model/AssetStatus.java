package com.attestry.dpp.domain.model;

/**
 * 자산(Asset)의 생명주기 상태를 나타내는 열거형.
 *
 * MINTED → 브랜드에 의해 최초 생성됨
 * ACTIVE → 자가등록에 의해 활성화됨
 * RELEASED → 브랜드가 유통처에 출고함
 */
public enum AssetStatus {
    MINTED,
    ACTIVE,
    RELEASED
}
