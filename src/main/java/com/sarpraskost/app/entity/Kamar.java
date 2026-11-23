package com.sarpraskost.app.entity;

import com.sarpraskost.app.entity.converter.StatusHunianConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "kamar")
public class Kamar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kamar")
    private Integer idKamar;

    @Column(name = "nomor_kamar", nullable = false, unique = true, length = 10)
    private String nomorKamar;

    @Column(name = "lantai", nullable = false, columnDefinition = "smallint")
    private Short lantai;

    @Column(name = "tipe_kamar", nullable = false, length = 30)
    private String tipeKamar;

    @Convert(converter = StatusHunianConverter.class)
    @Column(name = "status_hunian", nullable = false, length = 15)
    private StatusHunian statusHunian = StatusHunian.KOSONG;

    public Kamar() {
    }

    public Kamar(String nomorKamar, String tipeKamar, Short lantai, StatusHunian statusHunian) {
        this.nomorKamar = nomorKamar;
        this.tipeKamar = tipeKamar;
        this.lantai = lantai;
        this.statusHunian = statusHunian == null ? StatusHunian.KOSONG : statusHunian;
    }

    public Kamar(String nomorKamar, String tipeKamar, Integer lantai, StatusHunian statusHunian) {
        this(nomorKamar, tipeKamar, lantai == null ? null : lantai.shortValue(), statusHunian);
    }

    @Transient
    public boolean isFasilitasUmum() {
        return (nomorKamar != null && "UMUM".equalsIgnoreCase(nomorKamar.trim()))
            || (tipeKamar != null && tipeKamar.toLowerCase().contains("umum"));
    }

    public Integer getIdKamar() {
        return idKamar;
    }

    public void setIdKamar(Integer idKamar) {
        this.idKamar = idKamar;
    }

    public String getNomorKamar() {
        return nomorKamar;
    }

    public void setNomorKamar(String nomorKamar) {
        this.nomorKamar = nomorKamar;
    }

    public Short getLantai() {
        return lantai;
    }

    public void setLantai(Short lantai) {
        this.lantai = lantai;
    }

    public String getTipeKamar() {
        return tipeKamar;
    }

    public void setTipeKamar(String tipeKamar) {
        this.tipeKamar = tipeKamar;
    }

    public StatusHunian getStatusHunian() {
        return statusHunian;
    }

    public void setStatusHunian(StatusHunian statusHunian) {
        this.statusHunian = statusHunian;
    }
}
