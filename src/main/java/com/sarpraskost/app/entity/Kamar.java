package com.sarpraskost.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kamar")
public class Kamar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kamar")
    private Long idKamar;

    @Column(name = "nomor_kamar", nullable = false, unique = true, length = 30)
    private String nomorKamar;

    @Column(name = "tipe_kamar", nullable = false, length = 60)
    private String tipeKamar;

    @Column(name = "lantai", nullable = false)
    private Integer lantai;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_hunian", nullable = false, length = 30)
    private StatusHunian statusHunian;

    public Kamar() {
    }

    public Kamar(String nomorKamar, String tipeKamar, Integer lantai, StatusHunian statusHunian) {
        this.nomorKamar = nomorKamar;
        this.tipeKamar = tipeKamar;
        this.lantai = lantai;
        this.statusHunian = statusHunian;
    }

    public Long getIdKamar() {
        return idKamar;
    }

    public void setIdKamar(Long idKamar) {
        this.idKamar = idKamar;
    }

    public String getNomorKamar() {
        return nomorKamar;
    }

    public void setNomorKamar(String nomorKamar) {
        this.nomorKamar = nomorKamar;
    }

    public String getTipeKamar() {
        return tipeKamar;
    }

    public void setTipeKamar(String tipeKamar) {
        this.tipeKamar = tipeKamar;
    }

    public Integer getLantai() {
        return lantai;
    }

    public void setLantai(Integer lantai) {
        this.lantai = lantai;
    }

    public StatusHunian getStatusHunian() {
        return statusHunian;
    }

    public void setStatusHunian(StatusHunian statusHunian) {
        this.statusHunian = statusHunian;
    }
}
