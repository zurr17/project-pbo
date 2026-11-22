package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.KategoriFasilitasRequest;
import com.sarpraskost.app.dto.KategoriFasilitasResponse;
import com.sarpraskost.app.entity.KategoriFasilitas;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.KategoriFasilitasRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KategoriFasilitasService {

    private final KategoriFasilitasRepository kategoriRepository;
    private final FasilitasKamarRepository fasilitasRepository;

    public KategoriFasilitasService(
        KategoriFasilitasRepository kategoriRepository,
        FasilitasKamarRepository fasilitasRepository
    ) {
        this.kategoriRepository = kategoriRepository;
        this.fasilitasRepository = fasilitasRepository;
    }

    @Transactional(readOnly = true)
    public List<KategoriFasilitasResponse> listAll() {
        return kategoriRepository.findAllByOrderByNamaKategoriAsc().stream()
            .map(k -> KategoriFasilitasResponse.from(k, fasilitasRepository.countByKategoriIdKategori(k.getIdKategori())))
            .toList();
    }

    @Transactional(readOnly = true)
    public KategoriFasilitasResponse getById(Long id) {
        KategoriFasilitas kategori = kategoriRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori fasilitas tidak ditemukan."));
        return KategoriFasilitasResponse.from(kategori, fasilitasRepository.countByKategoriIdKategori(id));
    }

    @Transactional
    public KategoriFasilitasResponse create(KategoriFasilitasRequest req) {
        if (req == null || isBlank(req.namaKategori())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama kategori wajib diisi.");
        }
        String nama = req.namaKategori().trim();
        if (kategoriRepository.findByNamaKategoriIgnoreCase(nama).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nama kategori sudah ada.");
        }

        KategoriFasilitas kategori = new KategoriFasilitas(
            nama,
            req.deskripsi() != null ? req.deskripsi().trim() : ""
        );
        KategoriFasilitas saved = kategoriRepository.save(kategori);
        return KategoriFasilitasResponse.from(saved, 0);
    }

    @Transactional
    public KategoriFasilitasResponse update(Long id, KategoriFasilitasRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan kategori wajib diisi.");
        }
        KategoriFasilitas kategori = kategoriRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori fasilitas tidak ditemukan."));

        if (req.namaKategori() != null) {
            if (isBlank(req.namaKategori())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama kategori tidak boleh kosong.");
            }
            String nextNama = req.namaKategori().trim();
            if (!nextNama.equalsIgnoreCase(kategori.getNamaKategori())
                && kategoriRepository.findByNamaKategoriIgnoreCase(nextNama).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Nama kategori sudah digunakan.");
            }
            kategori.setNamaKategori(nextNama);
        }

        if (req.deskripsi() != null) {
            kategori.setDeskripsi(req.deskripsi().trim());
        }

        KategoriFasilitas saved = kategoriRepository.save(kategori);
        return KategoriFasilitasResponse.from(saved, fasilitasRepository.countByKategoriIdKategori(id));
    }

    @Transactional
    public void delete(Long id) {
        KategoriFasilitas kategori = kategoriRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori fasilitas tidak ditemukan."));
        if (fasilitasRepository.countByKategoriIdKategori(id) > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Kategori tidak dapat dihapus karena masih digunakan oleh aset fasilitas."
            );
        }
        kategoriRepository.delete(kategori);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
