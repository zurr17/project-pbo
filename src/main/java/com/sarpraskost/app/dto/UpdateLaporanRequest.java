package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateLaporanRequest(
    @JsonProperty("id_fasilitas") @JsonAlias("idFasilitas") Long idFasilitas,
    @JsonProperty("judul_laporan") @JsonAlias("judulLaporan") String judulLaporan,
    @JsonProperty("deskripsi_kerusakan") @JsonAlias({"deskripsi", "deskripsiKerusakan"}) String deskripsiKerusakan
) {
}
