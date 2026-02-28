package com.attestry.dpp.domain.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자 엔티티.
 * 역할(Role)과 상태(Status) enum을 사용하여 타입 안전성을 보장합니다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 50)
    private String id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "business_number", length = 50)
    private String businessNumber;

    @Column(name = "brand_name", length = 100)
    private String brandName;

    /**
     * 관리자가 계정을 승인합니다. PENDING → ACTIVE
     */
    public void approve() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("PENDING 상태의 계정만 승인할 수 있습니다.");
        }
        this.status = Status.ACTIVE;
    }

    /**
     * 관리자가 계정을 거절합니다. PENDING → REJECTED
     */
    public void reject() {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("PENDING 상태의 계정만 거절할 수 있습니다.");
        }
        this.status = Status.REJECTED;
    }

    /**
     * 계정이 활성 상태인지 확인합니다.
     */
    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }

    public enum Role {
        BRAND, RETAIL, OWNER, PROVIDER, ADMIN
    }

    public enum Status {
        ACTIVE, PENDING, REJECTED
    }
}
