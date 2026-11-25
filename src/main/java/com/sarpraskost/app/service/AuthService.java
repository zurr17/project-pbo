package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.LoginRequest;
import com.sarpraskost.app.dto.PenggunaResponse;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.repository.PenggunaRepository;
import com.sarpraskost.app.util.PasswordHasher;
import com.sarpraskost.app.util.SqlInjectionValidator;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final PenggunaRepository penggunaRepository;
    private final LoginAttemptService loginAttemptService;

    public AuthService(
        PenggunaRepository penggunaRepository,
        LoginAttemptService loginAttemptService
    ) {
        this.penggunaRepository = penggunaRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional(readOnly = true)
    public PenggunaResponse login(LoginRequest request) {
        return login(request, "unknown");
    }

    @Transactional(readOnly = true)
    public PenggunaResponse login(LoginRequest request, String clientIp) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Email atau nomor kamar dan kata sandi wajib diisi."
            );
        }

        String identifier = request.email().trim();
        SqlInjectionValidator.requireSafe(identifier, "Email atau nomor kamar");
        if (identifier.length() > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Email atau nomor kamar maksimal 100 karakter."
            );
        }

        loginAttemptService.checkAllowed(clientIp, identifier);

        Optional<Pengguna> penggunaOpt = penggunaRepository.findByEmailIgnoreCase(identifier);
        if (penggunaOpt.isEmpty() && !identifier.contains("@")) {
            penggunaOpt = penggunaRepository.findFirstByKamarNomorKamarIgnoreCaseAndRole(identifier, Role.PENGHUNI);
        }

        if (penggunaOpt.isEmpty()) {
            throw buildInvalidCredentialsException(clientIp, identifier);
        }

        Pengguna pengguna = penggunaOpt.get();
        if (!PasswordHasher.matches(request.password(), pengguna.getPasswordHash())) {
            throw buildInvalidCredentialsException(clientIp, identifier);
        }

        String nomorKamar = pengguna.getKamar() != null ? pengguna.getKamar().getNomorKamar() : null;
        loginAttemptService.recordSuccess(clientIp, identifier, pengguna.getEmail(), nomorKamar);

        return PenggunaResponse.from(pengguna);
    }

    private ResponseStatusException buildInvalidCredentialsException(String clientIp, String identifier) {
        int remaining = loginAttemptService.recordFailureAndGetRemaining(clientIp, identifier);
        if (remaining <= 0) {
            return new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Terlalu banyak percobaan login gagal (" + LoginAttemptService.MAX_ACCOUNT_ATTEMPTS
                    + " kali). Akses login dikunci sementara selama 5 menit."
            );
        }
        return new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Email/nomor kamar atau kata sandi salah. Sisa percobaan: " + remaining + " kali sebelum dikunci sementara."
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
