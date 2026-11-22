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
    private Long idKategori;

    @Column(name = "nama_kategori", nullable = false, unique = true, length = 100)
    private String namaKategori;

    @Column(name = "deskripsi", columnDefinition = "TEXT")
    private String deskripsi;

    public KategoriFasilitas() {
    }

    public KategoriFasilitas(String namaKategori, String deskripsi) {
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
    }

    public Long getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(Long idKategori) {
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
