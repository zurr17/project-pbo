package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OwnerTolakRequest(
    @JsonProperty("kategori_penolakan") @JsonAlias("kategoriPenolakan") String kategoriPenolakan,
    @JsonProperty("alasan_penolakan") @JsonAlias("alasanPenolakan") String alasanPenolakan
) {
}
