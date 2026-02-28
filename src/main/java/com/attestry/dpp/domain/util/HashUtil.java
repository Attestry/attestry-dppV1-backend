package com.attestry.dpp.domain.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 해시 유틸리티.
 * 원장(Ledger) 해시 체인에서 사용하는 단일 해시 함수 제공.
 */
public final class HashUtil {

    private HashUtil() {
        // 인스턴스 생성 방지 — 유틸리티 클래스
    }

    /**
     * 입력 문자열의 SHA-256 해시를 16진수 문자열로 반환합니다.
     *
     * @param data 해시할 원본 문자열
     * @return 64자 길이의 16진수 해시 문자열
     * @throws IllegalStateException SHA-256 알고리즘을 사용할 수 없는 경우
     */
    public static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder(64);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * null-safe 문자열 변환. null이면 "null" 문자열을 반환합니다.
     * 해시 입력 조합 시 일관된 null 처리를 보장합니다.
     */
    public static String nullSafe(Object value) {
        return value != null ? value.toString() : "null";
    }
}
