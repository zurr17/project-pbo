package com.sarpraskost.app.entity;

import java.util.Locale;

public enum StatusTiket {
    MENUNGGU("Menunggu"),
    DIPROSES("Diproses"),
    SELESAI("Selesai"),
    DITOLAK("Ditolak");

    private final String dbValue;

    StatusTiket(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static StatusTiket fromInput(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Status tiket tidak boleh kosong.");
        }
        String norm = raw.trim().toLowerCase(Locale.ROOT);
        return switch (norm) {
            case "menunggu" -> MENUNGGU;
            case "diproses" -> DIPROSES;
            case "selesai" -> SELESAI;
            case "ditolak" -> DITOLAK;
            default -> throw new IllegalArgumentException("Status tiket tidak valid: " + raw);
        };
    }
}
