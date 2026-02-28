package com.attestry.dpp.infrastructure.web.mapper;

import com.attestry.dpp.application.dto.result.BrandMintResult;
import com.attestry.dpp.infrastructure.web.response.BrandMintResponse;

public final class BrandResponseMapper {

    private BrandResponseMapper() {
    }

    public static BrandMintResponse toBrandMintResponse(BrandMintResult result) {
        return BrandMintResponse.from(result.getQrCode());
    }
}
