package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterUserRequest(
    String nama,
    String email,
    String password,
    @JsonProperty("nomor_kamar") @JsonAlias("nomorKamar") String nomorKamar
) {
}

