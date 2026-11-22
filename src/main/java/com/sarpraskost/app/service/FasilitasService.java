package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.FasilitasRequest;
import com.sarpraskost.app.dto.FasilitasResponse;
import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.KategoriFasilitas;
import com.sarpraskost.app.entity.KondisiFasilitas;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.KamarRepository;
import com.sarpraskost.app.repository.KategoriFasilitasRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FasilitasService {

    private final FasilitasKamarRepository fasilitasRepo;
    private final KamarRepository kamarRepo;
    private final KategoriFasilitasRepository kategoriRepo;
    private final PenggunaRepository penggunaRepo;
    private final LaporanKerusakanRepository laporanRepo;

    public FasilitasService(
        FasilitasKamarRepository fasilitasRepo,
        KamarRepository kamarRepo,
        KategoriFasilitasRepository kategoriRepo,
        PenggunaRepository penggunaRepo,
        LaporanKerusakanRepository laporanRepo
    ) {
        this.fasilitasRepo = fasilitasRepo;
        this.kamarRepo = kamarRepo;
        this.kategoriRepo = kategoriRepo;
        this.penggunaRepo = penggunaRepo;
        this.laporanRepo = laporanRepo;
    }

    @Transactional(readOnly = true)
    public List<FasilitasResponse> listAll(Long idKamar, Long idKategori) {
        List<FasilitasKamar> items;
        if (idKamar != null) {
            items = fasilitasRepo.findByKamarIdKamarOrderByIdFasilitasDesc(idKamar);
        } else if (idKategori != null) {
            items = fasilitasRepo.findByKategoriIdKategoriOrderByIdFasilitasDesc(idKategori);
        } else {
            items = fasilitasRepo.findAllByOrderByIdFasilitasDesc();
        }
        return items.stream().map(FasilitasResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FasilitasResponse getById(Long id) {
        FasilitasKamar item = fasilitasRepo.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas tidak ditemukan."));
        return FasilitasResponse.from(item);
    }

    @Transactional(readOnly = true)
    public List<FasilitasResponse> listByKamar(Long idKamar) {
        if (!kamarRepo.existsById(idKamar)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan.");
        }
        return fasilitasRepo.findByKamarIdKamarOrderByIdFasilitasDesc(idKamar)
            .stream()
            .map(FasilitasResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FasilitasResponse> listByPenghuni(Long idPengguna) {
        Pengguna pengguna = penggunaRepo.findByIdPengguna(idPengguna)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Penghuni tidak ditemukan."));
        if (pengguna.getKamar() == null) {
            return List.of();
        }
        return fasilitasRepo.findByKamarIdKamarOrderByIdFasilitasDesc(pengguna.getKamar().getIdKamar())
            .stream()
            .map(FasilitasResponse::from)
            .toList();
    }

    @Transactional
    public synchronized FasilitasResponse create(FasilitasRequest req) {
        if (req == null || req.idKamar() == null || req.idKategori() == null || isBlank(req.namaFasilitas())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Kamar, kategori fasilitas, dan nama fasilitas wajib diisi."
            );
        }

        Kamar kamar = kamarRepo.findById(req.idKamar())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan."));
        KategoriFasilitas kategori = kategoriRepo.findById(req.idKategori())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori fasilitas tidak ditemukan."));

        String kodeAset;
        if (isBlank(req.kodeAset())) {
            kodeAset = generateNextKodeAset(kamar);
        } else {
            kodeAset = req.kodeAset().trim().toUpperCase(Locale.ROOT);
            if (fasilitasRepo.existsByKodeAsetIgnoreCase(kodeAset)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Kode aset sudah digunakan.");
            }
        }

        FasilitasKamar entity = new FasilitasKamar(
            kamar,
            kategori,
            kodeAset,
            req.namaFasilitas().trim(),
            req.merkTipe() != null ? req.merkTipe().trim() : "",
            parseKondisi(req.kondisi(), KondisiFasilitas.BAIK),
            req.tglPasang() != null ? req.tglPasang() : LocalDate.now()
        );

        return FasilitasResponse.from(fasilitasRepo.save(entity));
    }

    @Transactional
    public FasilitasResponse update(Long id, FasilitasRequest req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan fasilitas wajib diisi.");
        }
        FasilitasKamar entity = fasilitasRepo.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas tidak ditemukan."));

        if (req.idKamar() != null) {
            Kamar kamar = kamarRepo.findById(req.idKamar())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kamar tidak ditemukan."));
            entity.setKamar(kamar);
        }

        if (req.idKategori() != null) {
            KategoriFasilitas kategori = kategoriRepo.findById(req.idKategori())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori fasilitas tidak ditemukan."));
            entity.setKategori(kategori);
        }

        if (req.kodeAset() != null) {
            if (isBlank(req.kodeAset())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kode aset tidak boleh kosong.");
            }
            String nextKode = req.kodeAset().trim().toUpperCase(Locale.ROOT);
            if (!nextKode.equalsIgnoreCase(entity.getKodeAset())
                && fasilitasRepo.existsByKodeAsetIgnoreCase(nextKode)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Kode aset sudah digunakan.");
            }
            entity.setKodeAset(nextKode);
        }

        if (req.namaFasilitas() != null) {
            if (isBlank(req.namaFasilitas())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama fasilitas tidak boleh kosong.");
            }
            entity.setNamaFasilitas(req.namaFasilitas().trim());
        }

        if (req.merkTipe() != null) {
            entity.setMerkTipe(req.merkTipe().trim());
        }

        if (!isBlank(req.kondisi())) {
            entity.setKondisi(parseKondisi(req.kondisi(), entity.getKondisi()));
        }

        if (req.tglPasang() != null) {
            entity.setTglPasang(req.tglPasang());
        }

        return FasilitasResponse.from(fasilitasRepo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!fasilitasRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas tidak ditemukan.");
        }
        if (laporanRepo.countByFasilitasIdFasilitas(id) > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Fasilitas tidak dapat dihapus karena masih memiliki riwayat laporan kerusakan."
            );
        }
        fasilitasRepo.deleteById(id);
    }

    public String generateNextKodeAset(Kamar kamar) {
        String cleanNomor = kamar.getNomorKamar().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String prefix = "AST-" + cleanNomor + "-";
        int maxSeq = fasilitasRepo.findByKodeAsetStartingWith(prefix).stream()
            .map(FasilitasKamar::getKodeAset)
            .mapToInt(code -> extractSequence(code, prefix))
            .max()
            .orElse(0);
        return prefix + String.format("%02d", maxSeq + 1);
    }

    private int extractSequence(String code, String prefix) {
        if (code == null || !code.startsWith(prefix)) {
            return 0;
        }
        try {
            return Integer.parseInt(code.substring(prefix.length()));
        } catch (NumberFormatException ex) {
            return 0;
        }
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
