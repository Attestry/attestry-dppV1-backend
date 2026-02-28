package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.TodayStatsResult;

public interface StatsQueryUseCase {
    TodayStatsResult getTodayStats();
}
