package com.attestry.dpp.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 소유권 이전 토큰 엔티티.
 * 소유자가 이전을 발의하면 생성되고, 수신자가 수락하면 완료됩니다.
 * 15분 만료 시간, 최대 5회 시도 제한이 적용됩니다.
 */
@Entity
@Table(name = "transfer_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TransferToken {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int EXPIRY_MINUTES = 15;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "failed_attempts", columnDefinition = "integer default 0")
    @Builder.Default
    private Integer failedAttempts = 0;

    @Id
    @Column(name = "transfer_id", length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passport_id", referencedColumnName = "passport_id", nullable = false)
    private DigitalPassport passport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", referencedColumnName = "user_id", nullable = true)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", referencedColumnName = "user_id")
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransferState state;

    @Enumerated(EnumType.STRING)
    @Column(name = "accept_method", nullable = false, length = 30)
    private AcceptMethod acceptMethod;

    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "receipt_number", length = 50)
    private String receiptNumber;

    @Column(name = "evidence_urls", columnDefinition = "TEXT")
    private String evidenceUrls;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;


    /**
     * 새로운 이전 토큰을 생성합니다.
     */
    public static TransferToken create(
            DigitalPassport passport,
            User fromUser,
            AcceptMethod method,
            String receiptNumber,
            String evidenceUrls) {

        String token = "tr_" + UUID.randomUUID().toString().substring(0, 8);
        String code = (method == AcceptMethod.ONE_TIME_CODE)
                ? UUID.randomUUID().toString().substring(0, 6).toUpperCase()
                : null;

        return TransferToken.builder()
                .id(token)
                .passport(passport)
                .fromUser(fromUser)
                .state(TransferState.INITIATED)
                .acceptMethod(method)
                .code(code)
                .receiptNumber(receiptNumber)
                .evidenceUrls(evidenceUrls)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .build();
    }


    /**
     * 토큰이 현재 수락 가능한 상태인지 검증합니다.
     *
     * @throws IllegalStateException 만료, 잠금, 또는 비활성 상태인 경우
     */
    public void validateAcceptable() {
        if (this.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            throw new IllegalStateException("시도 횟수 초과로 이전이 잠금되었습니다.");
        }
        if (this.state != TransferState.INITIATED) {
            recordFailedAttempt();
            throw new IllegalStateException("이전 토큰이 유효하지 않은 상태입니다: " + this.state);
        }
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            recordFailedAttempt();
            throw new IllegalStateException("이전 토큰이 만료되었습니다.");
        }
    }

    /**
     * 수신자가 이전을 수락합니다.
     */
    public void accept(User toUser) {
        validateAcceptable();
        this.state = TransferState.COMPLETED;
        this.toUser = toUser;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * 발신자가 이전을 취소합니다.
     *
     * @throws IllegalStateException INITIATED 상태가 아닌 경우
     */
    public void cancel() {
        if (this.state != TransferState.INITIATED) {
            throw new IllegalStateException(
                    "INITIATED 상태의 이전만 취소할 수 있습니다. 현재 상태: " + this.state);
        }
        this.state = TransferState.CANCELLED;
    }

    /**
     * 실패 시도를 기록합니다.
     */
    private void recordFailedAttempt() {
        this.failedAttempts = this.failedAttempts + 1;
    }

    /**
     * 발신자가 없는 경우(최초 클레임)인지 확인합니다.
     */
    public boolean isFirstClaim() {
        return this.fromUser == null;
    }
}
