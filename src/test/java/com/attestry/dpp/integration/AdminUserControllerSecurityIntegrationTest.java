package com.attestry.dpp.integration;

import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminUserControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("ADMIN은 대기 사용자 목록을 페이징/정렬 조건으로 조회할 수 있다")
    @WithMockUser(roles = "ADMIN")
    void getPendingUsers_admin_canAccess() throws Exception {
        userRepository.save(User.builder()
                .id("U1")
                .email("pending@test.com")
                .password("encoded-password")
                .role(User.Role.BRAND)
                .status(User.Status.PENDING)
                .phone(null)
                .businessNumber(null)
                .brandName("Pending Brand")
                .build());
        userRepository.save(User.builder()
                .id("U2")
                .email("active@test.com")
                .password("encoded-password")
                .role(User.Role.OWNER)
                .status(User.Status.ACTIVE)
                .phone("010-1234-5678")
                .build());

        mockMvc.perform(get("/api/v1/admin/users/pending")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].userId").value("U1"))
                .andExpect(jsonPath("$.data.content[0].phone").doesNotExist())
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    @DisplayName("ADMIN이 아닌 사용자는 대기 사용자 목록 조회 시 403을 받는다")
    @WithMockUser(roles = "OWNER")
    void getPendingUsers_nonAdmin_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/pending"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
