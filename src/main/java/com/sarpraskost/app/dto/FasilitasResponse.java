package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.KategoriFasilitas;
import com.sarpraskost.app.entity.KondisiFasilitas;
import java.time.LocalDate;

public record FasilitasResponse(
    @JsonProperty("id_fasilitas") Long idFasilitas,
    @JsonProperty("id_kamar") Long idKamar,
    @JsonProperty("nomor_kamar") String nomorKamar,
    @JsonProperty("tipe_kamar") String tipeKamar,
    Integer lantai,
    @JsonProperty("id_kategori") Long idKategori,
    @JsonProperty("nama_kategori") String namaKategori,
    @JsonProperty("kode_aset") String kodeAset,
    @JsonProperty("nama_fasilitas") String namaFasilitas,
    @JsonProperty("merk_tipe") String merkTipe,
    KondisiFasilitas kondisi,
    @JsonProperty("tgl_pasang") LocalDate tglPasang
) {
    public static FasilitasResponse from(FasilitasKamar f) {
        Kamar kamar = f.getKamar();
        KategoriFasilitas kat = f.getKategori();
        return new FasilitasResponse(
            f.getIdFasilitas(),
            kamar.getIdKamar(),
            kamar.getNomorKamar(),
            kamar.getTipeKamar(),
            kamar.getLantai(),
            kat.getIdKategori(),
            kat.getNamaKategori(),
            f.getKodeAset(),
            f.getNamaFasilitas(),
            f.getMerkTipe(),
            f.getKondisi(),
            f.getTglPasang()
        );
    }
}
