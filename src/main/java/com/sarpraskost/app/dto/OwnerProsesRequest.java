package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OwnerProsesRequest(
    @JsonProperty("id_pengguna") @JsonAlias({"id_owner", "idOwner", "idPengguna"}) Integer idPengguna,
    @JsonProperty("catatan_penanganan") @JsonAlias({"catatanPenanganan", "catatan_owner", "catatanOwner"}) String catatanPenanganan,
    @JsonProperty("biaya_perbaikan") @JsonAlias({"biayaPerbaikan", "estimasi_biaya", "estimasiBiaya"}) BigDecimal biayaPerbaikan,
    @JsonProperty("kondisi_fasilitas") @JsonAlias("kondisiFasilitas") String kondisiFasilitas,
    @JsonProperty("jadwal_pengerjaan") @JsonAlias("jadwalPengerjaan") LocalDateTime jadwalPengerjaan
) {
}
