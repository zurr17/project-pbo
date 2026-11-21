package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.LoginRequest;
import com.sarpraskost.app.dto.UserResponse;
import com.sarpraskost.app.entity.User;
import com.sarpraskost.app.repository.UserRepository;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Email atau nomor kamar dan kata sandi wajib diisi."
            );
        }

        String identifier = request.email().trim();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(identifier);
        if (userOpt.isEmpty() && !identifier.contains("@")) {
            userOpt = userRepository.findFirstByNomorKamarIgnoreCase(identifier);
        }

        User user = userOpt.orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Email/nomor kamar atau kata sandi salah."
        ));

        if (!user.getPassword().equals(request.password())) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Email/nomor kamar atau kata sandi salah."
            );
        }

        return UserResponse.from(user);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
