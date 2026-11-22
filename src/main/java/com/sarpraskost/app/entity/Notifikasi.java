package com.sarpraskost.app.entity;

import jakarta.persistence.Column;
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
@Table(name = "notifikasi")
public class Notifikasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notifikasi")
    private Long idNotifikasi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pengguna", nullable = false)
    private Pengguna pengguna;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_laporan", nullable = false)
    private LaporanKerusakan laporan;

    @Column(name = "judul_notif", nullable = false, length = 150)
    private String judulNotif;

    @Column(name = "pesan_notif", nullable = false, columnDefinition = "TEXT")
    private String pesanNotif;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "waktu_kirim", nullable = false)
    private LocalDateTime waktuKirim;

    public Notifikasi() {
    }

    public Long getIdNotifikasi() {
        return idNotifikasi;
    }

    public void setIdNotifikasi(Long idNotifikasi) {
        this.idNotifikasi = idNotifikasi;
    }

    public Pengguna getPengguna() {
        return pengguna;
    }

    public void setPengguna(Pengguna pengguna) {
        this.pengguna = pengguna;
    }

    public LaporanKerusakan getLaporan() {
        return laporan;
    }

    public void setLaporan(LaporanKerusakan laporan) {
        this.laporan = laporan;
    }

    public String getJudulNotif() {
        return judulNotif;
    }

    public void setJudulNotif(String judulNotif) {
        this.judulNotif = judulNotif;
    }

    public String getPesanNotif() {
        return pesanNotif;
    }

    public void setPesanNotif(String pesanNotif) {
        this.pesanNotif = pesanNotif;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getWaktuKirim() {
        return waktuKirim;
    }

    public void setWaktuKirim(LocalDateTime waktuKirim) {
        this.waktuKirim = waktuKirim;
    }
}
