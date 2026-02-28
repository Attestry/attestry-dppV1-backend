package com.attestry.dpp.infrastructure.web.controller;

import com.attestry.dpp.application.dto.result.TodayStatsResult;
import com.attestry.dpp.application.usecase.query.StatsQueryUseCase;
import com.attestry.dpp.infrastructure.web.mapper.StatsResponseMapper;
import com.attestry.dpp.infrastructure.web.response.ApiResponse;
import com.attestry.dpp.infrastructure.web.response.TodayStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/stats", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class StatsController {

    private final StatsQueryUseCase statsQueryUseCase;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayStatsResponse>> getTodayStats() {
        TodayStatsResult result = statsQueryUseCase.getTodayStats();
        TodayStatsResponse response = StatsResponseMapper.toTodayStatsResponse(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
