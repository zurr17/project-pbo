package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.LoginRequest;
import com.sarpraskost.app.dto.PenggunaResponse;
import com.sarpraskost.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<PenggunaResponse> login(
        @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        String clientIp = resolveClientIp(httpRequest);
        return ResponseEntity.ok(authService.login(request, clientIp));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (isTrustedProxy(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.trim().isEmpty()) {
                String firstHop = forwarded.split(",")[0].trim();
                if (!firstHop.isEmpty()) {
                    return firstHop;
                }
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.trim().isEmpty()) {
                return realIp.trim();
            }
        }
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    private boolean isTrustedProxy(String remoteAddr) {
        return "127.0.0.1".equals(remoteAddr)
            || "::1".equals(remoteAddr)
            || "0:0:0:0:0:0:0:1".equals(remoteAddr);
    }
}
