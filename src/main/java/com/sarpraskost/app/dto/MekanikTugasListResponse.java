package com.sarpraskost.app.dto;

import java.util.List;

public record MekanikTugasListResponse(
    LaporanSummaryResponse ringkasan,
    List<LaporanResponse> laporan
) {
}
