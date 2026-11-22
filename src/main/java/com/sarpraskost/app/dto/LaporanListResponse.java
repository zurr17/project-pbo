package com.sarpraskost.app.dto;

import java.util.List;

public record LaporanListResponse(
    LaporanSummaryResponse ringkasan,
    List<LaporanResponse> laporan
) {
}
