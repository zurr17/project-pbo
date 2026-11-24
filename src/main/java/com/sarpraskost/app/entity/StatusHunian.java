package com.sarpraskost.app.entity;

import java.util.Locale;

public enum StatusHunian {
    TERISI("Terisi"),
    KOSONG("Kosong"),
    PERBAIKAN("Perbaikan"),
    PEMBANGUNAN("Pembangunan");

    private final String dbValue;

    StatusHunian(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static StatusHunian fromInput(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Status hunian tidak boleh kosong.");
        }
        String norm = raw.trim().toLowerCase(Locale.ROOT);
        return switch (norm) {
            case "terisi" -> TERISI;
            case "kosong" -> KOSONG;
            case "perbaikan" -> PERBAIKAN;
            case "pembangunan", "on progress", "on_progress", "on progress pembangunan", "dalam pembangunan" -> PEMBANGUNAN;
            default -> throw new IllegalArgumentException("Status hunian tidak valid: " + raw);
        };
    }
}
