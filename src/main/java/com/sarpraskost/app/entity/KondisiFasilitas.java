package com.sarpraskost.app.entity;

import java.util.Locale;

public enum KondisiFasilitas {
    BAIK("Baik"),
    RUSAK_RINGAN("Rusak Ringan"),
    RUSAK_BERAT("Rusak Berat"),
    DALAM_PERBAIKAN("Dalam Perbaikan");

    private final String dbValue;

    KondisiFasilitas(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static KondisiFasilitas fromInput(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Kondisi fasilitas tidak boleh kosong.");
        }
        String norm = raw.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
        return switch (norm) {
            case "baik" -> BAIK;
            case "rusak ringan", "perlu perawatan" -> RUSAK_RINGAN;
            case "rusak berat", "rusak" -> RUSAK_BERAT;
            case "dalam perbaikan" -> DALAM_PERBAIKAN;
            default -> throw new IllegalArgumentException("Kondisi fasilitas tidak valid: " + raw);
        };
    }
}
