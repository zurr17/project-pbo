package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Notifikasi;
import com.sarpraskost.app.entity.StatusTiket;
import java.time.LocalDateTime;

public record NotifikasiResponse(
    @JsonProperty("id_notifikasi") Integer idNotifikasi,
    @JsonProperty("id_pengguna") Integer idPengguna,
    @JsonProperty("id_laporan") Integer idLaporan,
    @JsonProperty("kode_tiket") String kodeTiket,
    @JsonProperty("judul_laporan") String judulLaporan,
    @JsonProperty("status_tiket") StatusTiket statusTiket,
    @JsonProperty("is_urgent") boolean isUrgent,
    @JsonProperty("izin_masuk_kamar") boolean izinMasukKamar,
    @JsonProperty("jumlah_terdampak") Integer jumlahTerdampak,
    @JsonProperty("is_umum") boolean isUmum,
    @JsonProperty("nomor_kamar") String nomorKamar,
    @JsonProperty("has_foto_bukti") boolean hasFotoBukti,
    @JsonProperty("judul_notif") String judulNotif,
    @JsonProperty("pesan_notif") String pesanNotif,
    @JsonProperty("is_read") boolean isRead,
    @JsonProperty("waktu_kirim") LocalDateTime waktuKirim
) {
    public static NotifikasiResponse from(Notifikasi n) {
        LaporanKerusakan laporan = n.getLaporan();
        FasilitasKamar fasilitas = laporan.getFasilitas();
        Kamar kamar = fasilitas.getKamar();
        boolean hasFoto = laporan.getFotoUrl() != null && !laporan.getFotoUrl().isBlank();
        return new NotifikasiResponse(
            n.getIdNotifikasi(),
            n.getPengguna().getIdPengguna(),
            laporan.getIdLaporan(),
            laporan.getKodeTiket(),
            laporan.getJudulLaporan(),
            laporan.getStatusTiket(),
            laporan.isUrgent(),
            laporan.isIzinMasukKamar(),
            laporan.getJumlahTerdampak(),
            kamar.isFasilitasUmum(),
            kamar.getNomorKamar(),
            hasFoto,
            n.getJudulNotif(),
            n.getPesanNotif(),
            n.isRead(),
            n.getWaktuKirim()
        );
    }
}
