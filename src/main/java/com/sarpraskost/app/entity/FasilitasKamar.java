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

@Entity
@Table(name = "fasilitas_kamar")
public class FasilitasKamar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "penghuni_id", nullable = false)
    private User penghuni;

    @Column(name = "nama_fasilitas", nullable = false, length = 120)
    private String namaFasilitas;

    @Column(name = "merk_tipe", length = 120)
    private String merkTipe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KondisiFasilitas kondisi;

    @Column(columnDefinition = "TEXT")
    private String keterangan;

    public FasilitasKamar() {
    }

    public FasilitasKamar(
        User penghuni,
        String namaFasilitas,
        String merkTipe,
        KondisiFasilitas kondisi,
        String keterangan
    ) {
        this.penghuni = penghuni;
        this.namaFasilitas = namaFasilitas;
        this.merkTipe = merkTipe;
        this.kondisi = kondisi;
        this.keterangan = keterangan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPenghuni() {
        return penghuni;
    }

    public void setPenghuni(User penghuni) {
        this.penghuni = penghuni;
    }

    public String getNamaFasilitas() {
        return namaFasilitas;
    }

    public void setNamaFasilitas(String namaFasilitas) {
        this.namaFasilitas = namaFasilitas;
    }

    public String getMerkTipe() {
        return merkTipe;
    }

    public void setMerkTipe(String merkTipe) {
        this.merkTipe = merkTipe;
    }

    public KondisiFasilitas getKondisi() {
        return kondisi;
    }

    public void setKondisi(KondisiFasilitas kondisi) {
        this.kondisi = kondisi;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
}

