package com.attestry.dpp.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.attestry.dpp.domain.util.HashUtil;

/**
 * 원장(Ledger) 항목 엔티티.
 * SHA-256 해시 체인으로 연결되어 불변성을 보장합니다.
 * 각 항목은 이전 항목의 해시를 참조하여 체인을 형성합니다.
 */
@Entity
@Table(name = "ledger_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "passport_id", "seq" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LedgerEntry {

    @Id
    @Column(name = "ledger_id", length = 36)
    private String id;

    @Column(nullable = false)
    private Integer seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passport_id", referencedColumnName = "passport_id", nullable = false)
    private DigitalPassport passport;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_action", nullable = false, length = 50)
    private LedgerAction eventAction;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_json", columnDefinition = "jsonb")
    private String dataJson;

    @Column(name = "actor_role", nullable = false, length = 30)
    private String actorRole;

    @Column(name = "actor_id", nullable = false, length = 50)
    private String actorId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "prev_hash", length = 64)
    private String prevHash;

    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    /**
     * 새로운 원장 항목을 생성하고 해시를 계산합니다.
     * 해시 입력 문자열 포맷이 이 메서드에서만 결정되므로 일관성이 보장됩니다.
     */
    public static LedgerEntry create(
            DigitalPassport passport,
            int seq,
            LedgerAction action,
            String actorRole,
            String actorId,
            String dataJson,
            String correlationId,
            String prevHash) {

        String entryId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // 해시 입력 문자열 — 단일 포맷으로 통일, UTC 타임존 고정으로 서버 설정 변경 시에도 해시 일관성 보장
        DateTimeFormatter utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC);
        String hashInput = String.join("|",
                entryId,
                String.valueOf(seq),
                action.name(),
                actorRole,
                actorId,
                HashUtil.nullSafe(dataJson),
                HashUtil.nullSafe(correlationId),
                HashUtil.nullSafe(prevHash),
                now.format(utcFormatter),
                HashUtil.nullSafe(passport != null ? passport.getId() : null));

        return LedgerEntry.builder()
                .id(entryId)
                .seq(seq)
                .passport(passport)
                .eventAction(action)
                .actorRole(actorRole)
                .actorId(actorId)
                .dataJson(dataJson)
                .correlationId(correlationId)
                .prevHash(prevHash)
                .occurredAt(now)
                .hash(HashUtil.sha256(hashInput))
                .build();
    }

    /**
     * 해시 요약(앞 8자)을 반환합니다. UI 표시용.
     */
    public String getHashSummary() {
        return this.hash != null && this.hash.length() >= 8
                ? this.hash.substring(0, 8) + "..."
                : this.hash;
    }
}
