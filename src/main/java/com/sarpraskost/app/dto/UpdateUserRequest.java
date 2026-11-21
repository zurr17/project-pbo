package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateUserRequest(
    String nama,
    @JsonProperty("nomor_kamar") @JsonAlias("nomorKamar") String nomorKamar,
    String password
) {
}

