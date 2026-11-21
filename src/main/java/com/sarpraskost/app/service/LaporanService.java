package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.LaporanSummaryResponse;
import com.sarpraskost.app.dto.MekanikDashboardResponse;
import com.sarpraskost.app.dto.MekanikTugasListResponse;
import com.sarpraskost.app.dto.OwnerDashboardResponse;
import com.sarpraskost.app.dto.OwnerLaporanListResponse;
import com.sarpraskost.app.dto.OwnerSetujuiRequest;
import com.sarpraskost.app.dto.OwnerTolakRequest;
import com.sarpraskost.app.dto.PenghuniLaporanListResponse;
import com.sarpraskost.app.dto.UpdateLaporanRequest;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Role;
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
            .filter(item -> keyword.isEmpty() || matchesKeyword(item, keyword, false))
            .map(LaporanResponse::from)
            .toList();

        return new PenghuniLaporanListResponse(summary, filtered);
    }

    @Transactional(readOnly = true)
    public OwnerDashboardResponse getOwnerDashboard() {
        OwnerLaporanListResponse result = getOwnerLaporan(null, null);
        return new OwnerDashboardResponse(result.ringkasan(), result.laporan());
    }

    @Transactional(readOnly = true)
    public OwnerLaporanListResponse getOwnerLaporan(String statusParam, String searchQuery) {
        List<LaporanKerusakan> allReports = laporanRepository.findAllByOrderByTanggalLaporDescIdDesc();
        LaporanSummaryResponse summary = buildSummary(allReports);
        StatusLaporan filterStatus = parseStatusFilter(statusParam);
        String keyword = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);

        List<LaporanResponse> filtered = allReports.stream()
            .filter(item -> filterStatus == null || item.getStatus() == filterStatus)
            .filter(item -> keyword.isEmpty() || matchesKeyword(item, keyword, true))
            .map(LaporanResponse::from)
            .toList();

        return new OwnerLaporanListResponse(summary, filtered);
    }

    @Transactional
    public LaporanResponse setujuiLaporan(Long id, OwnerSetujuiRequest request) {
        if (request == null || request.mekanikId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mekanik wajib dipilih.");
        }

        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        User mekanik = userRepository.findById(request.mekanikId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mekanik tidak ditemukan."));

        if (mekanik.getRole() != Role.MEKANIK) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pengguna yang dipilih bukan mekanik.");
        }

        String prioritas = isBlank(request.prioritas()) ? "Sedang" : request.prioritas().trim();
        String catatanOwner = isBlank(request.catatanOwner()) ? null : request.catatanOwner().trim();

        StringBuilder catatanPenanganan = new StringBuilder("Ditugaskan ke teknisi ")
            .append(mekanik.getNama())
            .append(" (Prioritas: ")
            .append(prioritas)
            .append(")");
        if (catatanOwner != null) {
            catatanPenanganan.append(". Catatan: ").append(catatanOwner);
        } else {
            catatanPenanganan.append(".");
        }

        laporan.setStatus(StatusLaporan.DIPROSES);
        laporan.setMekanikId(mekanik.getId());
        laporan.setNamaMekanik(mekanik.getNama());
        laporan.setPrioritas(prioritas);
        laporan.setCatatanOwner(catatanOwner);
        laporan.setKategoriPenolakan(null);
        laporan.setAlasanPenolakan(null);
        laporan.setCatatanPenanganan(catatanPenanganan.toString());
        laporan.setTanggalDiproses(LocalDateTime.now());
        laporan.setTanggalSelesai(null);

        return LaporanResponse.from(laporanRepository.save(laporan));
    }

    @Transactional
    public LaporanResponse tolakLaporan(Long id, OwnerTolakRequest request) {
        if (request == null || isBlank(request.alasanPenolakan())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alasan penolakan wajib diisi.");
        }

        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        String kategoriPenolakan = isBlank(request.kategoriPenolakan()) ? "Lainnya" : request.kategoriPenolakan().trim();
        String alasanPenolakan = request.alasanPenolakan().trim();

        laporan.setStatus(StatusLaporan.DITOLAK);
        laporan.setKategoriPenolakan(kategoriPenolakan);
        laporan.setAlasanPenolakan(alasanPenolakan);
        laporan.setMekanikId(null);
        laporan.setNamaMekanik(null);
        laporan.setPrioritas(null);
        laporan.setCatatanOwner(null);
        laporan.setCatatanPenanganan("Ditolak (" + kategoriPenolakan + "): " + alasanPenolakan);
        laporan.setTanggalDiproses(LocalDateTime.now());
        laporan.setTanggalSelesai(null);

        return LaporanResponse.from(laporanRepository.save(laporan));
    }

    @Transactional(readOnly = true)
    public MekanikDashboardResponse getMekanikDashboard(Long mekanikId) {
        MekanikTugasListResponse result = getMekanikTugas(mekanikId, null, null);
        return new MekanikDashboardResponse(result.ringkasan(), result.laporan());
    }

    @Transactional(readOnly = true)
    public MekanikTugasListResponse getMekanikTugas(
        Long mekanikId,
        String statusParam,
        String searchQuery
    ) {
        User mekanik = userRepository.findById(mekanikId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mekanik tidak ditemukan."));

        if (mekanik.getRole() != Role.MEKANIK) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pengguna bukan merupakan mekanik.");
        }

        List<LaporanKerusakan> allTasks =
            laporanRepository.findByMekanikIdOrderByTanggalLaporDescIdDesc(mekanikId);
        LaporanSummaryResponse summary = buildSummary(allTasks);
        StatusLaporan filterStatus = parseStatusFilter(statusParam);
        String keyword = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);

        List<LaporanResponse> filtered = allTasks.stream()
            .filter(item -> filterStatus == null || item.getStatus() == filterStatus)
            .filter(item -> keyword.isEmpty() || matchesKeyword(item, keyword, true))
            .map(LaporanResponse::from)
            .toList();

        return new MekanikTugasListResponse(summary, filtered);
    }

    @Transactional
    public LaporanResponse updateTugasMekanik(
        Long id,
        String statusParam,
        String catatanMekanik,
        MultipartFile fotoSelesai
    ) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tugas laporan tidak ditemukan."));

        StatusLaporan targetStatus;
        if (isBlank(statusParam)) {
            targetStatus = StatusLaporan.SELESAI;
        } else {
            try {
                targetStatus = StatusLaporan.valueOf(statusParam.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status tugas tidak valid: " + statusParam);
            }
        }

        if (targetStatus != StatusLaporan.DIPROSES && targetStatus != StatusLaporan.SELESAI) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Status pengerjaan mekanik hanya boleh DIPROSES atau SELESAI."
            );
        }

        String note = isBlank(catatanMekanik) ? null : catatanMekanik.trim();
        if (note != null) {
            laporan.setCatatanMekanik(note);
        }

        String oldFotoSelesaiUrl = null;
        if (fotoSelesai != null && !fotoSelesai.isEmpty()) {
            String storedUrl = fileStorageService.storeFoto(fotoSelesai);
            oldFotoSelesaiUrl = laporan.getFotoSelesaiUrl();
            laporan.setFotoSelesaiUrl(storedUrl);
        }

        LocalDateTime now = LocalDateTime.now();
        if (laporan.getTanggalDiproses() == null) {
            laporan.setTanggalDiproses(now);
        }

        laporan.setStatus(targetStatus);
        if (targetStatus == StatusLaporan.SELESAI) {
            laporan.setTanggalSelesai(now);
        } else {
            laporan.setTanggalSelesai(null);
        }

        String namaTeknisi = isBlank(laporan.getNamaMekanik()) ? "Teknisi" : laporan.getNamaMekanik();
        if (note != null) {
            String prefix = targetStatus == StatusLaporan.SELESAI
                ? "Selesai oleh " + namaTeknisi + ": "
                : "Progres oleh " + namaTeknisi + ": ";
            laporan.setCatatanPenanganan(prefix + note);
        } else if (targetStatus == StatusLaporan.SELESAI && isBlank(laporan.getCatatanPenanganan())) {
            laporan.setCatatanPenanganan("Perbaikan telah diselesaikan oleh " + namaTeknisi + ".");
        }

        LaporanKerusakan saved = laporanRepository.save(laporan);
        laporanRepository.flush();
        if (oldFotoSelesaiUrl != null) {
            fileStorageService.deleteFoto(oldFotoSelesaiUrl);
        }

        return LaporanResponse.from(saved);
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

    @Transactional
    public LaporanResponse updateLaporan(Long id, UpdateLaporanRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data pembaruan laporan wajib diisi.");
        }

        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        if (request.kategoriFasilitas() == null && request.lokasiKamar() == null && request.deskripsi() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimal satu data laporan harus diubah.");
        }

        if (request.kategoriFasilitas() != null) {
            if (isBlank(request.kategoriFasilitas())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kategori fasilitas wajib dipilih.");
            }
            laporan.setKategoriFasilitas(request.kategoriFasilitas().trim());
        }

        if (request.lokasiKamar() != null) {
            if (isBlank(request.lokasiKamar())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lokasi atau nomor kamar wajib diisi.");
            }
            laporan.setLokasiKamar(request.lokasiKamar().trim());
        }

        if (request.deskripsi() != null) {
            if (isBlank(request.deskripsi())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deskripsi kerusakan wajib diisi.");
            }
            laporan.setDeskripsi(request.deskripsi().trim());
        }

        return LaporanResponse.from(laporanRepository.save(laporan));
    }

    @Transactional
    public void deleteLaporan(Long id) {
        LaporanKerusakan laporan = laporanRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));
        String fotoUrl = laporan.getFotoUrl();
        String fotoSelesaiUrl = laporan.getFotoSelesaiUrl();
        laporanRepository.delete(laporan);
        laporanRepository.flush();
        fileStorageService.deleteFoto(fotoUrl);
        fileStorageService.deleteFoto(fotoSelesaiUrl);
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

    private boolean matchesKeyword(LaporanKerusakan item, String keyword, boolean includePenghuni) {
        boolean matched = containsIgnoreCase(item.getKodeLaporan(), keyword)
            || containsIgnoreCase(item.getKategoriFasilitas(), keyword)
            || containsIgnoreCase(item.getLokasiKamar(), keyword)
            || containsIgnoreCase(item.getDeskripsi(), keyword);
        if (matched || !includePenghuni || item.getPenghuni() == null) {
            return matched;
        }
        return containsIgnoreCase(item.getPenghuni().getNama(), keyword)
            || containsIgnoreCase(item.getPenghuni().getNomorKamar(), keyword);
    }

    private boolean containsIgnoreCase(String value, String lowerKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerKeyword);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
