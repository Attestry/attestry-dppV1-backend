package com.attestry.dpp.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 소유권 자가등록 요청 엔티티.
 *
 * 소비자가 제품의 소유권을 등록하기 위해 제출하는 요청입니다.
 * 관리자가 승인하면 디지털 여권이 생성되고, 소유권이 확정됩니다.
 */
@Entity
@Table(name = "registration_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RegistrationRequest {

    @Id
    private String requestId;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private String serialNumber;

    @Column(columnDefinition = "TEXT")
    private String evidenceUrls;

    @Column(nullable = false)
    private String requesterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;


    /**
     * 소비자가 새로운 자가등록 요청을 생성합니다.
     * 초기 상태는 PENDING(관리자 승인 대기)입니다.
     *
     * @param modelName    제품 모델명 (예: "구찌 마몽 백")
     * @param serialNumber 제품 시리얼 번호 (예: "GC-2024-001")
     * @param evidenceUrls 증빙 사진 URL (JSON 배열 문자열)
     * @param requesterId  요청자 ID (JWT에서 추출)
     */
    public static RegistrationRequest submit(
            String modelName, String serialNumber,
            String evidenceUrls, String requesterId) {
        return RegistrationRequest.builder()
                .requestId("REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .modelName(modelName)
                .serialNumber(serialNumber)
                .evidenceUrls(evidenceUrls)
                .requesterId(requesterId)
                .status(RegistrationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 관리자가 요청을 승인합니다.
     * PENDING 상태에서만 승인 가능합니다.
     *
     * @throws IllegalStateException 이미 처리된 요청인 경우
     */
    public void approve() {
        if (this.status != RegistrationStatus.PENDING) {
            throw new IllegalStateException(
                    "이미 처리된 요청입니다. 현재 상태: " + this.status);
        }
        this.status = RegistrationStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 관리자가 요청을 거절합니다.
     */
    public void reject() {
        if (this.status != RegistrationStatus.PENDING) {
            throw new IllegalStateException(
                    "이미 처리된 요청입니다. 현재 상태: " + this.status);
        }
        this.status = RegistrationStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 요청이 대기 중인지 확인합니다.
     */
    public boolean isPending() {
        return this.status == RegistrationStatus.PENDING;
    }
}
