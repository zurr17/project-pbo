package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.KategoriFasilitas;
import com.sarpraskost.app.entity.KondisiFasilitas;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.StatusTiket;
import com.sarpraskost.app.entity.TingkatUrgensi;
import java.time.LocalDateTime;

/**
 * Representasi publik untuk banner Gangguan Fasilitas Umum Aktif.
 * Sengaja tidak mengekspos identitas pelapor pertama (privasi penghuni).
 */
public record FasilitasUmumAktifResponse(
    @JsonProperty("id_laporan") Integer idLaporan,
    @JsonProperty("kode_tiket") String kodeTiket,
    @JsonProperty("judul_laporan") String judulLaporan,
    @JsonProperty("deskripsi_kerusakan") String deskripsiKerusakan,
    @JsonProperty("foto_url") String fotoUrl,
    @JsonProperty("tgl_lapor") LocalDateTime tglLapor,
    @JsonProperty("tingkat_urgensi") TingkatUrgensi tingkatUrgensi,
    @JsonProperty("status_tiket") StatusTiket statusTiket,
    @JsonProperty("jumlah_terdampak") Integer jumlahTerdampak,
    @JsonProperty("jadwal_pengerjaan") LocalDateTime jadwalPengerjaan,
    @JsonProperty("is_urgent") boolean isUrgent,
    @JsonProperty("id_fasilitas") Integer idFasilitas,
    @JsonProperty("kode_aset") String kodeAset,
    @JsonProperty("nama_fasilitas") String namaFasilitas,
    @JsonProperty("merk_tipe") String merkTipe,
    @JsonProperty("kondisi_fasilitas") KondisiFasilitas kondisiFasilitas,
    @JsonProperty("nama_kategori") String namaKategori,
    String gedung,
    @JsonProperty("nomor_kamar") String nomorKamar,
    @JsonProperty("tipe_kamar") String tipeKamar,
    Short lantai,
    @JsonProperty("catatan_terakhir") String catatanTerakhir
) {
    public static FasilitasUmumAktifResponse from(LaporanKerusakan laporan, String catatanTerakhir) {
        FasilitasKamar fasilitas = laporan.getFasilitas();
        Kamar kamar = fasilitas.getKamar();
        KategoriFasilitas kategori = fasilitas.getKategori();
        return new FasilitasUmumAktifResponse(
            laporan.getIdLaporan(),
            laporan.getKodeTiket(),
            laporan.getJudulLaporan(),
            laporan.getDeskripsiKerusakan(),
            laporan.getFotoUrl(),
            laporan.getTglLapor(),
            laporan.getTingkatUrgensi(),
            laporan.getStatusTiket(),
            laporan.getJumlahTerdampak(),
            laporan.getJadwalPengerjaan(),
            laporan.isUrgent(),
            fasilitas.getIdFasilitas(),
            fasilitas.getKodeAset(),
            fasilitas.getNamaFasilitas(),
            fasilitas.getMerkTipe(),
            fasilitas.getKondisi(),
            kategori.getNamaKategori(),
            kamar.getGedung(),
            kamar.getNomorKamar(),
            kamar.getTipeKamar(),
            kamar.getLantai(),
            catatanTerakhir
        );
    }
}
