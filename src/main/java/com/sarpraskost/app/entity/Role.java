package com.sarpraskost.app.entity;

import java.util.Locale;

public enum Role {
    PENGHUNI("Penghuni"),
    OWNER("Pengelola");

    private final String dbValue;

    Role(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static Role fromInput(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Role tidak boleh kosong.");
        }
        String norm = raw.trim().toLowerCase(Locale.ROOT);
        if (norm.equals("penghuni") || norm.equals("pelapor")) {
            return PENGHUNI;
        }
        if (norm.equals("owner") || norm.equals("pengelola") || norm.equals("admin")) {
            return OWNER;
        }
        throw new IllegalArgumentException("Role tidak valid: " + raw);
    }
}
