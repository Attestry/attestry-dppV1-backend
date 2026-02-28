package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.PassportMyPassportResult;
import com.attestry.dpp.application.dto.result.PassportPublicViewResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PassportQueryUseCase {
    PassportPublicViewResult getPublicPassport(String qrPublicCode);
    Page<PassportMyPassportResult> getMyPassports(String userId, Pageable pageable);
}
