package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.FasilitasUmumAktifResponse;
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
import com.sarpraskost.app.entity.TingkatUrgensi;
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
import java.util.Comparator;
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
    private static final DateTimeFormatter SCHEDULE_FORMAT = DateTimeFormatter.ofPattern(
        "dd MMM yyyy 'pukul' HH:mm",
        Locale.forLanguageTag("id-ID")
    );
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
        Integer idPengguna,
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
    public List<FasilitasUmumAktifResponse> getActiveFasilitasUmum() {
        List<LaporanKerusakan> activeShared = laporanRepository.findActiveFasilitasUmum(ACTIVE_STATUSES);
        if (activeShared.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = activeShared.stream().map(LaporanKerusakan::getIdLaporan).toList();
        Map<Integer, String> lastNoteByLaporan = riwayatRepository
            .findByLaporanIdLaporanInOrderByWaktuUpdateAscIdRiwayatAsc(ids)
            .stream()
            .collect(Collectors.toMap(
                r -> r.getLaporan().getIdLaporan(),
                RiwayatPenanganan::getCatatanPenanganan,
                (first, second) -> second
            ));

        return activeShared.stream()
            .map(l -> FasilitasUmumAktifResponse.from(l, lastNoteByLaporan.get(l.getIdLaporan())))
            .toList();
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
    public LaporanResponse getLaporanById(Integer id) {
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
        Integer idPengguna,
        Integer idFasilitas,
        String judulLaporan,
        String deskripsiKerusakan,
        String tingkatUrgensiRaw,
        Boolean isUrgentParam,
        Boolean izinMasukKamarParam,
        MultipartFile foto
    ) {
        if (idPengguna == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id_pengguna wajib diisi.");
        }
        if (idFasilitas == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fasilitas wajib dipilih.");
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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas tidak ditemukan."));

        boolean fasilitasUmum = isFasilitasUmum(fasilitas);
        validatePelaporFacilityAccess(pelapor, fasilitas, fasilitasUmum);

        if (fasilitasUmum) {
            laporanRepository.findFirstByFasilitasIdFasilitasAndStatusTiketInOrderByTglLaporDescIdLaporanDesc(
                fasilitas.getIdFasilitas(),
                ACTIVE_STATUSES
            ).ifPresent(existing -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sudah ada laporan aktif untuk fasilitas umum ini (" + existing.getKodeTiket()
                        + "). Gunakan tombol 'Saya Terdampak (+1)'."
                );
            });
        }

        TingkatUrgensi urgensi = resolveUrgensi(tingkatUrgensiRaw, isUrgentParam);
        boolean urgent = Boolean.TRUE.equals(isUrgentParam) || urgensi == TingkatUrgensi.DARURAT;
        if (urgent && urgensi != TingkatUrgensi.DARURAT && isBlank(tingkatUrgensiRaw)) {
            urgensi = TingkatUrgensi.DARURAT;
        }
        boolean izinMasuk = Boolean.TRUE.equals(izinMasukKamarParam);

        if (!fasilitasUmum && urgent && !izinMasuk) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Untuk laporan darurat di kamar pribadi, Anda wajib mencentang persetujuan izin masuk kamar."
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
        laporan.setFotoUrl(fotoUrl != null ? fotoUrl : "");
        laporan.setTingkatUrgensi(urgensi);
        laporan.setTglLapor(now);
        laporan.setStatusTiket(StatusTiket.MENUNGGU);
        laporan.setJumlahTerdampak(1);
        laporan.setUrgent(urgent);
        laporan.setIzinMasukKamar(fasilitasUmum ? false : izinMasuk);

        LaporanKerusakan saved = laporanRepository.save(laporan);

        if (fasilitas.getKondisi() == KondisiFasilitas.BAIK) {
            fasilitas.setKondisi(urgent ? KondisiFasilitas.RUSAK_BERAT : KondisiFasilitas.RUSAK_RINGAN);
            fasilitasRepository.save(fasilitas);
        }

        String initialNote = buildInitialRiwayatNote(fasilitasUmum, urgent, izinMasuk);
        simpanRiwayat(saved, pelapor, StatusTiket.MENUNGGU, initialNote, null, BigDecimal.ZERO, now);

        notifyOwnersOnNewReport(saved, pelapor, fasilitas, fasilitasUmum, urgent, izinMasuk, now);

        return getLaporanById(saved.getIdLaporan());
    }

    @Transactional
    public synchronized FasilitasUmumAktifResponse tambahTerdampak(Integer idLaporan) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(idLaporan)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        if (!isFasilitasUmum(laporan.getFasilitas())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Fitur 'Saya Terdampak (+1)' hanya berlaku untuk laporan fasilitas umum."
            );
        }

        if (!ACTIVE_STATUSES.contains(laporan.getStatusTiket())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tiket ini sudah selesai atau ditolak sehingga tidak dapat menambah penghuni terdampak."
            );
        }

        int current = laporan.getJumlahTerdampak() == null ? 1 : laporan.getJumlahTerdampak();
        laporan.setJumlahTerdampak(current + 1);
        LaporanKerusakan saved = laporanRepository.save(laporan);

        String lastNote = riwayatRepository
            .findByLaporanIdLaporanOrderByWaktuUpdateAscIdRiwayatAsc(saved.getIdLaporan())
            .stream()
            .reduce((first, second) -> second)
            .map(RiwayatPenanganan::getCatatanPenanganan)
            .orElse(null);
        return FasilitasUmumAktifResponse.from(saved, lastNote);
    }

    @Transactional
    public LaporanResponse updateLaporan(Integer id, UpdateLaporanRequest request) {
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

        if (request.idFasilitas() == null && request.judulLaporan() == null
            && request.deskripsiKerusakan() == null && request.tingkatUrgensi() == null
            && request.isUrgent() == null && request.izinMasukKamar() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimal satu data laporan harus diubah.");
        }

        FasilitasKamar targetFasilitas = laporan.getFasilitas();
        if (request.idFasilitas() != null) {
            targetFasilitas = fasilitasRepository.findDetailById(request.idFasilitas())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fasilitas tidak ditemukan."));
            validatePelaporFacilityAccess(laporan.getPengguna(), targetFasilitas, isFasilitasUmum(targetFasilitas));
            laporan.setFasilitas(targetFasilitas);
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

        if (request.tingkatUrgensi() != null) {
            TingkatUrgensi nextUrgensi = resolveUrgensi(request.tingkatUrgensi(), request.isUrgent());
            laporan.setTingkatUrgensi(nextUrgensi);
            if (nextUrgensi == TingkatUrgensi.DARURAT) {
                laporan.setUrgent(true);
            }
        }
        if (request.isUrgent() != null) {
            laporan.setUrgent(request.isUrgent());
            if (request.isUrgent() && laporan.getTingkatUrgensi() != TingkatUrgensi.DARURAT) {
                laporan.setTingkatUrgensi(TingkatUrgensi.DARURAT);
            }
        }
        if (request.izinMasukKamar() != null) {
            laporan.setIzinMasukKamar(request.izinMasukKamar());
        }

        if (!isFasilitasUmum(targetFasilitas) && laporan.isUrgent() && !laporan.isIzinMasukKamar()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Untuk laporan darurat di kamar pribadi, persetujuan izin masuk kamar wajib dicentang."
            );
        }

        laporanRepository.save(laporan);
        return getLaporanById(id);
    }

    @Transactional
    public void deleteLaporan(Integer id) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        List<String> filesToDelete = new ArrayList<>();
        if (!isBlank(laporan.getFotoUrl())) {
            filesToDelete.add(laporan.getFotoUrl());
        }

        List<RiwayatPenanganan> riwayatList =
            riwayatRepository.findByLaporanIdLaporanOrderByWaktuUpdateAscIdRiwayatAsc(id);
        for (RiwayatPenanganan item : riwayatList) {
            if (!isBlank(item.getFotoSesudahUrl())) {
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
    public LaporanResponse prosesLaporan(Integer id, OwnerProsesRequest request) {
        LaporanKerusakan laporan = laporanRepository.findDetailById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Laporan tidak ditemukan."));

        if (laporan.getStatusTiket() == StatusTiket.SELESAI || laporan.getStatusTiket() == StatusTiket.DITOLAK) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Laporan yang sudah selesai atau ditolak tidak dapat diproses ulang."
            );
        }

        if (request == null || request.jadwalPengerjaan() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Jadwal pengerjaan (tanggal dan waktu pasti) wajib diisi oleh pengelola."
            );
        }

        Pengguna owner = resolveOwnerActor(request.idPengguna());
        String formattedJadwal = request.jadwalPengerjaan().format(SCHEDULE_FORMAT);
        String baseCatatan = !isBlank(request.catatanPenanganan())
            ? request.catatanPenanganan().trim()
            : "Laporan disetujui dan dijadwalkan untuk diperbaiki oleh pengelola.";
        String catatan = baseCatatan + " Jadwal pengerjaan: " + formattedJadwal + ".";
        BigDecimal biaya = validateBiayaNonNull(request.biayaPerbaikan());
        KondisiFasilitas nextKondisi = parseKondisiOptional(
            request.kondisiFasilitas(),
            KondisiFasilitas.DALAM_PERBAIKAN
        );

        LocalDateTime now = LocalDateTime.now();
        laporan.setStatusTiket(StatusTiket.DIPROSES);
        laporan.setJadwalPengerjaan(request.jadwalPengerjaan());
        laporanRepository.save(laporan);

        FasilitasKamar fasilitas = laporan.getFasilitas();
        fasilitas.setKondisi(nextKondisi);
        fasilitasRepository.save(fasilitas);

        simpanRiwayat(laporan, owner, StatusTiket.DIPROSES, catatan, null, biaya, now);

        String estimasiText = biaya.compareTo(BigDecimal.ZERO) > 0
            ? " Estimasi biaya: " + formatRupiah(biaya) + "."
            : "";
        notifikasiService.kirimNotifikasi(
            laporan.getPengguna(),
            laporan,
            "Jadwal Perbaikan Ditetapkan",
            "Laporan " + laporan.getJudulLaporan() + " akan ditangani oleh " + owner.getNamaLengkap()
                + " pada jadwal " + formattedJadwal + "." + estimasiText,
            now
        );

        return getLaporanById(id);
    }

    @Transactional
    public LaporanResponse tolakLaporan(Integer id, OwnerTolakRequest request) {
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

        simpanRiwayat(laporan, owner, StatusTiket.DITOLAK, catatan, null, BigDecimal.ZERO, now);

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
        Integer id,
        Integer idPengguna,
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
        BigDecimal biaya = validateBiayaNonNull(biayaPerbaikan);
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

        String biayaText = biaya.compareTo(BigDecimal.ZERO) > 0
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
        String prefix = "TKT-" + date.format(CODE_DATE_FORMAT) + "-";
        int maxSequence = laporanRepository.findByKodeTiketStartingWith(prefix).stream()
            .map(LaporanKerusakan::getKodeTiket)
            .mapToInt(code -> extractSequence(code, prefix))
            .max()
            .orElse(0);
        return prefix + String.format("%02d", maxSequence + 1);
    }

    private void validatePelaporFacilityAccess(Pengguna pelapor, FasilitasKamar fasilitas, boolean fasilitasUmum) {
        if (pelapor.getRole() != Role.PENGHUNI || fasilitasUmum) {
            return;
        }
        if (pelapor.getKamar() == null || !pelapor.getKamar().getIdKamar().equals(fasilitas.getKamar().getIdKamar())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Fasilitas yang dipilih tidak berada di kamar penghuni atau fasilitas umum."
            );
        }
    }

    private String buildInitialRiwayatNote(boolean fasilitasUmum, boolean urgent, boolean izinMasuk) {
        StringBuilder sb = new StringBuilder("Laporan kerusakan ");
        sb.append(fasilitasUmum ? "fasilitas umum " : "");
        sb.append("dikirim oleh penghuni");
        if (urgent) {
            sb.append(" dengan prioritas DARURAT");
        }
        if (!fasilitasUmum && izinMasuk) {
            sb.append(" (izin masuk kamar disetujui)");
        }
        sb.append(".");
        return sb.toString();
    }

    private void notifyOwnersOnNewReport(
        LaporanKerusakan saved,
        Pengguna pelapor,
        FasilitasKamar fasilitas,
        boolean fasilitasUmum,
        boolean urgent,
        boolean izinMasuk,
        LocalDateTime now
    ) {
        String lokasi = fasilitasUmum ? "Fasilitas Umum" : "Kamar " + fasilitas.getKamar().getNomorKamar();
        String urgencyTag = urgent ? "[DARURAT] " : "";
        String izinNote = (!fasilitasUmum && izinMasuk) ? " Izin masuk kamar telah disetujui penghuni." : "";
        String pelaporText = pelapor.getNamaLengkap() + " (" + lokasi + ")";

        for (Pengguna owner : penggunaRepository.findByRoleOrderByNamaLengkapAsc(Role.OWNER)) {
            notifikasiService.kirimNotifikasi(
                owner,
                saved,
                urgencyTag + "Laporan Baru: " + saved.getJudulLaporan(),
                pelaporText + " melaporkan kerusakan pada " + fasilitas.getNamaFasilitas() + "." + izinNote,
                now
            );
        }
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
        riwayat.setCatatanPenanganan(catatanPenanganan == null ? "" : catatanPenanganan);
        riwayat.setFotoSesudahUrl(fotoSesudahUrl);
        riwayat.setBiayaPerbaikan(biayaPerbaikan == null ? BigDecimal.ZERO : biayaPerbaikan);
        riwayat.setWaktuUpdate(waktuUpdate);
        riwayatRepository.save(riwayat);
    }

    private Pengguna resolveOwnerActor(Integer requestedPenggunaId) {
        if (requestedPenggunaId != null) {
            Pengguna pengguna = penggunaRepository.findByIdPengguna(requestedPenggunaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data pengelola tidak ditemukan."));
            if (pengguna.getRole() != Role.OWNER) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Hanya pengguna dengan peran OWNER/Pengelola yang dapat memproses laporan."
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
        boolean ownerMode
    ) {
        LaporanSummaryResponse summary = buildSummary(allReports);
        StatusTiket filterStatus = parseStatusFilter(statusParam);
        String keyword = searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);

        var stream = allReports.stream()
            .filter(item -> filterStatus == null || item.getStatusTiket() == filterStatus)
            .filter(item -> keyword.isEmpty() || matchesKeyword(item, keyword, ownerMode));

        if (ownerMode) {
            stream = stream.sorted(ownerPriorityComparator());
        }

        List<LaporanKerusakan> filteredEntities = stream.toList();
        if (filteredEntities.isEmpty()) {
            return new LaporanListResponse(summary, List.of());
        }

        List<Integer> ids = filteredEntities.stream().map(LaporanKerusakan::getIdLaporan).toList();
        Map<Integer, List<RiwayatPenangananResponse>> riwayatByLaporan = riwayatRepository
            .findByLaporanIdLaporanInOrderByWaktuUpdateAscIdRiwayatAsc(ids)
            .stream()
            .map(RiwayatPenangananResponse::from)
            .collect(Collectors.groupingBy(RiwayatPenangananResponse::idLaporan));

        List<LaporanResponse> responses = filteredEntities.stream()
            .map(item -> LaporanResponse.from(item, riwayatByLaporan.getOrDefault(item.getIdLaporan(), List.of())))
            .toList();

        return new LaporanListResponse(summary, responses);
    }

    private Comparator<LaporanKerusakan> ownerPriorityComparator() {
        return Comparator
            .comparingInt((LaporanKerusakan l) -> ACTIVE_STATUSES.contains(l.getStatusTiket()) ? 0 : 1)
            .thenComparingInt(l -> l.isUrgent() ? 0 : 1)
            .thenComparing(
                Comparator.comparingInt((LaporanKerusakan l) -> l.getJumlahTerdampak() == null ? 1 : l.getJumlahTerdampak())
                    .reversed()
            )
            .thenComparing(LaporanKerusakan::getTglLapor, Comparator.reverseOrder())
            .thenComparing(LaporanKerusakan::getIdLaporan, Comparator.reverseOrder());
    }

    private LaporanSummaryResponse buildSummary(List<LaporanKerusakan> reports) {
        long menunggu = 0;
        long diproses = 0;
        long selesai = 0;
        long ditolak = 0;
        long darurat = 0;
        for (LaporanKerusakan item : reports) {
            if (ACTIVE_STATUSES.contains(item.getStatusTiket()) && item.isUrgent()) {
                darurat++;
            }
            switch (item.getStatusTiket()) {
                case MENUNGGU -> menunggu++;
                case DIPROSES -> diproses++;
                case SELESAI -> selesai++;
                case DITOLAK -> ditolak++;
            }
        }
        return new LaporanSummaryResponse(reports.size(), menunggu, diproses, selesai, ditolak, darurat);
    }

    private StatusTiket parseStatusFilter(String statusParam) {
        if (isBlank(statusParam) || "SEMUA".equalsIgnoreCase(statusParam.trim())) {
            return null;
        }
        try {
            return StatusTiket.fromInput(statusParam);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status filter tidak valid: " + statusParam);
        }
    }

    private TingkatUrgensi resolveUrgensi(String raw, Boolean isUrgent) {
        if (isBlank(raw)) {
            return Boolean.TRUE.equals(isUrgent) ? TingkatUrgensi.DARURAT : TingkatUrgensi.SEDANG;
        }
        try {
            return TingkatUrgensi.fromInput(raw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tingkat urgensi harus Rendah, Sedang, Tinggi, atau Darurat."
            );
        }
    }

    private KondisiFasilitas parseKondisiOptional(String raw, KondisiFasilitas fallback) {
        if (isBlank(raw)) {
            return fallback;
        }
        try {
            return KondisiFasilitas.fromInput(raw);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Kondisi fasilitas harus BAIK, RUSAK RINGAN, RUSAK BERAT, atau DALAM PERBAIKAN."
            );
        }
    }

    private BigDecimal validateBiayaNonNull(BigDecimal biaya) {
        if (biaya == null) {
            return BigDecimal.ZERO;
        }
        if (biaya.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Biaya perbaikan tidak boleh negatif.");
        }
        return biaya;
    }

    private boolean isFasilitasUmum(FasilitasKamar fasilitas) {
        return fasilitas != null && fasilitas.getKamar() != null && fasilitas.getKamar().isFasilitasUmum();
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
