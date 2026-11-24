package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.Role;

public record PenggunaResponse(
    @JsonProperty("id_pengguna") Integer idPengguna,
    @JsonProperty("id_kamar") Integer idKamar,
    String gedung,
    @JsonProperty("nomor_kamar") String nomorKamar,
    @JsonProperty("tipe_kamar") String tipeKamar,
    Short lantai,
    @JsonProperty("nama_lengkap") String namaLengkap,
    String email,
    @JsonProperty("no_hp") String noHp,
    Role role,
    @JsonProperty("redirect_url") String redirectUrl
) {
    public static PenggunaResponse from(Pengguna pengguna) {
        Kamar kamar = pengguna.getKamar();
        String redirect = pengguna.getRole() == Role.OWNER ? "owner-home.html" : "home.html";
        return new PenggunaResponse(
            pengguna.getIdPengguna(),
            kamar != null ? kamar.getIdKamar() : null,
            kamar != null ? kamar.getGedung() : null,
            kamar != null ? kamar.getNomorKamar() : null,
            kamar != null ? kamar.getTipeKamar() : null,
            kamar != null ? kamar.getLantai() : null,
            pengguna.getNamaLengkap(),
            pengguna.getEmail(),
            pengguna.getNoHp(),
            pengguna.getRole(),
            redirect
        );
    }
}
