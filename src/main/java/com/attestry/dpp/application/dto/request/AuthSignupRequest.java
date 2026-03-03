package com.attestry.dpp.application.dto.request;

import com.attestry.dpp.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "password")
public class AuthSignupRequest {
    @NotBlank
    @Email
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "이메일 형식이 올바르지 않습니다."
    )
    private String email;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
            regexp = "^010-\\d{4}-\\d{4}$",
            message = "전화번호 형식은 010-0000-0000 이어야 합니다."
    )
    private String phone;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotNull
    private SignupRole role;

    private String businessNumber;
    private String brandName;

    public enum SignupRole {
        BRAND, RETAIL, OWNER, PROVIDER;

        public User.Role toUserRole() {
            return User.Role.valueOf(this.name());
        }
    }
}
