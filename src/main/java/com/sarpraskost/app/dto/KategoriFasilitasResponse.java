package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.KategoriFasilitas;

public record KategoriFasilitasResponse(
    @JsonProperty("id_kategori") Integer idKategori,
    @JsonProperty("nama_kategori") String namaKategori,
    String deskripsi,
    @JsonProperty("jumlah_fasilitas") long jumlahFasilitas
) {
    public static KategoriFasilitasResponse from(KategoriFasilitas kategori, long jumlahFasilitas) {
        return new KategoriFasilitasResponse(
            kategori.getIdKategori(),
            kategori.getNamaKategori(),
            kategori.getDeskripsi(),
            jumlahFasilitas
        );
    }
}
