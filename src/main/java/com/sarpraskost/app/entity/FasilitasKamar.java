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
import java.time.LocalDate;

@Entity
@Table(name = "fasilitas_kamar")
public class FasilitasKamar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fasilitas")
    private Long idFasilitas;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kamar", nullable = false)
    private Kamar kamar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kategori", nullable = false)
    private KategoriFasilitas kategori;

    @Column(name = "kode_aset", nullable = false, unique = true, length = 50)
    private String kodeAset;

    @Column(name = "nama_fasilitas", nullable = false, length = 120)
    private String namaFasilitas;

    @Column(name = "merk_tipe", length = 120)
    private String merkTipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "kondisi", nullable = false, length = 30)
    private KondisiFasilitas kondisi;

    @Column(name = "tgl_pasang")
    private LocalDate tglPasang;

    public FasilitasKamar() {
    }

    public FasilitasKamar(
        Kamar kamar,
        KategoriFasilitas kategori,
        String kodeAset,
        String namaFasilitas,
        String merkTipe,
        KondisiFasilitas kondisi,
        LocalDate tglPasang
    ) {
        this.kamar = kamar;
        this.kategori = kategori;
        this.kodeAset = kodeAset;
        this.namaFasilitas = namaFasilitas;
        this.merkTipe = merkTipe;
        this.kondisi = kondisi;
        this.tglPasang = tglPasang;
    }

    public Long getIdFasilitas() {
        return idFasilitas;
    }

    public void setIdFasilitas(Long idFasilitas) {
        this.idFasilitas = idFasilitas;
    }

    public Kamar getKamar() {
        return kamar;
    }

    public void setKamar(Kamar kamar) {
        this.kamar = kamar;
    }

    public KategoriFasilitas getKategori() {
        return kategori;
    }

    public void setKategori(KategoriFasilitas kategori) {
        this.kategori = kategori;
    }

    public String getKodeAset() {
        return kodeAset;
    }

    public void setKodeAset(String kodeAset) {
        this.kodeAset = kodeAset;
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

    public LocalDate getTglPasang() {
        return tglPasang;
    }

    public void setTglPasang(LocalDate tglPasang) {
        this.tglPasang = tglPasang;
    }
}
