package com.attestry.dpp.infrastructure.config;

import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개발 환경 초기 데이터 시더.
 *
 * 애플리케이션 시작 시 테스트에 필요한 최소한의 계정을 생성합니다.
 * 이미 존재하는 ID는 중복 생성하지 않습니다.
 *
 * 생성 계정 목록:
 * - ADMIN   : kimsunwook@naver.com / adminsw00@
 * - BRAND   : brand@test.com       / brand123!   (ACTIVE, 관리자 사전 승인 상태)
 * - OWNER   : owner@test.com       / owner123!
 * - PROVIDER: provider@test.com    / provider123!
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createIfAbsent("U_ADMIN", "kimsunwook@naver.com", "010-1111-1111",
                "adminsw00@", User.Role.ADMIN, User.Status.ACTIVE);

        createIfAbsent("U_BRAND_01", "brand@test.com", "010-2222-2222",
                "brand123!", User.Role.BRAND, User.Status.ACTIVE);

        createIfAbsent("U_OWNER_01", "owner@test.com", "010-3333-3333",
                "owner123!", User.Role.OWNER, User.Status.ACTIVE);

        createIfAbsent("U_PROVIDER_01", "provider@test.com", "010-4444-4444",
                "provider123!", User.Role.PROVIDER, User.Status.ACTIVE);
    }

    private void createIfAbsent(String id, String email, String phone,
                                String rawPassword, User.Role role, User.Status status) {
        userRepository.findById(id).orElseGet(() ->
                userRepository.save(User.builder()
                        .id(id)
                        .email(email)
                        .phone(phone)
                        .password(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .status(status)
                        .build()));
    }
}
