package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.BrandMintRequest;
import com.attestry.dpp.application.dto.request.BrandReleaseRequest;
import com.attestry.dpp.application.dto.result.BrandMintResult;

public interface BrandCommandUseCase {
    BrandMintResult mintAsset(BrandMintRequest request, String brandId);
    void releaseAsset(BrandReleaseRequest request, String brandId);
}
