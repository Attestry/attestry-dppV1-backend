package com.attestry.dpp.infrastructure.web.mapper;

import com.attestry.dpp.application.dto.result.PassportMyPassportResult;
import com.attestry.dpp.application.dto.result.PassportPublicViewResult;
import com.attestry.dpp.infrastructure.web.response.PassportMyPassportResponse;
import com.attestry.dpp.infrastructure.web.response.PassportPublicViewResponse;

public final class PassportResponseMapper {

    private PassportResponseMapper() {
    }

    public static PassportMyPassportResponse toPassportMyPassportResponse(PassportMyPassportResult result) {
        return PassportMyPassportResponse.builder()
                .passportId(result.getPassportId())
                .assetId(result.getAssetId())
                .modelName(result.getModelName())
                .serialNumber(result.getSerialNumber())
                .qrPublicCode(result.getQrPublicCode())
                .sinceAt(result.getSinceAt())
                .imageUrl(result.getImageUrl())
                .build();
    }

    public static PassportPublicViewResponse toPassportPublicViewResponse(PassportPublicViewResult result) {
        return PassportPublicViewResponse.builder()
                .passportId(result.getPassportId())
                .qrPublicCode(result.getQrPublicCode())
                .modelName(result.getModelName())
                .modelNumber(result.getModelNumber())
                .color(result.getColor())
                .size(result.getSize())
                .material(result.getMaterial())
                .isGenuine(result.isGenuine())
                .currentOwnerName(result.getCurrentOwnerName())
                .since(result.getSince())
                .imageUrl(result.getImageUrl())
                .ledgerEvents(result.getLedgerEvents() == null ? null :
                        result.getLedgerEvents().stream()
                                .map(PassportResponseMapper::toLedgerEventResponse)
                                .toList())
                .build();
    }

    private static PassportPublicViewResponse.LedgerEvent toLedgerEventResponse(
            PassportPublicViewResult.LedgerEvent result) {
        return PassportPublicViewResponse.LedgerEvent.builder()
                .date(result.getDate())
                .action(result.getAction())
                .hash(result.getHash())
                .actorName(result.getActorName())
                .build();
    }
}
