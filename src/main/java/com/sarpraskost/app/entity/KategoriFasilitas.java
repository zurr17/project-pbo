package com.sarpraskost.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kategori_fasilitas")
public class KategoriFasilitas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kategori")
    private Integer idKategori;

    @Column(name = "nama_kategori", nullable = false, unique = true, length = 50)
    private String namaKategori;

    @Column(name = "deskripsi", columnDefinition = "text")
    private String deskripsi;

    public KategoriFasilitas() {
    }

    public KategoriFasilitas(String namaKategori, String deskripsi) {
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
    }

    public Integer getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(Integer idKategori) {
        this.idKategori = idKategori;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
}
