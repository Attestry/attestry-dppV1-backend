package com.attestry.dpp.integration;

import com.attestry.dpp.domain.model.AcceptMethod;
import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.AssetStatus;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.QrStatus;
import com.attestry.dpp.domain.model.RegistrationRequest;
import com.attestry.dpp.domain.model.RegistrationStatus;
import com.attestry.dpp.domain.model.TransferState;
import com.attestry.dpp.domain.model.TransferToken;
import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.AssetRepository;
import com.attestry.dpp.domain.repository.PassportRepository;
import com.attestry.dpp.domain.repository.RegistrationRepository;
import com.attestry.dpp.domain.repository.UserRepository;
import com.attestry.dpp.infrastructure.persistence.TransferRepository;
import com.attestry.dpp.infrastructure.security.JwtUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegistrationAndTransferFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private PassportRepository passportRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Test
    @DisplayName("OWNER는 /registrations/my 페이지 조회 시 자신의 요청만 페이지 형태로 본다")
    void ownerCanReadMyRegistrationsWithPagination() throws Exception {
        registrationRepository.save(registration("REQ-1", "U_OWNER", RegistrationStatus.PENDING, LocalDateTime.of(2026, 3, 1, 9, 0)));
        registrationRepository.save(registration("REQ-2", "U_OWNER", RegistrationStatus.APPROVED, LocalDateTime.of(2026, 3, 1, 10, 0)));
        registrationRepository.save(registration("REQ-3", "U_OWNER", RegistrationStatus.REJECTED, LocalDateTime.of(2026, 3, 1, 11, 0)));
        registrationRepository.save(registration("REQ-4", "U_OTHER", RegistrationStatus.PENDING, LocalDateTime.of(2026, 3, 1, 12, 0)));

        mockMvc.perform(get("/api/v1/registrations/my")
                        .param("page", "0")
                        .param("size", "2")
                        .with(auth("U_OWNER", "owner@test.com", "OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(2));
    }

    @Test
    @DisplayName("ADMIN은 /registrations/list에서 PENDING 요청을 페이지로 조회한다")
    void adminCanReadPendingRegistrationsWithPagination() throws Exception {
        registrationRepository.save(registration("REQ-10", "U_OWNER", RegistrationStatus.PENDING, LocalDateTime.of(2026, 3, 2, 9, 0)));
        registrationRepository.save(registration("REQ-11", "U_OWNER", RegistrationStatus.PENDING, LocalDateTime.of(2026, 3, 2, 10, 0)));
        registrationRepository.save(registration("REQ-12", "U_OWNER", RegistrationStatus.APPROVED, LocalDateTime.of(2026, 3, 2, 11, 0)));

        mockMvc.perform(get("/api/v1/registrations/list")
                        .param("page", "0")
                        .param("size", "1")
                        .with(auth("U_ADMIN", "admin@test.com", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.page.totalElements").value(2))
                .andExpect(jsonPath("$.data.page.totalPages").value(2));
    }

    @Test
    @DisplayName("transfer 취소는 발신 OWNER 본인만 성공하고 타 OWNER는 실패한다")
    void transferCancelPermissionIsEnforced() throws Exception {
        User owner = userRepository.save(user("U_OWNER", "owner@test.com", User.Role.OWNER));
        User otherOwner = userRepository.save(user("U_OTHER", "other@test.com", User.Role.OWNER));
        Asset asset = assetRepository.save(Asset.builder()
                .id("ASSET-C1")
                .modelName("Model C")
                .serialNumber("SN-C1")
                .mintedBy(owner)
                .status(AssetStatus.ACTIVE)
                .build());
        DigitalPassport passport = passportRepository.save(DigitalPassport.builder()
                .id("P-C1")
                .qrPublicCode("QR-C1")
                .asset(asset)
                .qrStatus(QrStatus.ACTIVE)
                .issuedAt(LocalDateTime.now())
                .build());
        transferRepository.save(TransferToken.builder()
                .id("tr_can_1")
                .passport(passport)
                .fromUser(owner)
                .state(TransferState.INITIATED)
                .acceptMethod(AcceptMethod.ONE_TIME_CODE)
                .code("ABCD12")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .failedAttempts(0)
                .build());

        mockMvc.perform(post("/api/v1/transfers/cancel/tr_can_1")
                        .with(auth("U_OTHER", "other@test.com", "OWNER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        assertThat(transferRepository.findById("tr_can_1").orElseThrow().getState()).isEqualTo(TransferState.INITIATED);

        mockMvc.perform(post("/api/v1/transfers/cancel/tr_can_1")
                        .with(auth("U_OWNER", "owner@test.com", "OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        assertThat(transferRepository.findById("tr_can_1").orElseThrow().getState()).isEqualTo(TransferState.CANCELLED);
        assertThat(otherOwner.getId()).isEqualTo("U_OTHER");
    }

    private static RegistrationRequest registration(String id, String requesterId, RegistrationStatus status, LocalDateTime createdAt) {
        return RegistrationRequest.builder()
                .requestId(id)
                .modelName("Model-" + id)
                .serialNumber("SN-" + id)
                .evidenceUrls("[]")
                .requesterId(requesterId)
                .status(status)
                .createdAt(createdAt)
                .build();
    }

    private static User user(String id, String email, User.Role role) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded-password")
                .role(role)
                .status(User.Status.ACTIVE)
                .build();
    }

    private static RequestPostProcessor auth(
            String userId, String email, String role) {
        JwtUserDetails principal = new JwtUserDetails(userId, email, role);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return SecurityMockMvcRequestPostProcessors.authentication(token);
    }
}
