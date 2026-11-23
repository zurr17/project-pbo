package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdatePenggunaRequest(
    @JsonProperty("nama_lengkap") @JsonAlias({"nama", "namaLengkap"}) String namaLengkap,
    @JsonProperty("no_hp") @JsonAlias("noHp") String noHp,
    @JsonProperty("id_kamar") @JsonAlias("idKamar") Integer idKamar,
    @JsonProperty("nomor_kamar") @JsonAlias("nomorKamar") String nomorKamar,
    String password,
    String role
) {
}
