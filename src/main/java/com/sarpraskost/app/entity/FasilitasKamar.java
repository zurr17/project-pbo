package com.sarpraskost.app.entity;

import com.sarpraskost.app.entity.converter.KondisiFasilitasConverter;
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
import java.time.LocalDate;

@Entity
@Table(name = "fasilitas_kamar")
public class FasilitasKamar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fasilitas")
    private Integer idFasilitas;

    @Column(name = "kode_aset", nullable = false, unique = true, length = 20)
    private String kodeAset;

    @Column(name = "nama_fasilitas", nullable = false, length = 80)
    private String namaFasilitas;

    @Column(name = "merk_tipe", length = 60)
    private String merkTipe;

    @Convert(converter = KondisiFasilitasConverter.class)
    @Column(name = "kondisi", nullable = false, length = 20)
    private KondisiFasilitas kondisi = KondisiFasilitas.BAIK;

    @Column(name = "tgl_pasang")
    private LocalDate tglPasang;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kamar", nullable = false)
    private Kamar kamar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kategori", nullable = false)
    private KategoriFasilitas kategori;

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
        this.kondisi = kondisi == null ? KondisiFasilitas.BAIK : kondisi;
        this.tglPasang = tglPasang;
    }

    public Integer getIdFasilitas() {
        return idFasilitas;
    }

    public void setIdFasilitas(Integer idFasilitas) {
        this.idFasilitas = idFasilitas;
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
}
