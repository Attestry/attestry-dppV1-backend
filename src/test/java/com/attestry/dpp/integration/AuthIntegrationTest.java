package com.attestry.dpp.integration;

import com.attestry.dpp.application.dto.request.AuthLoginRequest;
import com.attestry.dpp.application.dto.request.AuthSignupRequest;
import com.attestry.dpp.application.dto.result.AuthLoginResult;
import com.attestry.dpp.application.dto.result.AuthRoleResult;
import com.attestry.dpp.application.dto.result.AuthSignupResult;
import com.attestry.dpp.application.dto.result.AuthStatusResult;
import com.attestry.dpp.application.usecase.command.AuthCommandUseCaseHandler;
import com.attestry.dpp.domain.exception.ConflictException;
import com.attestry.dpp.domain.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthCommandUseCaseHandler 통합 테스트.
 *
 * H2 인메모리 DB를 사용하여 실제 DB 레이어까지 포함한 E2E 검증을 수행합니다.
 * @ActiveProfiles("test")로 application-test.yml 설정을 사용합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthCommandUseCaseHandler 통합 테스트")
class AuthIntegrationTest {

    @Autowired
    private AuthCommandUseCaseHandler authService;

    private AuthSignupRequest ownerSignupRequest;

    @BeforeEach
    void setUp() {
        ownerSignupRequest = new AuthSignupRequest();
        ownerSignupRequest.setEmail("owner@test.com");
        ownerSignupRequest.setPassword("password123!");
        ownerSignupRequest.setPhone("010-1234-5678");
        ownerSignupRequest.setRole(AuthSignupRequest.SignupRole.OWNER);
    }

    @Nested
    @DisplayName("회원가입 (signup)")
    class SignupTest {

        @Test
        @DisplayName("OWNER 역할로 회원가입 시 즉시 ACTIVE 상태로 생성됩니다")
        void signup_ownerRole_createsActiveUser() {
            AuthSignupResult response = authService.signup(ownerSignupRequest);

            assertThat(response.getUserId()).isNotNull();
            assertThat(response.getEmail()).isEqualTo("owner@test.com");
            assertThat(response.getRole()).isEqualTo(AuthRoleResult.OWNER);
            assertThat(response.getStatus()).isEqualTo(AuthStatusResult.ACTIVE);
        }

        @Test
        @DisplayName("BRAND 역할로 회원가입 시 PENDING 상태로 생성됩니다")
        void signup_brandRole_createsPendingUser() {
            ownerSignupRequest.setRole(AuthSignupRequest.SignupRole.BRAND);
            ownerSignupRequest.setBusinessNumber("123-45-67890");
            ownerSignupRequest.setBrandName("Test Brand");

            AuthSignupResult response = authService.signup(ownerSignupRequest);

            assertThat(response.getStatus()).isEqualTo(AuthStatusResult.PENDING);
        }

        @Test
        @DisplayName("동일 이메일로 중복 가입 시 ConflictException이 발생합니다")
        void signup_duplicateEmail_throwsConflictException() {
            authService.signup(ownerSignupRequest);

            AuthSignupRequest duplicateRequest = new AuthSignupRequest();
            duplicateRequest.setEmail("owner@test.com");
            duplicateRequest.setPassword("other123!");
            duplicateRequest.setPhone("010-9999-9999");
            duplicateRequest.setRole(AuthSignupRequest.SignupRole.OWNER);

            assertThatThrownBy(() -> authService.signup(duplicateRequest))
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    @DisplayName("로그인 (login)")
    class LoginTest {

        @BeforeEach
        void registerUser() {
            authService.signup(ownerSignupRequest);
        }

        @Test
        @DisplayName("올바른 이메일/비밀번호로 로그인 시 JWT 토큰이 반환됩니다")
        void login_validCredentials_returnsAccessToken() {
            AuthLoginRequest loginRequest = new AuthLoginRequest();
            loginRequest.setEmail("owner@test.com");
            loginRequest.setPassword("password123!");

            AuthLoginResult response = authService.login(loginRequest);

            assertThat(response.getAccessToken()).isNotBlank();
            assertThat(response.getUserId()).isNotNull();
            assertThat(response.getRole()).isEqualTo(AuthRoleResult.OWNER);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 UnauthorizedException이 발생합니다")
        void login_unknownEmail_throwsUnauthorizedException() {
            AuthLoginRequest loginRequest = new AuthLoginRequest();
            loginRequest.setEmail("unknown@test.com");
            loginRequest.setPassword("password123!");

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 UnauthorizedException이 발생합니다")
        void login_wrongPassword_throwsUnauthorizedException() {
            AuthLoginRequest loginRequest = new AuthLoginRequest();
            loginRequest.setEmail("owner@test.com");
            loginRequest.setPassword("wrongpassword!");

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("PENDING 상태 계정으로 로그인 시 UnauthorizedException이 발생합니다")
        void login_pendingUser_throwsUnauthorizedException() {
            // BRAND 계정은 PENDING 상태로 생성
            AuthSignupRequest brandRequest = new AuthSignupRequest();
            brandRequest.setEmail("brand@test.com");
            brandRequest.setPassword("brand123!");
            brandRequest.setPhone("010-1111-2222");
            brandRequest.setRole(AuthSignupRequest.SignupRole.BRAND);
            brandRequest.setBusinessNumber("123-45-67890");
            brandRequest.setBrandName("Test Brand");
            authService.signup(brandRequest);

            AuthLoginRequest loginRequest = new AuthLoginRequest();
            loginRequest.setEmail("brand@test.com");
            loginRequest.setPassword("brand123!");

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("승인");
        }
    }
}
