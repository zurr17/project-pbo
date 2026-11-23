package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.StatusHunian;

public record KamarResponse(
    @JsonProperty("id_kamar") Integer idKamar,
    @JsonProperty("nomor_kamar") String nomorKamar,
    @JsonProperty("tipe_kamar") String tipeKamar,
    Short lantai,
    @JsonProperty("status_hunian") StatusHunian statusHunian,
    @JsonProperty("is_umum") boolean isUmum,
    @JsonProperty("jumlah_fasilitas") long jumlahFasilitas,
    @JsonProperty("jumlah_penghuni") long jumlahPenghuni
) {
    public static KamarResponse from(Kamar kamar, long jumlahFasilitas, long jumlahPenghuni) {
        return new KamarResponse(
            kamar.getIdKamar(),
            kamar.getNomorKamar(),
            kamar.getTipeKamar(),
            kamar.getLantai(),
            kamar.getStatusHunian(),
            kamar.isFasilitasUmum(),
            jumlahFasilitas,
            jumlahPenghuni
        );
    }
}
