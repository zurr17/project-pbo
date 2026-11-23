package com.sarpraskost.app.entity;

import com.sarpraskost.app.entity.converter.StatusTiketConverter;
import com.sarpraskost.app.entity.converter.TingkatUrgensiConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "laporan_kerusakan")
public class LaporanKerusakan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_laporan")
    private Integer idLaporan;

    @Column(name = "kode_tiket", nullable = false, unique = true, length = 25)
    private String kodeTiket;

    @Column(name = "judul_laporan", nullable = false, length = 100)
    private String judulLaporan;

    @Column(name = "deskripsi_kerusakan", nullable = false, columnDefinition = "text")
    private String deskripsiKerusakan;

    @Column(name = "foto_url", nullable = false, length = 255)
    private String fotoUrl = "";

    @Convert(converter = TingkatUrgensiConverter.class)
    @Column(name = "tingkat_urgensi", nullable = false, length = 15)
    private TingkatUrgensi tingkatUrgensi = TingkatUrgensi.SEDANG;

    @Convert(converter = StatusTiketConverter.class)
    @Column(name = "status_tiket", nullable = false, length = 15)
    private StatusTiket statusTiket = StatusTiket.MENUNGGU;

    @Column(name = "tgl_lapor", nullable = false)
    private LocalDateTime tglLapor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_fasilitas", nullable = false)
    private FasilitasKamar fasilitas;

    @Column(name = "jumlah_terdampak", nullable = false)
    private Integer jumlahTerdampak = 1;

    @Column(name = "jadwal_pengerjaan")
    private LocalDateTime jadwalPengerjaan;

    @Column(name = "is_urgent", nullable = false)
    private boolean isUrgent = false;

    @Column(name = "izin_masuk_kamar", nullable = false)
    private boolean izinMasukKamar = false;

    public LaporanKerusakan() {
    }

    public Integer getIdLaporan() {
        return idLaporan;
    }

    public void setIdLaporan(Integer idLaporan) {
        this.idLaporan = idLaporan;
    }

    public String getKodeTiket() {
        return kodeTiket;
    }

    public void setKodeTiket(String kodeTiket) {
        this.kodeTiket = kodeTiket;
    }

    public String getJudulLaporan() {
        return judulLaporan;
    }

    public void setJudulLaporan(String judulLaporan) {
        this.judulLaporan = judulLaporan;
    }

    public String getDeskripsiKerusakan() {
        return deskripsiKerusakan;
    }

    public void setDeskripsiKerusakan(String deskripsiKerusakan) {
        this.deskripsiKerusakan = deskripsiKerusakan;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl == null ? "" : fotoUrl;
    }

    public TingkatUrgensi getTingkatUrgensi() {
        return tingkatUrgensi;
    }

    public void setTingkatUrgensi(TingkatUrgensi tingkatUrgensi) {
        this.tingkatUrgensi = tingkatUrgensi == null ? TingkatUrgensi.SEDANG : tingkatUrgensi;
    }

    public StatusTiket getStatusTiket() {
        return statusTiket;
    }

    public void setStatusTiket(StatusTiket statusTiket) {
        this.statusTiket = statusTiket == null ? StatusTiket.MENUNGGU : statusTiket;
    }

    public LocalDateTime getTglLapor() {
        return tglLapor;
    }

    public void setTglLapor(LocalDateTime tglLapor) {
        this.tglLapor = tglLapor;
    }

    public Pengguna getPengguna() {
        return pengguna;
    }

    public void setPengguna(Pengguna pengguna) {
        this.pengguna = pengguna;
    }

    public FasilitasKamar getFasilitas() {
        return fasilitas;
    }

    public void setFasilitas(FasilitasKamar fasilitas) {
        this.fasilitas = fasilitas;
    }

    public Integer getJumlahTerdampak() {
        return jumlahTerdampak;
    }

    public void setJumlahTerdampak(Integer jumlahTerdampak) {
        this.jumlahTerdampak = jumlahTerdampak == null || jumlahTerdampak < 1 ? 1 : jumlahTerdampak;
    }

    public LocalDateTime getJadwalPengerjaan() {
        return jadwalPengerjaan;
    }

    public void setJadwalPengerjaan(LocalDateTime jadwalPengerjaan) {
        this.jadwalPengerjaan = jadwalPengerjaan;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public boolean isIzinMasukKamar() {
        return izinMasukKamar;
    }

    public void setIzinMasukKamar(boolean izinMasukKamar) {
        this.izinMasukKamar = izinMasukKamar;
    }
}
