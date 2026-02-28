package com.attestry.dpp.domain.util;

import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.DigitalPassport;

/**
 * 제네시스 해시 생성 유틸리티.
 *
 * 제품이 최초 등록(민팅)될 때 원장 첫 번째 항목에 포함되는 제네시스 해시를 생성합니다.
 * BrandCommandUseCaseHandler(브랜드 직접 민팅)와 RegistrationCommandUseCaseHandler(자가등록 승인) 양쪽에서 동일한 로직을 공유합니다.
 *
 * 해시 입력 포맷: assetId | serialNumber | modelName | mintedById | passportId | qrPublicCode | issuedAt
 */
public final class GenesisHashBuilder {

    private GenesisHashBuilder() {
        // 인스턴스 생성 방지
    }

    /**
     * 자산과 디지털 여권 정보를 바탕으로 제네시스 해시를 생성합니다.
     *
     * @param asset    대상 자산 (ID, 시리얼번호, 모델명, 발행자 포함)
     * @param passport 발행된 디지털 여권 (ID, QR코드, 발행일 포함)
     * @return 64자 SHA-256 해시 문자열
     */
    public static String build(Asset asset, DigitalPassport passport) {
        String mintedById = HashUtil.nullSafe(
                asset.getMintedBy() != null ? asset.getMintedBy().getId() : null);

        String hashInput = String.join("|",
                asset.getId(),
                asset.getSerialNumber(),
                asset.getModelName(),
                mintedById,
                passport.getId(),
                passport.getQrPublicCode(),
                passport.getIssuedAt().toString());

        return HashUtil.sha256(hashInput);
    }

    /**
     * 제네시스 해시를 dataJson 포맷으로 변환합니다.
     *
     * @param asset    대상 자산
     * @param passport 발행된 디지털 여권
     * @return {@code {"genesisHash": "..."}} 형식의 JSON 문자열
     */
    public static String buildDataJson(Asset asset, DigitalPassport passport) {
        return String.format("{\"genesisHash\": \"%s\"}", build(asset, passport));
    }
}
