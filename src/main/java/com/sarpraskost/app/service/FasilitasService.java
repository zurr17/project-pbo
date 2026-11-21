package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.FasilitasRequest;
import com.sarpraskost.app.dto.FasilitasResponse;
import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.KondisiFasilitas;
import com.sarpraskost.app.entity.User;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FasilitasService {

    private final FasilitasKamarRepository fasilitasRepo;
    private final UserRepository userRepo;

    public FasilitasService(FasilitasKamarRepository fasilitasRepo, UserRepository userRepo) {
        this.fasilitasRepo = fasilitasRepo;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public List<FasilitasResponse> listByPenghuni(Long penghuniId) {
        if (!userRepo.existsById(penghuniId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Penghuni tidak ditemukan.");
        }
        return fasilitasRepo.findByPenghuniIdOrderByIdDesc(penghuniId)
            .stream()
            .map(FasilitasResponse::from)
            .toList();
    }

    @Transactional
    public FasilitasResponse create(FasilitasRequest req) {
        if (req == null || req.penghuniId() == null || isBlank(req.namaFasilitas())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "ID penghuni dan nama fasilitas wajib diisi."
            );
        }
        User penghuni = userRepo.findById(req.penghuniId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Penghuni tidak ditemukan."));

        FasilitasKamar entity = new FasilitasKamar();
        entity.setPenghuni(penghuni);
        entity.setNamaFasilitas(req.namaFasilitas().trim());
        entity.setMerkTipe(req.merkTipe() != null ? req.merkTipe().trim() : "");
        entity.setKondisi(parseKondisi(req.kondisi(), KondisiFasilitas.BAIK));
        entity.setKeterangan(req.keterangan() != null ? req.keterangan().trim() : "");

        return FasilitasResponse.from(fasilitasRepo.save(entity));
    }

    @Transactional
    public FasilitasResponse update(Long id, FasilitasRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan fasilitas wajib diisi.");
        }
        FasilitasKamar entity = fasilitasRepo.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas tidak ditemukan."));

        if (!isBlank(req.namaFasilitas())) {
            entity.setNamaFasilitas(req.namaFasilitas().trim());
        }
        if (req.merkTipe() != null) {
            entity.setMerkTipe(req.merkTipe().trim());
        }
        if (!isBlank(req.kondisi())) {
            entity.setKondisi(parseKondisi(req.kondisi(), entity.getKondisi()));
        }
        if (req.keterangan() != null) {
            entity.setKeterangan(req.keterangan().trim());
        }

        return FasilitasResponse.from(fasilitasRepo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!fasilitasRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas tidak ditemukan.");
        }
        fasilitasRepo.deleteById(id);
    }

    private KondisiFasilitas parseKondisi(String raw, KondisiFasilitas fallback) {
        if (isBlank(raw)) {
            return fallback;
        }
        try {
            return KondisiFasilitas.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Kondisi harus BAIK, PERLU_PERAWATAN, atau RUSAK."
            );
        }
    }

    private boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }
}
