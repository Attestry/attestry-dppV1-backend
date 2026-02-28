package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.AuthLoginRequest;
import com.attestry.dpp.application.dto.request.AuthSignupRequest;
import com.attestry.dpp.application.dto.result.AuthLoginResult;
import com.attestry.dpp.application.dto.result.AuthRoleResult;
import com.attestry.dpp.application.dto.result.AuthSignupResult;
import com.attestry.dpp.application.dto.result.AuthStatusResult;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.domain.exception.*;
import com.attestry.dpp.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthCommandUseCaseHandler implements AuthCommandUseCase {

    private static final int MAX_LOGIN_ATTEMPTS = 10;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;

    // 분산 환경에서는 Redis로 교체할 것
    private final ConcurrentHashMap<String, LoginAttemptRecord> loginAttempts = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthSignupResult signup(AuthSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        User.Role role = request.getRole().toUserRole();
        validateRoleSpecificSignupFields(request, role);
        User.Status initialStatus = resolveInitialStatus(role);

        User user = User.builder()
                .id("U_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .status(initialStatus)
                .businessNumber(request.getBusinessNumber())
                .brandName(request.getBrandName())
                .build();

        userRepository.save(user);

        return AuthSignupResult.of(
                user.getId(),
                user.getEmail(),
                toAuthRoleResult(user.getRole()),
                toAuthStatusResult(user.getStatus()));
    }

    public AuthLoginResult login(AuthLoginRequest request) {
        String email = request.getEmail();

        checkLoginLock(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    recordFailedAttempt(email);
                    return new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordFailedAttempt(email);
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getStatus() == User.Status.PENDING) {
            throw new UnauthorizedException("계정이 아직 관리자 승인 대기 중입니다. 승인 후 로그인할 수 있습니다.");
        }

        if (user.getStatus() == User.Status.REJECTED) {
            throw new UnauthorizedException("계정 가입이 거절되었습니다. 관리자에게 문의하세요.");
        }

        // 로그인 성공 시 실패 기록 초기화
        loginAttempts.remove(email);

        return AuthLoginResult.of(
                user.getId(),
                user.getEmail(),
                toAuthRoleResult(user.getRole()),
                jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name()));
    }


    private void checkLoginLock(String email) {
        LoginAttemptRecord record = loginAttempts.get(email);
        if (record == null) {
            return;
        }
        if (record.isLocked()) {
            long remainingSeconds = (record.lockedUntil - Instant.now().toEpochMilli()) / 1000;
            throw new UnauthorizedException(
                    String.format("로그인 시도 횟수 초과로 계정이 잠겼습니다. %d초 후 다시 시도해 주세요.", remainingSeconds));
        }
        // 잠금 기간이 지났으면 기록 초기화
        loginAttempts.remove(email);
    }

    private void recordFailedAttempt(String email) {
        loginAttempts.compute(email, (key, existing) -> {
            LoginAttemptRecord record = (existing != null) ? existing : new LoginAttemptRecord();
            record.failCount++;
            if (record.failCount >= MAX_LOGIN_ATTEMPTS) {
                record.lockedUntil = Instant.now().toEpochMilli() + LOCK_DURATION_MS;
            }
            return record;
        });
    }

    private User.Status resolveInitialStatus(User.Role role) {
        return switch (role) {
            case BRAND, RETAIL -> User.Status.PENDING;
            default -> User.Status.ACTIVE;
        };
    }

    private void validateRoleSpecificSignupFields(AuthSignupRequest request, User.Role role) {
        if ((role == User.Role.BRAND || role == User.Role.RETAIL) && isBlank(request.getBusinessNumber())) {
            throw new BadRequestException("브랜드/유통 계정은 사업자 등록번호가 필요합니다.");
        }
        if (role == User.Role.BRAND && isBlank(request.getBrandName())) {
            throw new BadRequestException("브랜드 계정은 브랜드명이 필요합니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private AuthRoleResult toAuthRoleResult(User.Role role) {
        return switch (role) {
            case BRAND -> AuthRoleResult.BRAND;
            case RETAIL -> AuthRoleResult.RETAIL;
            case OWNER -> AuthRoleResult.OWNER;
            case PROVIDER -> AuthRoleResult.PROVIDER;
            case ADMIN -> AuthRoleResult.ADMIN;
        };
    }

    private AuthStatusResult toAuthStatusResult(User.Status status) {
        return switch (status) {
            case PENDING -> AuthStatusResult.PENDING;
            case ACTIVE -> AuthStatusResult.ACTIVE;
            case REJECTED -> AuthStatusResult.REJECTED;
        };
    }

    private static class LoginAttemptRecord {
        int  failCount  = 0;
        long lockedUntil = 0;

        boolean isLocked() {
            return lockedUntil > Instant.now().toEpochMilli();
        }
    }
}
