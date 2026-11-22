package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.CreatePenggunaRequest;
import com.sarpraskost.app.dto.PenggunaResponse;
import com.sarpraskost.app.dto.RegisterPenggunaRequest;
import com.sarpraskost.app.dto.UpdatePenggunaRequest;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.RiwayatPenanganan;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.entity.StatusHunian;
import com.sarpraskost.app.repository.KamarRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.NotifikasiRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import com.sarpraskost.app.repository.RiwayatPenangananRepository;
import com.sarpraskost.app.util.PasswordHasher;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PenggunaService {

    private final PenggunaRepository penggunaRepository;
    private final KamarRepository kamarRepository;
    private final LaporanKerusakanRepository laporanRepository;
    private final RiwayatPenangananRepository riwayatRepository;
    private final NotifikasiRepository notifikasiRepository;
    private final FileStorageService fileStorageService;

    public PenggunaService(
        PenggunaRepository penggunaRepository,
        KamarRepository kamarRepository,
        LaporanKerusakanRepository laporanRepository,
        RiwayatPenangananRepository riwayatRepository,
        NotifikasiRepository notifikasiRepository,
        FileStorageService fileStorageService
    ) {
        this.penggunaRepository = penggunaRepository;
        this.kamarRepository = kamarRepository;
        this.laporanRepository = laporanRepository;
        this.riwayatRepository = riwayatRepository;
        this.notifikasiRepository = notifikasiRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<PenggunaResponse> listAll(String roleParam) {
        List<Pengguna> list;
        if (!isBlank(roleParam)) {
            Role role = parseRole(roleParam);
            list = penggunaRepository.findByRoleOrderByNamaLengkapAsc(role);
        } else {
            list = penggunaRepository.findAllByOrderByIdPenggunaAsc();
        }
        return list.stream().map(PenggunaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PenggunaResponse getById(Long id) {
        Pengguna pengguna = penggunaRepository.findByIdPengguna(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan."));
        return PenggunaResponse.from(pengguna);
    }

    @Transactional
    public PenggunaResponse register(RegisterPenggunaRequest request) {
        if (request == null || isBlank(request.namaLengkap()) || isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nama lengkap, email, dan kata sandi wajib diisi."
            );
        }

        String email = request.email().trim();
        if (penggunaRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah terdaftar.");
        }

        Kamar kamar = resolveKamar(request.idKamar(), request.nomorKamar());
        Pengguna pengguna = new Pengguna(
            kamar,
            request.namaLengkap().trim(),
            email,
            PasswordHasher.hash(request.password()),
            isBlank(request.noHp()) ? null : request.noHp().trim(),
            Role.PENGHUNI
        );

        Pengguna saved = penggunaRepository.save(pengguna);
        if (kamar != null && kamar.getStatusHunian() == StatusHunian.KOSONG) {
            kamar.setStatusHunian(StatusHunian.TERISI);
            kamarRepository.save(kamar);
        }
        return PenggunaResponse.from(saved);
    }

    @Transactional
    public PenggunaResponse create(CreatePenggunaRequest request) {
        if (request == null || isBlank(request.namaLengkap()) || isBlank(request.email()) || isBlank(request.password())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nama lengkap, email, dan kata sandi wajib diisi."
            );
        }

        String email = request.email().trim();
        if (penggunaRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email sudah terdaftar.");
        }

        Role role = isBlank(request.role()) ? Role.PENGHUNI : parseRole(request.role());
        Kamar kamar = role == Role.OWNER ? null : resolveKamar(request.idKamar(), request.nomorKamar());

        Pengguna pengguna = new Pengguna(
            kamar,
            request.namaLengkap().trim(),
            email,
            PasswordHasher.hash(request.password()),
            isBlank(request.noHp()) ? null : request.noHp().trim(),
            role
        );

        Pengguna saved = penggunaRepository.save(pengguna);
        if (kamar != null && kamar.getStatusHunian() == StatusHunian.KOSONG) {
            kamar.setStatusHunian(StatusHunian.TERISI);
            kamarRepository.save(kamar);
        }
        return PenggunaResponse.from(saved);
    }

    @Transactional
    public PenggunaResponse update(Long id, UpdatePenggunaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan profil wajib diisi.");
        }

        Pengguna pengguna = penggunaRepository.findByIdPengguna(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan."));

        if (request.namaLengkap() != null) {
            if (isBlank(request.namaLengkap())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama lengkap tidak boleh kosong.");
            }
            pengguna.setNamaLengkap(request.namaLengkap().trim());
        }

        if (request.noHp() != null) {
            pengguna.setNoHp(isBlank(request.noHp()) ? null : request.noHp().trim());
        }

        if (!isBlank(request.role())) {
            Role nextRole = parseRole(request.role());
            pengguna.setRole(nextRole);
            if (nextRole == Role.OWNER) {
                pengguna.setKamar(null);
            }
        }

        Kamar oldKamar = pengguna.getKamar();
        if (pengguna.getRole() == Role.PENGHUNI && (request.idKamar() != null || request.nomorKamar() != null)) {
            Kamar nextKamar = resolveKamar(request.idKamar(), request.nomorKamar());
            pengguna.setKamar(nextKamar);
            if (nextKamar != null && nextKamar.getStatusHunian() == StatusHunian.KOSONG) {
                nextKamar.setStatusHunian(StatusHunian.TERISI);
                kamarRepository.save(nextKamar);
            }
        }

        if (!isBlank(request.password())) {
            pengguna.setPasswordHash(PasswordHasher.hash(request.password()));
        }

        Pengguna saved = penggunaRepository.save(pengguna);
        penggunaRepository.flush();

        if (oldKamar != null && (saved.getKamar() == null || !oldKamar.getIdKamar().equals(saved.getKamar().getIdKamar()))) {
            if (penggunaRepository.countByKamarIdKamar(oldKamar.getIdKamar()) == 0
                && oldKamar.getStatusHunian() == StatusHunian.TERISI) {
                oldKamar.setStatusHunian(StatusHunian.KOSONG);
                kamarRepository.save(oldKamar);
            }
        }

        return PenggunaResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Pengguna pengguna = penggunaRepository.findByIdPengguna(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan."));

        if (pengguna.getRole() == Role.OWNER) {
            if (penggunaRepository.countByRole(Role.OWNER) <= 1) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Akun Owner terakhir tidak dapat dihapus."
                );
            }
            if (riwayatRepository.countByPenggunaIdPengguna(id) > 0) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Akun Owner yang sudah memproses laporan tidak dapat dihapus."
                );
            }
        }

        Kamar oldKamar = pengguna.getKamar();
        List<LaporanKerusakan> ownedReports = laporanRepository.findByPenggunaIdPengguna(id);
        List<String> filesToDelete = new ArrayList<>();

        for (LaporanKerusakan laporan : ownedReports) {
            if (laporan.getFotoUrl() != null) {
                filesToDelete.add(laporan.getFotoUrl());
            }
            List<RiwayatPenanganan> riwayatList =
                riwayatRepository.findByLaporanIdLaporanOrderByWaktuUpdateAscIdRiwayatAsc(laporan.getIdLaporan());
            for (RiwayatPenanganan r : riwayatList) {
                if (r.getFotoSesudahUrl() != null) {
                    filesToDelete.add(r.getFotoSesudahUrl());
                }
            }
            notifikasiRepository.deleteByLaporanIdLaporan(laporan.getIdLaporan());
            riwayatRepository.deleteByLaporanIdLaporan(laporan.getIdLaporan());
        }

        notifikasiRepository.deleteByPenggunaIdPengguna(id);
        laporanRepository.deleteAll(ownedReports);
        penggunaRepository.delete(pengguna);
        penggunaRepository.flush();

        if (oldKamar != null && penggunaRepository.countByKamarIdKamar(oldKamar.getIdKamar()) == 0
            && oldKamar.getStatusHunian() == StatusHunian.TERISI) {
            oldKamar.setStatusHunian(StatusHunian.KOSONG);
            kamarRepository.save(oldKamar);
        }

        for (String url : filesToDelete) {
            fileStorageService.deleteFoto(url);
        }
    }

    private Kamar resolveKamar(Long idKamar, String nomorKamar) {
        if (idKamar != null) {
            return kamarRepository.findById(idKamar)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan."));
        }
        if (!isBlank(nomorKamar)) {
            return kamarRepository.findByNomorKamarIgnoreCase(nomorKamar.trim())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Kamar dengan nomor " + nomorKamar.trim() + " tidak ditemukan."
                ));
        }
        return null;
    }

    private Role parseRole(String raw) {
        try {
            return Role.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Role tidak valid. Hanya PENGHUNI atau OWNER yang diizinkan."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
