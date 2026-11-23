package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.KategoriFasilitas;
import com.sarpraskost.app.entity.KondisiFasilitas;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.StatusTiket;
import com.sarpraskost.app.entity.TingkatUrgensi;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record LaporanResponse(
    @JsonProperty("id_laporan") Integer idLaporan,
    @JsonProperty("id_pengguna") Integer idPengguna,
    @JsonProperty("nama_pelapor") String namaPelapor,
    @JsonProperty("no_hp_pelapor") String noHpPelapor,
    @JsonProperty("id_kamar") Integer idKamar,
    @JsonProperty("nomor_kamar") String nomorKamar,
    @JsonProperty("tipe_kamar") String tipeKamar,
    Short lantai,
    @JsonProperty("is_umum") boolean isUmum,
    @JsonProperty("id_fasilitas") Integer idFasilitas,
    @JsonProperty("kode_aset") String kodeAset,
    @JsonProperty("nama_fasilitas") String namaFasilitas,
    @JsonProperty("merk_tipe") String merkTipe,
    @JsonProperty("kondisi_fasilitas") KondisiFasilitas kondisiFasilitas,
    @JsonProperty("id_kategori") Integer idKategori,
    @JsonProperty("nama_kategori") String namaKategori,
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
    @JsonProperty("izin_masuk_kamar") boolean izinMasukKamar,
    @JsonProperty("catatan_terakhir") String catatanTerakhir,
    @JsonProperty("biaya_perbaikan") BigDecimal biayaPerbaikan,
    @JsonProperty("foto_sesudah_url") String fotoSesudahUrl,
    @JsonProperty("waktu_update_terakhir") LocalDateTime waktuUpdateTerakhir,
    @JsonProperty("nama_pemroses") String namaPemroses,
    List<RiwayatPenangananResponse> riwayat
) {
    public static LaporanResponse from(LaporanKerusakan laporan, List<RiwayatPenangananResponse> riwayat) {
        Pengguna pelapor = laporan.getPengguna();
        FasilitasKamar fasilitas = laporan.getFasilitas();
        Kamar kamar = fasilitas.getKamar();
        KategoriFasilitas kategori = fasilitas.getKategori();

        List<RiwayatPenangananResponse> safeRiwayat = riwayat == null ? List.of() : riwayat;
        RiwayatPenangananResponse latest = safeRiwayat.isEmpty() ? null : safeRiwayat.get(safeRiwayat.size() - 1);

        BigDecimal latestBiaya = null;
        String latestFotoSesudah = null;
        for (int i = safeRiwayat.size() - 1; i >= 0; i--) {
            RiwayatPenangananResponse item = safeRiwayat.get(i);
            if (latestBiaya == null && item.biayaPerbaikan() != null) {
                latestBiaya = item.biayaPerbaikan();
            }
            if (latestFotoSesudah == null && item.fotoSesudahUrl() != null && !item.fotoSesudahUrl().isBlank()) {
                latestFotoSesudah = item.fotoSesudahUrl();
            }
        }

        return new LaporanResponse(
            laporan.getIdLaporan(),
            pelapor.getIdPengguna(),
            pelapor.getNamaLengkap(),
            pelapor.getNoHp(),
            kamar.getIdKamar(),
            kamar.getNomorKamar(),
            kamar.getTipeKamar(),
            kamar.getLantai(),
            kamar.isFasilitasUmum(),
            fasilitas.getIdFasilitas(),
            fasilitas.getKodeAset(),
            fasilitas.getNamaFasilitas(),
            fasilitas.getMerkTipe(),
            fasilitas.getKondisi(),
            kategori.getIdKategori(),
            kategori.getNamaKategori(),
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
            laporan.isIzinMasukKamar(),
            latest != null ? latest.catatanPenanganan() : null,
            latestBiaya,
            latestFotoSesudah,
            latest != null ? latest.waktuUpdate() : laporan.getTglLapor(),
            latest != null ? latest.namaPengguna() : null,
            safeRiwayat
        );
    }
}
