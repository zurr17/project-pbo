package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.LoginRequest;
import com.sarpraskost.app.dto.PenggunaResponse;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.repository.PenggunaRepository;
import com.sarpraskost.app.util.PasswordHasher;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final PenggunaRepository penggunaRepository;

    public AuthService(PenggunaRepository penggunaRepository) {
        this.penggunaRepository = penggunaRepository;
    }

    @Transactional(readOnly = true)
    public PenggunaResponse login(LoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Email atau nomor kamar dan kata sandi wajib diisi."
            );
        }

        String identifier = request.email().trim();
        Optional<Pengguna> penggunaOpt = penggunaRepository.findByEmailIgnoreCase(identifier);
        if (penggunaOpt.isEmpty() && !identifier.contains("@")) {
            penggunaOpt = penggunaRepository.findFirstByKamarNomorKamarIgnoreCaseAndRole(identifier, Role.PENGHUNI);
        }

        Pengguna pengguna = penggunaOpt.orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Email/nomor kamar atau kata sandi salah."
        ));

        if (!PasswordHasher.matches(request.password(), pengguna.getPasswordHash())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Email/nomor kamar atau kata sandi salah."
            );
        }

        return PenggunaResponse.from(pengguna);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
