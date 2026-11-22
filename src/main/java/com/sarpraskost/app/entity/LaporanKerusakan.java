package com.sarpraskost.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private Long idLaporan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_fasilitas", nullable = false)
    private FasilitasKamar fasilitas;

    @Column(name = "kode_tiket", nullable = false, unique = true, length = 30)
    private String kodeTiket;

    @Column(name = "judul_laporan", nullable = false, length = 150)
    private String judulLaporan;

    @Column(name = "deskripsi_kerusakan", nullable = false, columnDefinition = "TEXT")
    private String deskripsiKerusakan;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Column(name = "tgl_lapor", nullable = false)
    private LocalDateTime tglLapor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_tiket", nullable = false, length = 30)
    private StatusTiket statusTiket;

    public LaporanKerusakan() {
    }

    public Long getIdLaporan() {
        return idLaporan;
    }

    public void setIdLaporan(Long idLaporan) {
        this.idLaporan = idLaporan;
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
        this.fotoUrl = fotoUrl;
    }

    public LocalDateTime getTglLapor() {
        return tglLapor;
    }

    public void setTglLapor(LocalDateTime tglLapor) {
        this.tglLapor = tglLapor;
    }

    public StatusTiket getStatusTiket() {
        return statusTiket;
    }

    public void setStatusTiket(StatusTiket statusTiket) {
        this.statusTiket = statusTiket;
    }
}
