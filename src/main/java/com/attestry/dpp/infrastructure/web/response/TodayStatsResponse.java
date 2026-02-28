package com.attestry.dpp.infrastructure.web.response;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "of")
public class TodayStatsResponse {
    private final long assets;
    private final long transfers;
    private final long ledger;
}
