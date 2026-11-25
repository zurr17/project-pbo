package com.sarpraskost.app.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Validator pertahanan berlapis (defense-in-depth) terhadap serangan SQL Injection
 * pada parameter URL, payload JSON, serta form input aplikasi.
 */
public final class SqlInjectionValidator {

    private static final List<Pattern> SQLI_PATTERNS = List.of(
        // Stacked queries: ; DROP TABLE, ; SELECT, ; DELETE, dll.
        Pattern.compile(
            "(?i);\\s*(select|insert|update|delete|drop|alter|truncate|create|replace|exec|execute|grant|revoke|copy|vacuum|comment|attach|pragma)\\b"
        ),
        // UNION-based SQL injection
        Pattern.compile("(?i)\\bunion\\s+(all\\s+)?select\\b"),
        // Quote + boolean tautology bypass: ' OR '1'='1, ' OR 1=1, " OR TRUE
        Pattern.compile(
            "(?i)['\"`]\\s*(or|and)\\s+(['\"`]?[a-z0-9_]+['\"`]?\\s*(=|!=|<>|<|>|like|is)\\s*['\"`]?[a-z0-9_%]+['\"`]?|true|false|\\d+\\s*=\\s*\\d+)"
        ),
        // Unquoted OR 1=1-- atau '='
        Pattern.compile("(?i)(\\bor\\s+\\d+\\s*=\\s*\\d+(\\s*--|\\s*#|\\s*$|\\s*;)|'\\s*=\\s*'|\"\\s*=\\s*\")"),
        // Komentar SQL setelah tanda kutip atau disertai kata kunci SQL
        Pattern.compile("(?i)(['\"`]\\s*(--|#|/\\*)|/\\*[\\s\\S]*?\\*/\\s*(or|and|select|union|drop|alter))"),
        // Fungsi time-based blind SQLi & skema internal PostgreSQL/MySQL
        Pattern.compile(
            "(?i)\\b(pg_sleep\\s*\\(|sleep\\s*\\(|benchmark\\s*\\(|waitfor\\s+delay\\b|information_schema\\.\\w+|pg_catalog\\.\\w+|load_file\\s*\\(|into\\s+(outfile|dumpfile)|xp_cmdshell|dbms_\\w+\\.\\w+|utl_\\w+\\.\\w+)"
        ),
        // Klausa SQL DML/DDL eksplisit
        Pattern.compile(
            "(?i)\\b(select\\s+[\\w*,.'\"()\\s]+\\s+from\\s+\\w+|insert\\s+into\\s+\\w+|delete\\s+from\\s+\\w+|drop\\s+(table|database|schema|view|index)\\s+\\w+|truncate\\s+table\\s+\\w+|alter\\s+table\\s+\\w+)\\b"
        )
    );

    private SqlInjectionValidator() {
    }

    public static boolean containsSqlInjection(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        if (value.indexOf('\0') >= 0) {
            return true;
        }
        String normalized = value.trim();
        for (Pattern pattern : SQLI_PATTERNS) {
            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    public static void requireSafe(String value, String fieldLabel) {
        if (containsSqlInjection(value)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Input pada " + fieldLabel + " terdeteksi mengandung pola berbahaya (SQL Injection) dan ditolak oleh sistem."
            );
        }
    }

    public static void validateRawQueryString(String rawQueryString) {
        if (rawQueryString == null || rawQueryString.isEmpty()) {
            return;
        }
        for (String pair : rawQueryString.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int idx = pair.indexOf('=');
            String rawKey = idx >= 0 ? pair.substring(0, idx) : pair;
            String rawVal = idx >= 0 ? pair.substring(idx + 1) : "";
            String decodedKey = decodeQuietly(rawKey);
            String decodedVal = decodeQuietly(rawVal);
            requireSafe(decodedKey, "nama parameter URL");
            requireSafe(decodedVal, "parameter '" + decodedKey + "'");
        }
    }

    private static String decodeQuietly(String raw) {
        try {
            return URLDecoder.decode(raw, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return raw;
        }
    }
}
