package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record KategoriFasilitasRequest(
    @JsonProperty("nama_kategori") @JsonAlias("namaKategori") String namaKategori,
    String deskripsi
) {
}
