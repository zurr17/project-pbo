package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record OwnerProsesRequest(
    @JsonProperty("id_pengguna") @JsonAlias({"id_owner", "idOwner", "idPengguna"}) Long idPengguna,
    @JsonProperty("catatan_penanganan") @JsonAlias({"catatanPenanganan", "catatan_owner", "catatanOwner"}) String catatanPenanganan,
    @JsonProperty("biaya_perbaikan") @JsonAlias({"biayaPerbaikan", "estimasi_biaya", "estimasiBiaya"}) BigDecimal biayaPerbaikan,
    @JsonProperty("kondisi_fasilitas") @JsonAlias("kondisiFasilitas") String kondisiFasilitas
) {
}
