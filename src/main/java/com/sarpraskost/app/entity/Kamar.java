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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "kamar",
    uniqueConstraints = {
        @UniqueConstraint(name = "kamar_gedung_nomor_key", columnNames = {"gedung", "nomor_kamar"})
    }
)
public class Kamar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kamar")
    private Integer idKamar;

    @Column(name = "gedung", nullable = false, length = 20)
    private String gedung = "Gedung 1";

    @Column(name = "nomor_kamar", nullable = false, length = 10)
    private String nomorKamar;

    @Column(name = "lantai", nullable = false, columnDefinition = "smallint")
    private Short lantai;

    @Column(name = "tipe_kamar", nullable = false, length = 30)
    private String tipeKamar;

    @Convert(converter = StatusHunianConverter.class)
    @Column(name = "status_hunian", nullable = false, length = 15)
    private StatusHunian statusHunian = StatusHunian.KOSONG;

    @Column(name = "keterangan", columnDefinition = "text")
    private String keterangan;

    public Kamar() {
    }

    public Kamar(String nomorKamar, String tipeKamar, Short lantai, StatusHunian statusHunian) {
        this("Gedung 1", nomorKamar, tipeKamar, lantai, statusHunian, null);
    }

    public Kamar(String nomorKamar, String tipeKamar, Integer lantai, StatusHunian statusHunian) {
        this("Gedung 1", nomorKamar, tipeKamar, lantai == null ? null : lantai.shortValue(), statusHunian, null);
    }

    public Kamar(
        String gedung,
        String nomorKamar,
        String tipeKamar,
        Short lantai,
        StatusHunian statusHunian,
        String keterangan
    ) {
        this.gedung = (gedung == null || gedung.trim().isEmpty()) ? "Gedung 1" : gedung.trim();
        this.nomorKamar = nomorKamar;
        this.tipeKamar = tipeKamar;
        this.lantai = lantai;
        this.statusHunian = statusHunian == null ? StatusHunian.KOSONG : statusHunian;
        this.keterangan = (keterangan == null || keterangan.trim().isEmpty()) ? null : keterangan.trim();
    }

    @Transient
    public boolean isFasilitasUmum() {
        return (nomorKamar != null && "UMUM".equalsIgnoreCase(nomorKamar.trim()))
            || (tipeKamar != null && tipeKamar.toLowerCase().contains("umum"))
            || (gedung != null && "UMUM".equalsIgnoreCase(gedung.trim()));
    }

    @Transient
    public boolean isTersediaUntukPenghuni() {
        return !isFasilitasUmum()
            && "Gedung 1".equalsIgnoreCase(gedung)
            && statusHunian != StatusHunian.PEMBANGUNAN
            && statusHunian != StatusHunian.PERBAIKAN;
    }

    public Integer getIdKamar() {
        return idKamar;
    }

    public void setIdKamar(Integer idKamar) {
        this.idKamar = idKamar;
    }

    public String getGedung() {
        return gedung;
    }

    public void setGedung(String gedung) {
        this.gedung = (gedung == null || gedung.trim().isEmpty()) ? "Gedung 1" : gedung.trim();
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

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = (keterangan == null || keterangan.trim().isEmpty()) ? null : keterangan.trim();
    }
}
