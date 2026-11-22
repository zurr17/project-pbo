package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OwnerTolakRequest(
    @JsonProperty("id_pengguna") @JsonAlias({"id_owner", "idOwner", "idPengguna"}) Long idPengguna,
    @JsonProperty("kategori_penolakan") @JsonAlias("kategoriPenolakan") String kategoriPenolakan,
    @JsonProperty("alasan_penolakan") @JsonAlias({"alasanPenolakan", "catatan_penanganan", "catatanPenanganan"}) String alasanPenolakan
) {
}
