package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.request.AuthSignupRequest;
import com.attestry.dpp.application.usecase.command.AuthCommandUseCaseHandler;
import com.attestry.dpp.domain.exception.BadRequestException;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthCommandUseCaseHandlerUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthCommandUseCaseHandler handler;

    @Test
    @DisplayName("signup: RETAIL/BRAND는 사업자 번호가 없으면 실패한다")
    void signup_requiresBusinessNumberForRetailAndBrand() {
        AuthSignupRequest request = new AuthSignupRequest();
        request.setEmail("retail@test.com");
        request.setPassword("Password1");
        request.setRole(AuthSignupRequest.SignupRole.RETAIL);
        when(userRepository.findByEmail("retail@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.signup(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("사업자 등록번호");
    }

    @Test
    @DisplayName("signup: BRAND는 브랜드명이 없으면 실패한다")
    void signup_requiresBrandNameForBrand() {
        AuthSignupRequest request = new AuthSignupRequest();
        request.setEmail("brand@test.com");
        request.setPassword("Password1");
        request.setRole(AuthSignupRequest.SignupRole.BRAND);
        request.setBusinessNumber("123-45-67890");
        when(userRepository.findByEmail("brand@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.signup(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("브랜드명");
    }
}

