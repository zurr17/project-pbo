package com.sarpraskost.app.dto;

import java.util.List;

public record OwnerDashboardResponse(
    LaporanSummaryResponse ringkasan,
    List<LaporanResponse> laporan
) {
}
