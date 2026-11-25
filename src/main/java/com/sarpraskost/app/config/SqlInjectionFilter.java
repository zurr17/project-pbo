package com.sarpraskost.app.config;

import com.sarpraskost.app.util.SqlInjectionValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Filter servlet untuk memvalidasi parameter URL dan payload JSON pada endpoint /api/**
 * guna menolak indikasi serangan SQL Injection sejak di lapisan request terluar.
 */
@Component
public class SqlInjectionFilter extends OncePerRequestFilter {

    private static final int MAX_INSPECTABLE_JSON_BYTES = 1024 * 1024;
    private static final Set<String> PASSWORD_FIELDS = Set.of(
        "password",
        "password_hash",
        "confirm_password",
        "confirmpassword",
        "kata_sandi"
    );

    private final ObjectMapper objectMapper;

    public SqlInjectionFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            SqlInjectionValidator.validateRawQueryString(request.getQueryString());
        } catch (ResponseStatusException ex) {
            writeErrorResponse(response, ex.getReason());
            return;
        }

        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE)) {
            byte[] bodyBytes = request.getInputStream().readNBytes(MAX_INSPECTABLE_JSON_BYTES + 1);
            if (bodyBytes.length > MAX_INSPECTABLE_JSON_BYTES) {
                writeErrorResponse(response, "Ukuran payload permintaan melebihi batas maksimal.");
                return;
            }
            if (bodyBytes.length > 0) {
                try {
                    JsonNode root = objectMapper.readTree(bodyBytes);
                    inspectJsonNode(root, "");
                } catch (ResponseStatusException ex) {
                    writeErrorResponse(response, ex.getReason());
                    return;
                } catch (RuntimeException ignored) {
                    // JSON malformed akan ditangani oleh GlobalExceptionHandler (HttpMessageNotReadableException)
                }
            }
            CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request, bodyBytes);
            filterChain.doFilter(wrapped, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void inspectJsonNode(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                String key = entry.getKey() == null ? "" : entry.getKey();
                if (PASSWORD_FIELDS.contains(key.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                inspectJsonNode(entry.getValue(), key);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                inspectJsonNode(item, fieldName);
            }
            return;
        }
        if (node.isString()) {
            String label = fieldName.isEmpty() ? "payload" : "field '" + fieldName + "'";
            SqlInjectionValidator.requireSafe(node.asString(), label);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String safeMsg = message == null
            ? "Permintaan ditolak karena terdeteksi pola berbahaya."
            : message;
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", safeMsg)));
    }

    private static final class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        private CachedBodyHttpServletRequest(HttpServletRequest request, byte[] cachedBody) {
            super(request);
            this.cachedBody = cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return bais.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Synchronous cached stream
                }

                @Override
                public int read() {
                    return bais.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
