package com.attestry.dpp.infrastructure.web.mapper;

import com.attestry.dpp.application.dto.result.TodayStatsResult;
import com.attestry.dpp.infrastructure.web.response.TodayStatsResponse;

public final class StatsResponseMapper {

    private StatsResponseMapper() {
    }

    public static TodayStatsResponse toTodayStatsResponse(TodayStatsResult result) {
        return TodayStatsResponse.of(result.getAssets(), result.getTransfers(), result.getLedger());
    }
}
