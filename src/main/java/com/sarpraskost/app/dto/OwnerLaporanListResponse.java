package com.sarpraskost.app.dto;

import java.util.List;

public record OwnerLaporanListResponse(
    LaporanSummaryResponse ringkasan,
    List<LaporanResponse> laporan
) {
}
