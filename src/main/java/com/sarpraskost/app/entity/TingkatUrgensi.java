package com.sarpraskost.app.entity;

import java.util.Locale;

public enum TingkatUrgensi {
    RENDAH("Rendah"),
    SEDANG("Sedang"),
    TINGGI("Tinggi"),
    DARURAT("Darurat");

    private final String dbValue;

    TingkatUrgensi(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static TingkatUrgensi fromInput(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Tingkat urgensi tidak boleh kosong.");
        }
        String norm = raw.trim().toLowerCase(Locale.ROOT);
        return switch (norm) {
            case "rendah" -> RENDAH;
            case "sedang" -> SEDANG;
            case "tinggi" -> TINGGI;
            case "darurat" -> DARURAT;
            default -> throw new IllegalArgumentException("Tingkat urgensi tidak valid: " + raw);
        };
    }
}
