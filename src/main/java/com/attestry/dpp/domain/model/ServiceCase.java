package com.attestry.dpp.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 서비스 케이스(수리/인증 등) 엔티티.
 *
 * 서비스 제공자가 제품에 대해 수리 또는 인증 서비스를 수행한 기록입니다.
 * 소유자가 승인하면 원장에 SERVICE_CONFIRMED 이벤트가 기록됩니다.
 */
@Entity
@Table(name = "service_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ServiceCase {

    @Id
    @Column(name = "case_id", length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", referencedColumnName = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", referencedColumnName = "user_id", nullable = false)
    private User provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", referencedColumnName = "user_id")
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceKind kind;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceState state;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * 서비스 제공자가 새로운 서비스 케이스를 접수합니다.
     *
     * @param asset    대상 자산
     * @param provider 서비스 제공자
     * @param kind     서비스 종류 (REPAIR, AUTHENTICATION)
     */
    public static ServiceCase submit(Asset asset, User provider, ServiceKind kind) {
        return ServiceCase.builder()
                .id("SC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .asset(asset)
                .provider(provider)
                .kind(kind)
                .state(ServiceState.REQUESTED)
                .submittedAt(LocalDateTime.now())
                .build();
    }


    /**
     * 소유자가 서비스 완료를 승인합니다.
     */
    public void approve(User approver) {
        if (this.state != ServiceState.COMPLETED) {
            throw new IllegalStateException("COMPLETED 상태에서만 승인할 수 있습니다. 현재 상태: " + this.state);
        }
        this.state = ServiceState.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 소유자가 서비스를 거절합니다.
     */
    public void reject(User approver) {
        if (this.state != ServiceState.COMPLETED) {
            throw new IllegalStateException("COMPLETED 상태에서만 거절할 수 있습니다. 현재 상태: " + this.state);
        }
        this.state = ServiceState.REJECTED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * 서비스 수행을 완료 처리합니다.
     */
    public void complete() {
        if (this.state != ServiceState.REQUESTED) {
            throw new IllegalStateException("REQUESTED 상태에서만 완료 처리할 수 있습니다. 현재 상태: " + this.state);
        }
        this.state = ServiceState.COMPLETED;
    }
}
