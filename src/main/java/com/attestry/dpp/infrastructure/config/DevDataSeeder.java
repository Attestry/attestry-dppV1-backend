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
 * 애플리케이션 시작 시 로컬 로그인 확인용 ADMIN 계정을 동기화합니다.
 *
 * 동기화 계정:
 * - ADMIN : kimsunwook@naver.com / adminsw00@
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
        createIfAbsent("U_ADMIN", "kimsunwook@admin.com", "010-1111-1111",
                "adminsw00@", User.Role.ADMIN, User.Status.ACTIVE);


        }

        private void upsert(String id, String email, String phone,
                        String rawPassword, User.Role role, User.Status status) {
                userRepository.save(User.builder()
                                .id(id)
                                .email(email)
                                .phone(phone)
                                .password(passwordEncoder.encode(rawPassword))
                                .role(role)
                                .status(status)
                                .build());
        }
}
