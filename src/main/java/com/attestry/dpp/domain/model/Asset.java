package com.attestry.dpp.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * 물리적 제품을 나타내는 도메인 엔티티.
 * 브랜드에 의해 민팅(생성)되며, 디지털 여권과 1:1로 연결됩니다.
 */
@Entity
@Table(name = "assets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Asset {

    @Id
    @Column(name = "asset_id", length = 36)
    private String id;

    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "minted_by", referencedColumnName = "user_id")
    private User mintedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status;

    /**
     * 브랜드 민팅 시 새로운 Asset을 생성합니다.
     */
    public static Asset mint(String modelName, String serialNumber, User brand) {
        return Asset.builder()
                .id("ASSET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .modelName(modelName)
                .serialNumber(serialNumber)
                .mintedBy(brand)
                .status(AssetStatus.MINTED)
                .build();
    }

    /**
     * 자가등록 승인 시 새로운 Asset을 생성합니다.
     */
    public static Asset createForRegistration(String modelName, String serialNumber, User approvedBy) {
        return Asset.builder()
                .id("ASSET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .modelName(modelName)
                .serialNumber(serialNumber)
                .mintedBy(approvedBy)
                .status(AssetStatus.ACTIVE)
                .build();
    }

    /**
     * 브랜드가 유통처에 출고합니다.
     * MINTED 상태에서만 RELEASED로 전환 가능합니다.
     */
    public void release() {
        if (this.status != AssetStatus.MINTED) {
            throw new IllegalStateException(
                    "MINTED 상태의 제품만 출고할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = AssetStatus.RELEASED;
    }
}
