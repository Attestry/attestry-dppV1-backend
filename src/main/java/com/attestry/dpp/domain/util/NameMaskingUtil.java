package com.attestry.dpp.domain.util;

/**
 * 이메일 기반 이름 마스킹 유틸리티.
 *
 * 개인정보 보호를 위해 공개 API 응답에서 사용자 이름을 마스킹합니다.
 * 예: "retailshop@example.com" → "r*******p"
 */
public final class NameMaskingUtil {

    private NameMaskingUtil() {
        // 인스턴스 생성 방지
    }

    /**
     * 이메일 주소의 로컬 파트(@앞)를 마스킹합니다.
     *
     * <ul>
     *   <li>2자 이하: 첫 글자 + "**"  (예: "ab" → "a**")</li>
     *   <li>3자 이상: 첫 글자 + 중간 마스킹 + 마지막 글자  (예: "retailer" → "r*****r")</li>
     * </ul>
     *
     * @param email 마스킹할 이메일 주소
     * @return 마스킹된 이름 문자열
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "***";
        }
        String localPart = email.split("@")[0];
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "**";
        }
        return localPart.charAt(0)
                + "*".repeat(localPart.length() - 2)
                + localPart.charAt(localPart.length() - 1);
    }
}
