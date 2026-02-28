package com.attestry.dpp.application.dto.request;

import com.attestry.dpp.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String email;

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
