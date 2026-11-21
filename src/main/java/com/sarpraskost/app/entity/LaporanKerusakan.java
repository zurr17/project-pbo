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
    private Long id;

    @Column(name = "kode_laporan", nullable = false, unique = true, length = 30)
    private String kodeLaporan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "penghuni_id", nullable = false)
    private User penghuni;

    @Column(name = "kategori_fasilitas", nullable = false, length = 100)
    private String kategoriFasilitas;

    @Column(name = "lokasi_kamar", nullable = false, length = 120)
    private String lokasiKamar;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String deskripsi;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusLaporan status;

    @Column(name = "catatan_penanganan", columnDefinition = "TEXT")
    private String catatanPenanganan;

    @Column(name = "tanggal_lapor", nullable = false)
    private LocalDateTime tanggalLapor;

    @Column(name = "tanggal_diproses")
    private LocalDateTime tanggalDiproses;

    @Column(name = "tanggal_selesai")
    private LocalDateTime tanggalSelesai;

    @Column(name = "mekanik_id")
    private Long mekanikId;

    @Column(name = "nama_mekanik", length = 120)
    private String namaMekanik;

    @Column(name = "prioritas", length = 30)
    private String prioritas;

    @Column(name = "catatan_owner", columnDefinition = "TEXT")
    private String catatanOwner;

    @Column(name = "kategori_penolakan", length = 120)
    private String kategoriPenolakan;

    @Column(name = "alasan_penolakan", columnDefinition = "TEXT")
    private String alasanPenolakan;

    @Column(name = "foto_selesai_url", length = 255)
    private String fotoSelesaiUrl;

    @Column(name = "catatan_mekanik", columnDefinition = "TEXT")
    private String catatanMekanik;

    public LaporanKerusakan() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodeLaporan() {
        return kodeLaporan;
    }

    public void setKodeLaporan(String kodeLaporan) {
        this.kodeLaporan = kodeLaporan;
    }

    public User getPenghuni() {
        return penghuni;
    }

    public void setPenghuni(User penghuni) {
        this.penghuni = penghuni;
    }

    public String getKategoriFasilitas() {
        return kategoriFasilitas;
    }

    public void setKategoriFasilitas(String kategoriFasilitas) {
        this.kategoriFasilitas = kategoriFasilitas;
    }

    public String getLokasiKamar() {
        return lokasiKamar;
    }

    public void setLokasiKamar(String lokasiKamar) {
        this.lokasiKamar = lokasiKamar;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public StatusLaporan getStatus() {
        return status;
    }

    public void setStatus(StatusLaporan status) {
        this.status = status;
    }

    public String getCatatanPenanganan() {
        return catatanPenanganan;
    }

    public void setCatatanPenanganan(String catatanPenanganan) {
        this.catatanPenanganan = catatanPenanganan;
    }

    public LocalDateTime getTanggalLapor() {
        return tanggalLapor;
    }

    public void setTanggalLapor(LocalDateTime tanggalLapor) {
        this.tanggalLapor = tanggalLapor;
    }

    public LocalDateTime getTanggalDiproses() {
        return tanggalDiproses;
    }

    public void setTanggalDiproses(LocalDateTime tanggalDiproses) {
        this.tanggalDiproses = tanggalDiproses;
    }

    public LocalDateTime getTanggalSelesai() {
        return tanggalSelesai;
    }

    public void setTanggalSelesai(LocalDateTime tanggalSelesai) {
        this.tanggalSelesai = tanggalSelesai;
    }

    public Long getMekanikId() {
        return mekanikId;
    }

    public void setMekanikId(Long mekanikId) {
        this.mekanikId = mekanikId;
    }

    public String getNamaMekanik() {
        return namaMekanik;
    }

    public void setNamaMekanik(String namaMekanik) {
        this.namaMekanik = namaMekanik;
    }

    public String getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }

    public String getCatatanOwner() {
        return catatanOwner;
    }

    public void setCatatanOwner(String catatanOwner) {
        this.catatanOwner = catatanOwner;
    }

    public String getKategoriPenolakan() {
        return kategoriPenolakan;
    }

    public void setKategoriPenolakan(String kategoriPenolakan) {
        this.kategoriPenolakan = kategoriPenolakan;
    }

    public String getAlasanPenolakan() {
        return alasanPenolakan;
    }

    public void setAlasanPenolakan(String alasanPenolakan) {
        this.alasanPenolakan = alasanPenolakan;
    }

    public String getFotoSelesaiUrl() {
        return fotoSelesaiUrl;
    }

    public void setFotoSelesaiUrl(String fotoSelesaiUrl) {
        this.fotoSelesaiUrl = fotoSelesaiUrl;
    }

    public String getCatatanMekanik() {
        return catatanMekanik;
    }

    public void setCatatanMekanik(String catatanMekanik) {
        this.catatanMekanik = catatanMekanik;
    }
}
