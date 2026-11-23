package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateLaporanRequest(
    @JsonProperty("id_fasilitas") @JsonAlias("idFasilitas") Integer idFasilitas,
    @JsonProperty("judul_laporan") @JsonAlias("judulLaporan") String judulLaporan,
    @JsonProperty("deskripsi_kerusakan") @JsonAlias({"deskripsi", "deskripsiKerusakan"}) String deskripsiKerusakan,
    @JsonProperty("tingkat_urgensi") @JsonAlias("tingkatUrgensi") String tingkatUrgensi,
    @JsonProperty("is_urgent") @JsonAlias("isUrgent") Boolean isUrgent,
    @JsonProperty("izin_masuk_kamar") @JsonAlias("izinMasukKamar") Boolean izinMasukKamar
) {
}
