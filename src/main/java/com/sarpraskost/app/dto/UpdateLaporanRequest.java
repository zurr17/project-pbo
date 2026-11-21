package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateLaporanRequest(
    @JsonProperty("kategori_fasilitas") @JsonAlias("kategoriFasilitas") String kategoriFasilitas,
    @JsonProperty("lokasi_kamar") @JsonAlias("lokasiKamar") String lokasiKamar,
    String deskripsi
) {
}

