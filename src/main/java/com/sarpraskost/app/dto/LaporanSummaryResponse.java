package com.sarpraskost.app.dto;

public record LaporanSummaryResponse(
    long total,
    long menunggu,
    long diproses,
    long selesai,
    long ditolak,
    long darurat
) {
}
