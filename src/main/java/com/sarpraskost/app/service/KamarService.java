package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.KamarRequest;
import com.sarpraskost.app.dto.KamarResponse;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.StatusHunian;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.KamarRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KamarService {

    private final KamarRepository kamarRepository;
    private final FasilitasKamarRepository fasilitasRepository;
    private final PenggunaRepository penggunaRepository;

    public KamarService(
        KamarRepository kamarRepository,
        FasilitasKamarRepository fasilitasRepository,
        PenggunaRepository penggunaRepository
    ) {
        this.kamarRepository = kamarRepository;
        this.fasilitasRepository = fasilitasRepository;
        this.penggunaRepository = penggunaRepository;
    }

    @Transactional(readOnly = true)
    public List<KamarResponse> listAll() {
        return kamarRepository.findAllByOrderByGedungAscLantaiAscNomorKamarAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public KamarResponse getById(Integer id) {
        Kamar kamar = kamarRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan."));
        return toResponse(kamar);
    }

    @Transactional
    public KamarResponse create(KamarRequest req) {
        if (req == null || isBlank(req.nomorKamar()) || req.lantai() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nomor kamar dan lantai wajib diisi."
            );
        }
        if (req.lantai() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lantai harus minimal 1.");
        }

        String nomor = req.nomorKamar().trim();
        if (nomor.length() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nomor kamar maksimal 10 karakter.");
        }
        String gedung = isBlank(req.gedung())
            ? ("UMUM".equalsIgnoreCase(nomor) ? "Umum" : "Gedung 1")
            : req.gedung().trim();
        if (gedung.length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama gedung maksimal 20 karakter.");
        }
        if (kamarRepository.existsByGedungIgnoreCaseAndNomorKamarIgnoreCase(gedung, nomor)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nomor kamar sudah terdaftar di " + gedung + ".");
        }

        String defaultTipe = ("Umum".equalsIgnoreCase(gedung) || "UMUM".equalsIgnoreCase(nomor))
            ? "Fasilitas Umum"
            : "Standar";
        String tipe = isBlank(req.tipeKamar()) ? defaultTipe : req.tipeKamar().trim();

        StatusHunian status = parseStatusHunian(
            req.statusHunian(),
            "Gedung 2".equalsIgnoreCase(gedung) ? StatusHunian.PEMBANGUNAN : StatusHunian.KOSONG
        );
        String keterangan = isBlank(req.keterangan()) && status == StatusHunian.PEMBANGUNAN
            ? "On Progress Pembangunan - Perkiraan Ready Awal Juli"
            : (req.keterangan() == null ? null : req.keterangan().trim());

        Kamar kamar = new Kamar(
            gedung,
            nomor,
            tipe,
            req.lantai(),
            status,
            keterangan
        );
        return toResponse(kamarRepository.save(kamar));
    }

    @Transactional
    public KamarResponse update(Integer id, KamarRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan kamar wajib diisi.");
        }

        Kamar kamar = kamarRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan."));

        String nextGedung = kamar.getGedung();
        if (req.gedung() != null) {
            if (isBlank(req.gedung())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gedung tidak boleh kosong.");
            }
            nextGedung = req.gedung().trim();
            if (nextGedung.length() > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama gedung maksimal 20 karakter.");
            }
        }

        String nextNomor = kamar.getNomorKamar();
        if (req.nomorKamar() != null) {
            if (isBlank(req.nomorKamar())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nomor kamar tidak boleh kosong.");
            }
            nextNomor = req.nomorKamar().trim();
            if (nextNomor.length() > 10) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nomor kamar maksimal 10 karakter.");
            }
        }

        if ((!nextGedung.equalsIgnoreCase(kamar.getGedung()) || !nextNomor.equalsIgnoreCase(kamar.getNomorKamar()))
            && kamarRepository.existsByGedungIgnoreCaseAndNomorKamarIgnoreCase(nextGedung, nextNomor)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nomor kamar sudah digunakan di " + nextGedung + ".");
        }
        kamar.setGedung(nextGedung);
        kamar.setNomorKamar(nextNomor);

        if (req.tipeKamar() != null) {
            if (isBlank(req.tipeKamar())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipe kamar tidak boleh kosong.");
            }
            kamar.setTipeKamar(req.tipeKamar().trim());
        }

        if (req.lantai() != null) {
            if (req.lantai() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lantai harus minimal 1.");
            }
            kamar.setLantai(req.lantai());
        }

        if (!isBlank(req.statusHunian())) {
            kamar.setStatusHunian(parseStatusHunian(req.statusHunian(), kamar.getStatusHunian()));
        }

        if (req.keterangan() != null) {
            kamar.setKeterangan(req.keterangan());
        } else if (kamar.getStatusHunian() == StatusHunian.PEMBANGUNAN && isBlank(kamar.getKeterangan())) {
            kamar.setKeterangan("On Progress Pembangunan - Perkiraan Ready Awal Juli");
        }

        return toResponse(kamarRepository.save(kamar));
    }

    @Transactional
    public void delete(Integer id) {
        Kamar kamar = kamarRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan."));

        if (penggunaRepository.countByKamarIdKamar(id) > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Kamar tidak dapat dihapus karena masih ditempati penghuni."
            );
        }
        if (fasilitasRepository.countByKamarIdKamar(id) > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Kamar tidak dapat dihapus karena masih memiliki aset fasilitas."
            );
        }

        kamarRepository.delete(kamar);
    }

    private KamarResponse toResponse(Kamar kamar) {
        long jumlahFasilitas = fasilitasRepository.countByKamarIdKamar(kamar.getIdKamar());
        long jumlahPenghuni = penggunaRepository.countByKamarIdKamar(kamar.getIdKamar());
        return KamarResponse.from(kamar, jumlahFasilitas, jumlahPenghuni);
    }

    private StatusHunian parseStatusHunian(String raw, StatusHunian fallback) {
        if (isBlank(raw)) {
            return fallback;
        }
        try {
            return StatusHunian.fromInput(raw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Status hunian tidak valid. Pilih TERISI, KOSONG, PERBAIKAN, atau PEMBANGUNAN."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
