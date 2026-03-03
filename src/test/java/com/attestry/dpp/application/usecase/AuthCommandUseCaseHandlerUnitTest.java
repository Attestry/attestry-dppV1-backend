package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.request.AuthSignupRequest;
import com.attestry.dpp.application.usecase.command.AuthCommandUseCaseHandler;
import com.attestry.dpp.domain.exception.BadRequestException;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

    @Test
    @DisplayName("signup: OWNER 가입 시 businessNumber/brandName 빈문자열은 null로 저장된다")
    void signup_ownerStoresNullForOptionalFields() {
        AuthSignupRequest request = new AuthSignupRequest();
        request.setEmail("owner@test.com");
        request.setPassword("Password1");
        request.setRole(AuthSignupRequest.SignupRole.OWNER);
        request.setBusinessNumber("  ");
        request.setBrandName("");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getBusinessNumber()).isNull();
        assertThat(saved.getBrandName()).isNull();
    }

    @Test
    @DisplayName("signup: RETAIL 가입 시 brandName 빈문자열은 null로 저장되고 businessNumber는 유지된다")
    void signup_retailStoresNullBrandNameAndKeepsBusinessNumber() {
        AuthSignupRequest request = new AuthSignupRequest();
        request.setEmail("retail2@test.com");
        request.setPassword("Password1");
        request.setRole(AuthSignupRequest.SignupRole.RETAIL);
        request.setBusinessNumber("123-45-67890");
        request.setBrandName("  ");
        when(userRepository.findByEmail("retail2@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        handler.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getBusinessNumber()).isEqualTo("123-45-67890");
        assertThat(saved.getBrandName()).isNull();
    }
}
