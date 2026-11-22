package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record KamarRequest(
    @JsonProperty("nomor_kamar") @JsonAlias("nomorKamar") String nomorKamar,
    @JsonProperty("tipe_kamar") @JsonAlias("tipeKamar") String tipeKamar,
    Integer lantai,
    @JsonProperty("status_hunian") @JsonAlias("statusHunian") String statusHunian
) {
}
