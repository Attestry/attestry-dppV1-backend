package com.attestry.dpp.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 물리적 제품에 대한 디지털 여권 엔티티.
 * 제품과 1:1로 매핑되며, 고유한 QR 공개 코드를 가집니다.
 */
@Entity
@Table(name = "digital_passports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DigitalPassport {

    @Id
    @Column(name = "passport_id", length = 36)
    private String id;

    @Column(name = "qr_public_code", unique = true, nullable = false, length = 20)
    private String qrPublicCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", referencedColumnName = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "qr_status", nullable = false, length = 20)
    private QrStatus qrStatus;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    /**
     * 새로운 디지털 여권을 생성합니다.
     * UUID 기반의 고유 ID와 QR 공개 코드를 부여합니다.
     */
    public static DigitalPassport issue(Asset asset) {
        return DigitalPassport.builder()
                .id("P-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .asset(asset)
                .qrPublicCode("QR" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .qrStatus(QrStatus.ACTIVE)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    /**
     * QR 코드를 무효화합니다.
     */
    public void revokeQr() {
        this.qrStatus = QrStatus.REVOKED;
    }

    /**
     * QR 코드가 유효한지 확인합니다.
     */
    public boolean isQrActive() {
        return this.qrStatus == QrStatus.ACTIVE;
    }
}
