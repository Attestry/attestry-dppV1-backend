package com.attestry.dpp.domain.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * QR 스캔 문자열에서 공개 여권 코드(qrPublicCode)를 추출하는 유틸리티.
 * 지원 입력:
 * - "QR1A2B3C" (코드 단독)
 * - "https://host/p/QR1A2B3C" (공개 페이지 URL)
 * - "/p/QR1A2B3C?utm=..." (상대 경로)
 */
public final class QrPayloadParser {

    private QrPayloadParser() {
    }

    public static String extractPublicPassportCode(String rawPayload) {
        String decoded = decode(rawPayload).trim();
        if (decoded.isEmpty()) {
            return decoded;
        }

        String candidate = decoded;
        int routeIndex = decoded.indexOf("/p/");
        if (routeIndex >= 0) {
            candidate = decoded.substring(routeIndex + 3);
        }

        int queryIndex = candidate.indexOf('?');
        if (queryIndex >= 0) {
            candidate = candidate.substring(0, queryIndex);
        }

        int fragmentIndex = candidate.indexOf('#');
        if (fragmentIndex >= 0) {
            candidate = candidate.substring(0, fragmentIndex);
        }

        int slashIndex = candidate.indexOf('/');
        if (slashIndex >= 0) {
            candidate = candidate.substring(0, slashIndex);
        }

        return candidate.trim();
    }

    private static String decode(String rawPayload) {
        if (rawPayload == null) {
            return "";
        }
        try {
            return URLDecoder.decode(rawPayload, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return rawPayload;
        }
    }
}
