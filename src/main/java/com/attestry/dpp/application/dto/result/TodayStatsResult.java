package com.attestry.dpp.application.dto.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "of")
public class TodayStatsResult {
    private final long assets;
    private final long transfers;
    private final long ledger;
}
