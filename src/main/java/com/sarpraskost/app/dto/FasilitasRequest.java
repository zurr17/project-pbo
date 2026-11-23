package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record FasilitasRequest(
    @JsonProperty("id_kamar") @JsonAlias("idKamar") Integer idKamar,
    @JsonProperty("id_kategori") @JsonAlias("idKategori") Integer idKategori,
    @JsonProperty("kode_aset") @JsonAlias("kodeAset") String kodeAset,
    @JsonProperty("nama_fasilitas") @JsonAlias("namaFasilitas") String namaFasilitas,
    @JsonProperty("merk_tipe") @JsonAlias("merkTipe") String merkTipe,
    String kondisi,
    @JsonProperty("tgl_pasang") @JsonAlias("tglPasang") LocalDate tglPasang
) {
}
