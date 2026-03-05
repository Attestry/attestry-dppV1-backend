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
 * 애플리케이션 시작 시 관리자 계정을 2명으로 고정/동기화합니다.
 *
 * 생성 계정 목록:
 * - ADMIN   : kimsunwook@admin.com  / adminsw00@
 * - ADMIN   : kimminyoung@admin.com / adminmy96@
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
        upsertAdmin("U_ADMIN", "kimsunwook@admin.com", "010-1111-1111",
                "adminsw00@", User.Role.ADMIN, User.Status.ACTIVE);
        upsertAdmin("U_ADMIN_02", "kimminyoung@admin.com", "010-1111-1112",
                "adminmy96@", User.Role.ADMIN, User.Status.ACTIVE);
    }

    private void upsertAdmin(String id, String email, String phone,
                             String rawPassword, User.Role role, User.Status status) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            userRepository.save(User.builder()
                    .id(id)
                    .email(email)
                    .phone(phone)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .status(status)
                    .build());
            return;
        }

        User synced = User.builder()
                .id(existing.getId())
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(status)

                .businessNumber(existing.getBusinessNumber())
                .brandName(existing.getBrandName())
                .build();
        userRepository.save(synced);

    }
}
