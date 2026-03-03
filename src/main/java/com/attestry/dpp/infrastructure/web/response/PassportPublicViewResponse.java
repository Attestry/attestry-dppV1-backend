package com.attestry.dpp.infrastructure.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PassportPublicViewResponse {
    private final String passportId;

    @JsonProperty("qrPublicCode")
    private final String qrPublicCode;

    private final String modelName;
    private final String modelNumber;
    private final String color;
    private final String size;
    private final String material;
    private final boolean isGenuine;
    private final String currentOwnerName;
    private final String since;
    private final String imageUrl;
    private final List<LedgerEvent> ledgerEvents;

    @Getter
    @Builder
    public static class LedgerEvent {
        private final String date;
        private final String action;
        private final String hash;
        private final String prevHash;
        private final String correlationId;
        private final String dataJson;
        private final String actorName;
    }
}
