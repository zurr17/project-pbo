package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.StatusLaporan;
import java.time.LocalDateTime;

public record LaporanResponse(
    Long id,
    @JsonProperty("kode_laporan") String kodeLaporan,
    @JsonProperty("penghuni_id") Long penghuniId,
    @JsonProperty("nama_penghuni") String namaPenghuni,
    @JsonProperty("kategori_fasilitas") String kategoriFasilitas,
    @JsonProperty("lokasi_kamar") String lokasiKamar,
    String deskripsi,
    @JsonProperty("foto_url") String fotoUrl,
    StatusLaporan status,
    @JsonProperty("catatan_penanganan") String catatanPenanganan,
    @JsonProperty("tanggal_lapor") LocalDateTime tanggalLapor,
    @JsonProperty("tanggal_diproses") LocalDateTime tanggalDiproses,
    @JsonProperty("tanggal_selesai") LocalDateTime tanggalSelesai
) {
    public static LaporanResponse from(LaporanKerusakan laporan) {
        return new LaporanResponse(
            laporan.getId(),
            laporan.getKodeLaporan(),
            laporan.getPenghuni().getId(),
            laporan.getPenghuni().getNama(),
            laporan.getKategoriFasilitas(),
            laporan.getLokasiKamar(),
            laporan.getDeskripsi(),
            laporan.getFotoUrl(),
            laporan.getStatus(),
            laporan.getCatatanPenanganan(),
            laporan.getTanggalLapor(),
            laporan.getTanggalDiproses(),
            laporan.getTanggalSelesai()
        );
    }
}
