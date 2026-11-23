package com.sarpraskost.app.entity;

import com.sarpraskost.app.entity.converter.RoleConverter;
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

@Entity
@Table(name = "pengguna")
public class Pengguna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pengguna")
    private Integer idPengguna;

    @Column(name = "nama_lengkap", nullable = false, length = 80)
    private String namaLengkap;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "no_hp", nullable = false, unique = true, length = 20)
    private String noHp;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Convert(converter = RoleConverter.class)
    @Column(name = "role", nullable = false, length = 15)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kamar")
    private Kamar kamar;

    public Pengguna() {
    }

    public Pengguna(
        Kamar kamar,
        String namaLengkap,
        String email,
        String passwordHash,
        String noHp,
        Role role
    ) {
        this.kamar = kamar;
        this.namaLengkap = namaLengkap;
        this.email = email;
        this.passwordHash = passwordHash;
        this.noHp = noHp;
        this.role = role;
    }

    public Integer getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(Integer idPengguna) {
        this.idPengguna = idPengguna;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Kamar getKamar() {
        return kamar;
    }

    public void setKamar(Kamar kamar) {
        this.kamar = kamar;
    }
}
