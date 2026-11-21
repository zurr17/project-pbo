package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FasilitasRequest(
    @JsonProperty("penghuni_id") @JsonAlias("penghuniId") Long penghuniId,
    @JsonProperty("nama_fasilitas") @JsonAlias("namaFasilitas") String namaFasilitas,
    @JsonProperty("merk_tipe") @JsonAlias("merkTipe") String merkTipe,
    String kondisi,
    String keterangan
) {
}

