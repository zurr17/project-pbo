package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.LaporanListResponse;
import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.LaporanSummaryResponse;
import com.sarpraskost.app.dto.OwnerProsesRequest;
import com.sarpraskost.app.dto.OwnerTolakRequest;
import com.sarpraskost.app.dto.RiwayatPenangananResponse;
import com.sarpraskost.app.dto.UpdateLaporanRequest;
import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.KondisiFasilitas;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.RiwayatPenanganan;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.entity.StatusTiket;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.NotifikasiRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import com.sarpraskost.app.repository.RiwayatPenangananRepository;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LaporanService {

    private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final List<StatusTiket> ACTIVE_STATUSES = List.of(StatusTiket.MENUNGGU, StatusTiket.DIPROSES);

    private final LaporanKerusakanRepository laporanRepository;
    private final PenggunaRepository penggunaRepository;
    private final FasilitasKamarRepository fasilitasRepository;
    private final RiwayatPenangananRepository riwayatRepository;
    private final NotifikasiRepository notifikasiRepository;
    private final NotifikasiService notifikasiService;
    private final FileStorageService fileStorageService;

    public LaporanService(
        LaporanKerusakanRepository laporanRepository,
        PenggunaRepository penggunaRepository,
        FasilitasKamarRepository fasilitasRepository,
        RiwayatPenangananRepository riwayatRepository,
        NotifikasiRepository notifikasiRepository,
        NotifikasiService notifikasiService,
        FileStorageService fileStorageService
    ) {
        this.laporanRepository = laporanRepository;
        this.penggunaRepository = penggunaRepository;
        this.fasilitasRepository = fasilitasRepository;
        this.riwayatRepository = riwayatRepository;
        this.notifikasiRepository = notifikasiRepository;
        this.notifikasiService = notifikasiService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public LaporanListResponse getLaporanByPenghuni(
        Long idPengguna,
        String statusParam,
        String searchQuery
    ) {
        if (!penggunaRepository.existsById(idPengguna)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Penghuni tidak ditemukan.");
        }

        List<LaporanKerusakan> allReports =
            laporanRepository.findByPenggunaIdPenggunaOrderByTglLaporDescIdLaporanDesc(idPengguna);
        return buildFilteredListResponse(allReports, statusParam, searchQuery, false);
    }

    @Transactional(readOnly = true)
    public LaporanListResponse getOwnerDashboard() {
        return getOwnerLaporan(null, null);
    }

    @Transactional(readOnly = true)
    public LaporanListResponse getOwnerLaporan(String statusParam, String searchQuery) {
        List<LaporanKerusakan> allReports = laporanRepository.findAllByOrderByTglLaporDescIdLaporanDesc();
        return buildFilteredListResponse(allReports, statusParam, searchQuery, true);
    }

    @Transactional(readOnly = true)
    public LaporanResponse getLaporanById(Long id) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));
        List<RiwayatPenangananResponse> riwayat = riwayatRepository
            .findByLaporanIdLaporanOrderByWaktuUpdateAscIdRiwayatAsc(id)
            .stream()
            .map(RiwayatPenangananResponse::from)
            .toList();
        return LaporanResponse.from(laporan, riwayat);
    }

    @Transactional
    public synchronized LaporanResponse createLaporan(
        Long idPengguna,
        Long idFasilitas,
        String judulLaporan,
        String deskripsiKerusakan,
        MultipartFile foto
    ) {
        if (idPengguna == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id_pengguna wajib diisi.");
        }
        if (idFasilitas == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fasilitas kamar wajib dipilih.");
        }
        if (isBlank(judulLaporan)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Judul laporan wajib diisi.");
        }
        if (isBlank(deskripsiKerusakan)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deskripsi kerusakan wajib diisi.");
        }

        Pengguna pelapor = penggunaRepository.findByIdPengguna(idPengguna)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data penghuni tidak ditemukan."));
        FasilitasKamar fasilitas = fasilitasRepository.findDetailById(idFasilitas)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas kamar tidak ditemukan."));

        if (pelapor.getRole() == Role.PENGHUNI && pelapor.getKamar() != null
            && !pelapor.getKamar().getIdKamar().equals(fasilitas.getKamar().getIdKamar())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Fasilitas yang dipilih tidak berada di kamar penghuni."
            );
        }

        String fotoUrl = fileStorageService.storeFoto(foto);
        LocalDateTime now = LocalDateTime.now();

        LaporanKerusakan laporan = new LaporanKerusakan();
        laporan.setPengguna(pelapor);
        laporan.setFasilitas(fasilitas);
        laporan.setKodeTiket(generateNextKodeTiket(now.toLocalDate()));
        laporan.setJudulLaporan(judulLaporan.trim());
        laporan.setDeskripsiKerusakan(deskripsiKerusakan.trim());
        laporan.setFotoUrl(fotoUrl);
        laporan.setTglLapor(now);
        laporan.setStatusTiket(StatusTiket.MENUNGGU);

        LaporanKerusakan saved = laporanRepository.save(laporan);

        if (fasilitas.getKondisi() == KondisiFasilitas.BAIK) {
            fasilitas.setKondisi(KondisiFasilitas.RUSAK);
            fasilitasRepository.save(fasilitas);
        }

        simpanRiwayat(
            saved,
            pelapor,
            StatusTiket.MENUNGGU,
            "Laporan kerusakan dikirim oleh penghuni.",
            null,
            null,
            now
        );

        String kamarLabel = fasilitas.getKamar().getNomorKamar();
        List<Pengguna> owners = penggunaRepository.findByRoleOrderByNamaLengkapAsc(Role.OWNER);
        for (Pengguna owner : owners) {
            notifikasiService.kirimNotifikasi(
                owner,
                saved,
                "Laporan Baru: " + saved.getJudulLaporan(),
                pelapor.getNamaLengkap() + " (Kamar " + kamarLabel + ") melaporkan kerusakan pada "
                    + fasilitas.getNamaFasilitas() + ".",
                now
            );
        }

        return getLaporanById(saved.getIdLaporan());
    }

    @Transactional
    public LaporanResponse updateLaporan(Long id, UpdateLaporanRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan laporan wajib diisi.");
        }

        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        if (laporan.getStatusTiket() != StatusTiket.MENUNGGU) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Hanya laporan berstatus MENUNGGU yang dapat diedit oleh penghuni."
            );
        }

        if (request.idFasilitas() == null && request.judulLaporan() == null && request.deskripsiKerusakan() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimal satu data laporan harus diubah.");
        }

        if (request.idFasilitas() != null) {
            FasilitasKamar fasilitas = fasilitasRepository.findDetailById(request.idFasilitas())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas kamar tidak ditemukan."));
            if (laporan.getPengguna().getKamar() != null
                && !laporan.getPengguna().getKamar().getIdKamar().equals(fasilitas.getKamar().getIdKamar())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Fasilitas yang dipilih tidak berada di kamar penghuni."
                );
            }
            laporan.setFasilitas(fasilitas);
        }

        if (request.judulLaporan() != null) {
            if (isBlank(request.judulLaporan())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Judul laporan wajib diisi.");
            }
            laporan.setJudulLaporan(request.judulLaporan().trim());
        }

        if (request.deskripsiKerusakan() != null) {
            if (isBlank(request.deskripsiKerusakan())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deskripsi kerusakan wajib diisi.");
            }
            laporan.setDeskripsiKerusakan(request.deskripsiKerusakan().trim());
        }

        laporanRepository.save(laporan);
        return getLaporanById(id);
    }

    @Transactional
    public void deleteLaporan(Long id) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        List<String> filesToDelete = new ArrayList<>();
        if (laporan.getFotoUrl() != null) {
            filesToDelete.add(laporan.getFotoUrl());
        }

        List<RiwayatPenanganan> riwayatList =
            riwayatRepository.findByLaporanIdLaporanOrderByWaktuUpdateAscIdRiwayatAsc(id);
        for (RiwayatPenanganan item : riwayatList) {
            if (item.getFotoSesudahUrl() != null) {
                filesToDelete.add(item.getFotoSesudahUrl());
            }
        }

        FasilitasKamar fasilitas = laporan.getFasilitas();
        notifikasiRepository.deleteByLaporanIdLaporan(id);
        riwayatRepository.deleteByLaporanIdLaporan(id);
        laporanRepository.delete(laporan);
        laporanRepository.flush();

        long remainingActive = laporanRepository.countByFasilitasIdFasilitasAndStatusTiketIn(
            fasilitas.getIdFasilitas(),
            ACTIVE_STATUSES
        );
        if (remainingActive == 0 && fasilitas.getKondisi() != KondisiFasilitas.BAIK) {
            fasilitas.setKondisi(KondisiFasilitas.BAIK);
            fasilitasRepository.save(fasilitas);
        }

        for (String url : filesToDelete) {
            fileStorageService.deleteFoto(url);
        }
    }

    @Transactional
    public LaporanResponse prosesLaporan(Long id, OwnerProsesRequest request) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        if (laporan.getStatusTiket() == StatusTiket.SELESAI || laporan.getStatusTiket() == StatusTiket.DITOLAK) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Laporan yang sudah selesai atau ditolak tidak dapat diproses ulang."
            );
        }

        Pengguna owner = resolveOwnerActor(request != null ? request.idPengguna() : null);
        String catatan = request != null && !isBlank(request.catatanPenanganan())
            ? request.catatanPenanganan().trim()
            : "Laporan disetujui dan sedang ditangani oleh pengelola.";
        BigDecimal biaya = validateBiaya(request != null ? request.biayaPerbaikan() : null);
        KondisiFasilitas nextKondisi = parseKondisiOptional(
            request != null ? request.kondisiFasilitas() : null,
            KondisiFasilitas.PERLU_PERAWATAN
        );

        LocalDateTime now = LocalDateTime.now();
        laporan.setStatusTiket(StatusTiket.DIPROSES);
        laporanRepository.save(laporan);

        FasilitasKamar fasilitas = laporan.getFasilitas();
        fasilitas.setKondisi(nextKondisi);
        fasilitasRepository.save(fasilitas);

        simpanRiwayat(laporan, owner, StatusTiket.DIPROSES, catatan, null, biaya, now);

        String estimasiText = biaya != null && biaya.compareTo(BigDecimal.ZERO) > 0
            ? " Estimasi biaya: " + formatRupiah(biaya) + "."
            : "";
        notifikasiService.kirimNotifikasi(
            laporan.getPengguna(),
            laporan,
            "Laporan Diproses",
            "Laporan " + laporan.getJudulLaporan() + " sedang ditangani oleh "
                + owner.getNamaLengkap() + "." + estimasiText,
            now
        );

        return getLaporanById(id);
    }

    @Transactional
    public LaporanResponse tolakLaporan(Long id, OwnerTolakRequest request) {
        if (request == null || isBlank(request.alasanPenolakan())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alasan penolakan wajib diisi.");
        }

        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        if (laporan.getStatusTiket() == StatusTiket.SELESAI) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Laporan yang sudah selesai tidak dapat ditolak."
            );
        }

        Pengguna owner = resolveOwnerActor(request.idPengguna());
        String kategori = isBlank(request.kategoriPenolakan()) ? "" : request.kategoriPenolakan().trim();
        String alasan = request.alasanPenolakan().trim();
        String catatan = kategori.isEmpty() ? alasan : kategori + " — " + alasan;

        LocalDateTime now = LocalDateTime.now();
        laporan.setStatusTiket(StatusTiket.DITOLAK);
        laporanRepository.save(laporan);
        laporanRepository.flush();

        FasilitasKamar fasilitas = laporan.getFasilitas();
        long remainingActive = laporanRepository.countByFasilitasIdFasilitasAndStatusTiketIn(
            fasilitas.getIdFasilitas(),
            ACTIVE_STATUSES
        );
        if (remainingActive == 0) {
            fasilitas.setKondisi(KondisiFasilitas.BAIK);
            fasilitasRepository.save(fasilitas);
        }

        simpanRiwayat(laporan, owner, StatusTiket.DITOLAK, catatan, null, null, now);

        notifikasiService.kirimNotifikasi(
            laporan.getPengguna(),
            laporan,
            "Laporan Ditolak",
            "Laporan " + laporan.getJudulLaporan() + " ditolak oleh pengelola: " + catatan,
            now
        );

        return getLaporanById(id);
    }

    @Transactional
    public LaporanResponse selesaikanLaporan(
        Long id,
        Long idPengguna,
        String catatanPenanganan,
        BigDecimal biayaPerbaikan,
        String kondisiFasilitas,
        MultipartFile fotoSesudah
    ) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        if (laporan.getStatusTiket() == StatusTiket.DITOLAK) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Laporan yang ditolak tidak dapat diselesaikan."
            );
        }

        Pengguna owner = resolveOwnerActor(idPengguna);
        BigDecimal biaya = validateBiaya(biayaPerbaikan);
        String catatan = !isBlank(catatanPenanganan)
            ? catatanPenanganan.trim()
            : "Perbaikan telah diselesaikan oleh pengelola.";
        String fotoSesudahUrl = fileStorageService.storeFoto(fotoSesudah);
        KondisiFasilitas nextKondisi = parseKondisiOptional(kondisiFasilitas, KondisiFasilitas.BAIK);

        LocalDateTime now = LocalDateTime.now();
        laporan.setStatusTiket(StatusTiket.SELESAI);
        laporanRepository.save(laporan);

        FasilitasKamar fasilitas = laporan.getFasilitas();
        fasilitas.setKondisi(nextKondisi);
        fasilitasRepository.save(fasilitas);

        simpanRiwayat(laporan, owner, StatusTiket.SELESAI, catatan, fotoSesudahUrl, biaya, now);

        String biayaText = biaya != null && biaya.compareTo(BigDecimal.ZERO) > 0
            ? " Total biaya: " + formatRupiah(biaya) + "."
            : "";
        notifikasiService.kirimNotifikasi(
            laporan.getPengguna(),
            laporan,
            "Perbaikan Selesai",
            "Perbaikan untuk " + laporan.getJudulLaporan() + " telah selesai dikerjakan." + biayaText,
            now
        );

        return getLaporanById(id);
    }

    public String generateNextKodeTiket(LocalDate date) {
        String prefix = "LPR-" + date.format(CODE_DATE_FORMAT) + "-";
        int maxSequence = laporanRepository.findByKodeTiketStartingWith(prefix).stream()
            .map(LaporanKerusakan::getKodeTiket)
            .mapToInt(code -> extractSequence(code, prefix))
            .max()
            .orElse(0);
        return prefix + String.format("%03d", maxSequence + 1);
    }

    private void simpanRiwayat(
        LaporanKerusakan laporan,
        Pengguna actor,
        StatusTiket statusBaru,
        String catatanPenanganan,
        String fotoSesudahUrl,
        BigDecimal biayaPerbaikan,
        LocalDateTime waktuUpdate
    ) {
        RiwayatPenanganan riwayat = new RiwayatPenanganan();
        riwayat.setLaporan(laporan);
        riwayat.setPengguna(actor);
        riwayat.setStatusBaru(statusBaru);
        riwayat.setCatatanPenanganan(catatanPenanganan);
        riwayat.setFotoSesudahUrl(fotoSesudahUrl);
        riwayat.setBiayaPerbaikan(biayaPerbaikan);
        riwayat.setWaktuUpdate(waktuUpdate);
        riwayatRepository.save(riwayat);
    }

    private Pengguna resolveOwnerActor(Long requestedPenggunaId) {
        if (requestedPenggunaId != null) {
            Pengguna pengguna = penggunaRepository.findByIdPengguna(requestedPenggunaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data owner tidak ditemukan."));
            if (pengguna.getRole() != Role.OWNER) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hanya pengguna dengan peran OWNER yang dapat memproses laporan."
                );
            }
            return pengguna;
        }
        return penggunaRepository.findFirstByRoleOrderByIdPenggunaAsc(Role.OWNER)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Akun Owner belum tersedia."));
    }

    private LaporanListResponse buildFilteredListResponse(
        List<LaporanKerusakan> allReports,
        String statusParam,
        String searchQuery,
        boolean includePelapor
    ) {
        LaporanSummaryResponse summary = buildSummary(allReports);
        StatusTiket filterStatus = parseStatusFilter(statusParam);
        String keyword = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);

        List<LaporanKerusakan> filteredEntities = allReports.stream()
            .filter(item -> filterStatus == null || item.getStatusTiket() == filterStatus)
            .filter(item -> keyword.isEmpty() || matchesKeyword(item, keyword, includePelapor))
            .toList();

        if (filteredEntities.isEmpty()) {
            return new LaporanListResponse(summary, List.of());
        }

        List<Long> ids = filteredEntities.stream().map(LaporanKerusakan::getIdLaporan).toList();
        Map<Long, List<RiwayatPenangananResponse>> riwayatByLaporan = riwayatRepository
            .findByLaporanIdLaporanInOrderByWaktuUpdateAscIdRiwayatAsc(ids)
            .stream()
            .map(RiwayatPenangananResponse::from)
            .collect(Collectors.groupingBy(RiwayatPenangananResponse::idLaporan));

        List<LaporanResponse> responses = filteredEntities.stream()
            .map(item -> LaporanResponse.from(item, riwayatByLaporan.getOrDefault(item.getIdLaporan(), List.of())))
            .toList();

        return new LaporanListResponse(summary, responses);
    }

    private LaporanSummaryResponse buildSummary(List<LaporanKerusakan> reports) {
        long menunggu = 0;
        long diproses = 0;
        long selesai = 0;
        long ditolak = 0;
        for (LaporanKerusakan item : reports) {
            switch (item.getStatusTiket()) {
                case MENUNGGU -> menunggu++;
                case DIPROSES -> diproses++;
                case SELESAI -> selesai++;
                case DITOLAK -> ditolak++;
            }
        }
        return new LaporanSummaryResponse(reports.size(), menunggu, diproses, selesai, ditolak);
    }

    private StatusTiket parseStatusFilter(String statusParam) {
        if (isBlank(statusParam) || "SEMUA".equalsIgnoreCase(statusParam.trim())) {
            return null;
        }
        try {
            return StatusTiket.valueOf(statusParam.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status filter tidak valid: " + statusParam);
        }
    }

    private KondisiFasilitas parseKondisiOptional(String raw, KondisiFasilitas fallback) {
        if (isBlank(raw)) {
            return fallback;
        }
        try {
            return KondisiFasilitas.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Kondisi fasilitas harus BAIK, PERLU_PERAWATAN, atau RUSAK."
            );
        }
    }

    private BigDecimal validateBiaya(BigDecimal biaya) {
        if (biaya != null && biaya.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Biaya perbaikan tidak boleh negatif.");
        }
        return biaya;
    }

    private boolean matchesKeyword(LaporanKerusakan item, String keyword, boolean includePelapor) {
        FasilitasKamar f = item.getFasilitas();
        boolean matched = containsIgnoreCase(item.getKodeTiket(), keyword)
            || containsIgnoreCase(item.getJudulLaporan(), keyword)
            || containsIgnoreCase(item.getDeskripsiKerusakan(), keyword)
            || containsIgnoreCase(f.getNamaFasilitas(), keyword)
            || containsIgnoreCase(f.getKodeAset(), keyword)
            || containsIgnoreCase(f.getKategori().getNamaKategori(), keyword)
            || containsIgnoreCase(f.getKamar().getNomorKamar(), keyword);
        if (matched || !includePelapor || item.getPengguna() == null) {
            return matched;
        }
        return containsIgnoreCase(item.getPengguna().getNamaLengkap(), keyword);
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
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

    private String formatRupiah(BigDecimal amount) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"));
        return "Rp " + fmt.format(amount);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
