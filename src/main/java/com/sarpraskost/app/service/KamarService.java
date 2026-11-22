package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.KamarRequest;
import com.sarpraskost.app.dto.KamarResponse;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.StatusHunian;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.KamarRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import java.util.List;
import java.util.Locale;
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
        return kamarRepository.findAllByOrderByNomorKamarAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public KamarResponse getById(Long id) {
        Kamar kamar = kamarRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan."));
        return toResponse(kamar);
    }

    @Transactional
    public KamarResponse create(KamarRequest req) {
        if (req == null || isBlank(req.nomorKamar()) || isBlank(req.tipeKamar()) || req.lantai() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nomor kamar, tipe kamar, dan lantai wajib diisi."
            );
        }
        if (req.lantai() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lantai harus minimal 1.");
        }

        String nomor = req.nomorKamar().trim();
        if (kamarRepository.existsByNomorKamarIgnoreCase(nomor)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nomor kamar sudah terdaftar.");
        }

        Kamar kamar = new Kamar(
            nomor,
            req.tipeKamar().trim(),
            req.lantai(),
            parseStatusHunian(req.statusHunian(), StatusHunian.KOSONG)
        );
        return toResponse(kamarRepository.save(kamar));
    }

    @Transactional
    public KamarResponse update(Long id, KamarRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan kamar wajib diisi.");
        }

        Kamar kamar = kamarRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan."));

        if (req.nomorKamar() != null) {
            if (isBlank(req.nomorKamar())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nomor kamar tidak boleh kosong.");
            }
            String nextNomor = req.nomorKamar().trim();
            if (!nextNomor.equalsIgnoreCase(kamar.getNomorKamar())
                && kamarRepository.existsByNomorKamarIgnoreCase(nextNomor)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Nomor kamar sudah digunakan.");
            }
            kamar.setNomorKamar(nextNomor);
        }

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

        return toResponse(kamarRepository.save(kamar));
    }

    @Transactional
    public void delete(Long id) {
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
            return StatusHunian.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Status hunian harus TERISI, KOSONG, atau PERBAIKAN."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
