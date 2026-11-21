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
    @JsonProperty("nomor_kamar_penghuni") String nomorKamarPenghuni,
    @JsonProperty("kategori_fasilitas") String kategoriFasilitas,
    @JsonProperty("lokasi_kamar") String lokasiKamar,
    String deskripsi,
    @JsonProperty("foto_url") String fotoUrl,
    StatusLaporan status,
    @JsonProperty("catatan_penanganan") String catatanPenanganan,
    @JsonProperty("mekanik_id") Long mekanikId,
    @JsonProperty("nama_mekanik") String namaMekanik,
    String prioritas,
    @JsonProperty("catatan_owner") String catatanOwner,
    @JsonProperty("kategori_penolakan") String kategoriPenolakan,
    @JsonProperty("alasan_penolakan") String alasanPenolakan,
    @JsonProperty("foto_selesai_url") String fotoSelesaiUrl,
    @JsonProperty("catatan_mekanik") String catatanMekanik,
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
            laporan.getPenghuni().getNomorKamar(),
            laporan.getKategoriFasilitas(),
            laporan.getLokasiKamar(),
            laporan.getDeskripsi(),
            laporan.getFotoUrl(),
            laporan.getStatus(),
            laporan.getCatatanPenanganan(),
            laporan.getMekanikId(),
            laporan.getNamaMekanik(),
            laporan.getPrioritas(),
            laporan.getCatatanOwner(),
            laporan.getKategoriPenolakan(),
            laporan.getAlasanPenolakan(),
            laporan.getFotoSelesaiUrl(),
            laporan.getCatatanMekanik(),
            laporan.getTanggalLapor(),
            laporan.getTanggalDiproses(),
            laporan.getTanggalSelesai()
        );
    }
}
