package com.attestry.dpp.domain.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

/**
 * 증빙 사진 URL 파싱 유틸리티.
 *
 * DB에 저장된 evidenceUrls 문자열 (예: "[\"url1\", \"url2\"]")을
 * Jackson ObjectMapper를 사용해 안전하게 파싱합니다.
 *
 * 기존의 단순 문자열 split 방식은 URL에 쉼표가 포함된 경우
 * (예: query parameter) 잘못 파싱되는 문제가 있었습니다.
 */
public final class EvidenceUrlParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private EvidenceUrlParser() {
        // 인스턴스 생성 방지
    }

    /**
     * JSON 배열 형식의 문자열에서 URL 목록을 추출합니다.
     *
     * @param raw DB에 저장된 원본 문자열 (예: "[\"url1\", \"url2\"]")
     * @return URL 문자열 리스트. null/빈 입력 또는 파싱 실패 시 빈 리스트 반환
     */
    public static List<String> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }

        String trimmed = raw.trim();

        // JSON 배열 형식 파싱 — URL 내 쉼표도 안전하게 처리
        if (trimmed.startsWith("[")) {
            try {
                return OBJECT_MAPPER.readValue(trimmed, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                // JSON 파싱 실패 시 빈 리스트 반환
                return Collections.emptyList();
            }
        }

        // 단일 URL 문자열인 경우
        return List.of(trimmed);
    }

    /**
     * 첫 번째 증빙 사진 URL을 반환합니다.
     * URL이 없으면 null을 반환합니다.
     *
     * @param raw DB에 저장된 원본 문자열
     * @return 첫 번째 URL 또는 null
     */
    public static String firstOrNull(String raw) {
        List<String> urls = parse(raw);
        return urls.isEmpty() ? null : urls.get(0);
    }
}
