package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.LaporanSummaryResponse;
import com.sarpraskost.app.dto.PenghuniLaporanListResponse;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.StatusLaporan;
import com.sarpraskost.app.entity.User;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LaporanService {

    private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final LaporanKerusakanRepository laporanRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public LaporanService(
        LaporanKerusakanRepository laporanRepository,
        UserRepository userRepository,
        FileStorageService fileStorageService
    ) {
        this.laporanRepository = laporanRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public PenghuniLaporanListResponse getLaporanByPenghuni(
        Long penghuniId,
        String statusParam,
        String searchQuery
    ) {
        if (!userRepository.existsById(penghuniId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Penghuni tidak ditemukan.");
        }

        List<LaporanKerusakan> allReports =
            laporanRepository.findByPenghuniIdOrderByTanggalLaporDescIdDesc(penghuniId);

        LaporanSummaryResponse summary = buildSummary(allReports);
        StatusLaporan filterStatus = parseStatusFilter(statusParam);
        String keyword = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);

        List<LaporanResponse> filtered = allReports.stream()
            .filter(item -> filterStatus == null || item.getStatus() == filterStatus)
            .filter(item -> keyword.isEmpty() || matchesKeyword(item, keyword))
            .map(LaporanResponse::from)
            .toList();

        return new PenghuniLaporanListResponse(summary, filtered);
    }

    @Transactional(readOnly = true)
    public LaporanResponse getLaporanById(Long id) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));
        return LaporanResponse.from(laporan);
    }

    @Transactional
    public synchronized LaporanResponse createLaporan(
        Long penghuniId,
        String kategoriFasilitas,
        String lokasiKamar,
        String deskripsi,
        MultipartFile foto
    ) {
        if (penghuniId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "penghuni_id wajib diisi.");
        }
        if (isBlank(kategoriFasilitas)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kategori fasilitas wajib dipilih.");
        }
        if (isBlank(lokasiKamar)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lokasi atau nomor kamar wajib diisi.");
        }
        if (isBlank(deskripsi)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deskripsi kerusakan wajib diisi.");
        }

        User penghuni = userRepository.findById(penghuniId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data penghuni tidak ditemukan."));

        String fotoUrl = fileStorageService.storeFoto(foto);
        LocalDateTime now = LocalDateTime.now();

        LaporanKerusakan laporan = new LaporanKerusakan();
        laporan.setKodeLaporan(generateNextKodeLaporan(now.toLocalDate()));
        laporan.setPenghuni(penghuni);
        laporan.setKategoriFasilitas(kategoriFasilitas.trim());
        laporan.setLokasiKamar(lokasiKamar.trim());
        laporan.setDeskripsi(deskripsi.trim());
        laporan.setFotoUrl(fotoUrl);
        laporan.setStatus(StatusLaporan.MENUNGGU);
        laporan.setTanggalLapor(now);

        LaporanKerusakan saved = laporanRepository.save(laporan);
        return LaporanResponse.from(saved);
    }

    public String generateNextKodeLaporan(LocalDate date) {
        String prefix = "LPR-" + date.format(CODE_DATE_FORMAT) + "-";
        int maxSequence = laporanRepository.findByKodeLaporanStartingWith(prefix).stream()
            .map(LaporanKerusakan::getKodeLaporan)
            .mapToInt(code -> extractSequence(code, prefix))
            .max()
            .orElse(0);
        return prefix + String.format("%03d", maxSequence + 1);
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

    private LaporanSummaryResponse buildSummary(List<LaporanKerusakan> reports) {
        long menunggu = 0;
        long diproses = 0;
        long selesai = 0;
        long ditolak = 0;
        for (LaporanKerusakan item : reports) {
            switch (item.getStatus()) {
                case MENUNGGU -> menunggu++;
                case DIPROSES -> diproses++;
                case SELESAI -> selesai++;
                case DITOLAK -> ditolak++;
            }
        }
        return new LaporanSummaryResponse(reports.size(), menunggu, diproses, selesai, ditolak);
    }

    private StatusLaporan parseStatusFilter(String statusParam) {
        if (isBlank(statusParam) || "SEMUA".equalsIgnoreCase(statusParam.trim())) {
            return null;
        }
        try {
            return StatusLaporan.valueOf(statusParam.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status filter tidak valid: " + statusParam);
        }
    }

    private boolean matchesKeyword(LaporanKerusakan item, String keyword) {
        return containsIgnoreCase(item.getKodeLaporan(), keyword)
            || containsIgnoreCase(item.getKategoriFasilitas(), keyword)
            || containsIgnoreCase(item.getLokasiKamar(), keyword)
            || containsIgnoreCase(item.getDeskripsi(), keyword);
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
