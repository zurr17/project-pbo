package com.sarpraskost.app.entity;

import com.sarpraskost.app.entity.converter.StatusTiketConverter;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "riwayat_penanganan")
public class RiwayatPenanganan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_riwayat")
    private Integer idRiwayat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_laporan", nullable = false)
    private LaporanKerusakan laporan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @Convert(converter = StatusTiketConverter.class)
    @Column(name = "status_baru", nullable = false, length = 15)
    private StatusTiket statusBaru;

    @Column(name = "catatan_penanganan", nullable = false, columnDefinition = "text")
    private String catatanPenanganan = "";

    @Column(name = "foto_sesudah_url", length = 255)
    private String fotoSesudahUrl;

    @Column(name = "biaya_perbaikan", nullable = false, precision = 12, scale = 2)
    private BigDecimal biayaPerbaikan = BigDecimal.ZERO;

    @Column(name = "waktu_update", nullable = false)
    private LocalDateTime waktuUpdate;

    public RiwayatPenanganan() {
    }

    public Integer getIdRiwayat() {
        return idRiwayat;
    }

    public void setIdRiwayat(Integer idRiwayat) {
        this.idRiwayat = idRiwayat;
    }

    public LaporanKerusakan getLaporan() {
        return laporan;
    }

    public void setLaporan(LaporanKerusakan laporan) {
        this.laporan = laporan;
    }

    public Pengguna getPengguna() {
        return pengguna;
    }

    public void setPengguna(Pengguna pengguna) {
        this.pengguna = pengguna;
    }

    public StatusTiket getStatusBaru() {
        return statusBaru;
    }

    public void setStatusBaru(StatusTiket statusBaru) {
        this.statusBaru = statusBaru;
    }

    public String getCatatanPenanganan() {
        return catatanPenanganan;
    }

    public void setCatatanPenanganan(String catatanPenanganan) {
        this.catatanPenanganan = catatanPenanganan == null ? "" : catatanPenanganan;
    }

    public String getFotoSesudahUrl() {
        return fotoSesudahUrl;
    }

    public void setFotoSesudahUrl(String fotoSesudahUrl) {
        this.fotoSesudahUrl = fotoSesudahUrl;
    }

    public BigDecimal getBiayaPerbaikan() {
        return biayaPerbaikan;
    }

    public void setBiayaPerbaikan(BigDecimal biayaPerbaikan) {
        this.biayaPerbaikan = biayaPerbaikan == null ? BigDecimal.ZERO : biayaPerbaikan;
    }

    public LocalDateTime getWaktuUpdate() {
        return waktuUpdate;
    }

    public void setWaktuUpdate(LocalDateTime waktuUpdate) {
        this.waktuUpdate = waktuUpdate;
    }
}
