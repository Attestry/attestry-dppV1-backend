package com.attestry.dpp.domain.model;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 제품 소유권 투영(Projection) 엔티티.
 * 특정 디지털 여권의 현재 소유자를 나타냅니다.
 * version을 통해 소유권 변경 이력을 추적합니다.
 */
@Entity
@Table(name = "ownership_projections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Ownership implements Persistable<String> {

    @Id
    @Column(name = "passport_id")
    private String passportId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "passport_id")
    private DigitalPassport passport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "user_id", nullable = false)
    private User owner;

    @Column(name = "since_at", nullable = false)
    private LocalDateTime sinceAt;

    @Column(nullable = false)
    private Integer version;

    @Override
    public String getId() {
        return passportId;
    }

    @Override
    public boolean isNew() {
        return this.version == null || this.version == 0;
    }


    /**
     * 최초 소유권을 설정합니다 (자가등록 또는 최초 클레임).
     */
    public static Ownership establish(DigitalPassport passport, User owner) {
        return Ownership.builder()
                .passportId(passport.getId())
                .passport(passport)
                .owner(owner)
                .sinceAt(LocalDateTime.now())
                .version(0)
                .build();
    }

    /**
     * 소유권을 새로운 소유자에게 이전합니다.
     * version이 증가하여 이전 횟수를 추적합니다.
     */
    public void transferTo(User newOwner) {
        this.owner = newOwner;
        this.sinceAt = LocalDateTime.now();
        this.version = this.version + 1;
    }

    /**
     * 특정 사용자가 현재 소유자인지 확인합니다.
     */
    public boolean isOwnedBy(String userId) {
        return this.owner != null && this.owner.getId().equals(userId);
    }
}
