package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.RegisterUserRequest;
import com.sarpraskost.app.dto.UpdateUserRequest;
import com.sarpraskost.app.dto.UserResponse;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.entity.User;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FasilitasKamarRepository fasilitasKamarRepository;
    private final LaporanKerusakanRepository laporanKerusakanRepository;
    private final FileStorageService fileStorageService;

    public UserService(
        UserRepository userRepository,
        FasilitasKamarRepository fasilitasKamarRepository,
        LaporanKerusakanRepository laporanKerusakanRepository,
        FileStorageService fileStorageService
    ) {
        this.userRepository = userRepository;
        this.fasilitasKamarRepository = fasilitasKamarRepository;
        this.laporanKerusakanRepository = laporanKerusakanRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (request == null || isBlank(request.nama()) || isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nama, email, dan kata sandi wajib diisi."
            );
        }

        String email = request.email().trim();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email sudah terdaftar."
            );
        }

        String nomorKamar = isBlank(request.nomorKamar()) ? null : request.nomorKamar().trim();
        User user = new User(
            request.nama().trim(),
            email,
            request.password(),
            Role.PENGHUNI,
            nomorKamar
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getMekanikList() {
        return userRepository.findByRoleOrderByNamaAsc(Role.MEKANIK).stream()
            .map(UserResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan."));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan profil wajib diisi.");
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan."));

        if (request.nama() != null) {
            if (isBlank(request.nama())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama tidak boleh kosong.");
            }
            user.setNama(request.nama().trim());
        }

        if (request.nomorKamar() != null) {
            user.setNomorKamar(isBlank(request.nomorKamar()) ? null : request.nomorKamar().trim());
        }

        if (!isBlank(request.password())) {
            user.setPassword(request.password());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan."));

        List<String> fotoUrls = laporanKerusakanRepository.findByPenghuniIdOrderByTanggalLaporDescIdDesc(id)
            .stream()
            .map(LaporanKerusakan::getFotoUrl)
            .toList();

        fasilitasKamarRepository.deleteByPenghuniId(id);
        laporanKerusakanRepository.deleteByPenghuniId(id);
        userRepository.delete(user);
        userRepository.flush();

        for (String fotoUrl : fotoUrls) {
            fileStorageService.deleteFoto(fotoUrl);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
