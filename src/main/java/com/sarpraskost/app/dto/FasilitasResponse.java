package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.KondisiFasilitas;

public record FasilitasResponse(
    Long id,
    @JsonProperty("penghuni_id") Long penghuniId,
    @JsonProperty("nama_fasilitas") String namaFasilitas,
    @JsonProperty("merk_tipe") String merkTipe,
    KondisiFasilitas kondisi,
    String keterangan
) {
    public static FasilitasResponse from(FasilitasKamar fasilitas) {
        return new FasilitasResponse(
            fasilitas.getId(),
            fasilitas.getPenghuni().getId(),
            fasilitas.getNamaFasilitas(),
            fasilitas.getMerkTipe(),
            fasilitas.getKondisi(),
            fasilitas.getKeterangan()
        );
    }
}
