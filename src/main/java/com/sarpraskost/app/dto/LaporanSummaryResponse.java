package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record LaporanSummaryResponse(
    long total,
    long menunggu,
    long diproses,
    long selesai,
    long ditolak,
    long darurat,
    @JsonProperty("total_biaya_internal") BigDecimal totalBiayaInternal
) {
}
