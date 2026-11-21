package com.sarpraskost.app.dto;

import java.util.List;

public record PenghuniLaporanListResponse(
    LaporanSummaryResponse ringkasan,
    List<LaporanResponse> laporan
) {
}
