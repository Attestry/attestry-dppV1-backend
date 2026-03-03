package com.attestry.dpp.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "of")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminPendingUserResponse {
    private final String userId;
    private final String email;
    private final String role;
    private final String phone;
    private final String businessNumber;
    private final String brandName;
    private final String status;
}
