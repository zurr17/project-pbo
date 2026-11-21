package com.sarpraskost.app.dto;

import java.util.List;

public record MekanikDashboardResponse(
    LaporanSummaryResponse ringkasan,
    List<LaporanResponse> laporan
) {
}
