package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.FasilitasUmumAktifResponse;
import com.sarpraskost.app.dto.LaporanListResponse;
import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.UpdateLaporanRequest;
import com.sarpraskost.app.service.LaporanService;
import com.sarpraskost.app.util.SqlInjectionValidator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/laporan")
public class LaporanController {

    private final LaporanService laporanService;

    public LaporanController(LaporanService laporanService) {
        this.laporanService = laporanService;
    }

    @GetMapping("/fasilitas-umum/aktif")
    public ResponseEntity<List<FasilitasUmumAktifResponse>> getActiveFasilitasUmum() {
        return ResponseEntity.ok(laporanService.getActiveFasilitasUmum());
    }

    @PostMapping("/{id}/terdampak")
    public ResponseEntity<FasilitasUmumAktifResponse> tambahTerdampak(@PathVariable Integer id) {
        return ResponseEntity.ok(laporanService.tambahTerdampak(id));
    }

    @GetMapping("/penghuni/{idPengguna}")
    public ResponseEntity<LaporanListResponse> getByPenghuni(
        @PathVariable Integer idPengguna,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(laporanService.getLaporanByPenghuni(idPengguna, status, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaporanResponse> getById(
        @PathVariable Integer id,
        @RequestParam(required = false) String viewer,
        @RequestParam(required = false) Integer viewerId
    ) {
        return ResponseEntity.ok(laporanService.getLaporanById(id, viewer, viewerId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LaporanResponse> createLaporan(
        @RequestParam(value = "id_pengguna", required = false) Integer idPenggunaSnake,
        @RequestParam(value = "penghuni_id", required = false) Integer penghuniIdAlias,
        @RequestParam(value = "idPengguna", required = false) Integer idPenggunaCamel,
        @RequestParam(value = "id_fasilitas", required = false) String idFasilitasSnake,
        @RequestParam(value = "idFasilitas", required = false) String idFasilitasCamel,
        @RequestParam(value = "lokasi_laporan", required = false) String lokasiSnake,
        @RequestParam(value = "lokasiLaporan", required = false) String lokasiCamel,
        @RequestParam(value = "lokasi", required = false) String lokasiShort,
        @RequestParam(value = "nama_fasilitas_lainnya", required = false) String customNamaSnake,
        @RequestParam(value = "namaFasilitasLainnya", required = false) String customNamaCamel,
        @RequestParam(value = "fasilitas_lainnya", required = false) String customNamaAlt,
        @RequestParam(value = "fasilitasLainnya", required = false) String customNamaAltCamel,
        @RequestParam(value = "judul_laporan", required = false) String judulSnake,
        @RequestParam(value = "judulLaporan", required = false) String judulCamel,
        @RequestParam(value = "deskripsi_kerusakan", required = false) String deskripsiSnake,
        @RequestParam(value = "deskripsi", required = false) String deskripsiShort,
        @RequestParam(value = "tingkat_urgensi", required = false) String tingkatUrgensiSnake,
        @RequestParam(value = "tingkatUrgensi", required = false) String tingkatUrgensiCamel,
        @RequestParam(value = "is_urgent", required = false) Boolean isUrgentSnake,
        @RequestParam(value = "isUrgent", required = false) Boolean isUrgentCamel,
        @RequestParam(value = "izin_masuk_kamar", required = false) Boolean izinMasukSnake,
        @RequestParam(value = "izinMasukKamar", required = false) Boolean izinMasukCamel,
        @RequestParam(value = "foto", required = false) MultipartFile foto,
        @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Integer idPengguna = firstNonNull(idPenggunaSnake, penghuniIdAlias, idPenggunaCamel);
        Integer idFasilitas = parseOptionalInt(firstNonNullStr(idFasilitasSnake, idFasilitasCamel));
        String lokasiLaporan = firstNonNullStr(lokasiSnake, firstNonNullStr(lokasiCamel, lokasiShort));
        String namaFasilitasLainnya = firstNonNullStr(
            firstNonNullStr(customNamaSnake, customNamaCamel),
            firstNonNullStr(customNamaAlt, customNamaAltCamel)
        );
        String judul = firstNonNullStr(judulSnake, judulCamel);
        String deskripsi = firstNonNullStr(deskripsiSnake, deskripsiShort);
        String tingkatUrgensi = firstNonNullStr(tingkatUrgensiSnake, tingkatUrgensiCamel);
        Boolean isUrgent = firstNonNullBool(isUrgentSnake, isUrgentCamel);
        Boolean izinMasuk = firstNonNullBool(izinMasukSnake, izinMasukCamel);
        MultipartFile uploadedFoto = (foto != null && !foto.isEmpty()) ? foto : file;

        SqlInjectionValidator.requireSafe(lokasiLaporan, "lokasi_laporan");
        SqlInjectionValidator.requireSafe(namaFasilitasLainnya, "nama_fasilitas_lainnya");
        SqlInjectionValidator.requireSafe(judul, "judul_laporan");
        SqlInjectionValidator.requireSafe(deskripsi, "deskripsi_kerusakan");
        SqlInjectionValidator.requireSafe(tingkatUrgensi, "tingkat_urgensi");

        LaporanResponse created = laporanService.createLaporan(
            idPengguna,
            idFasilitas,
            lokasiLaporan,
            namaFasilitasLainnya,
            judul,
            deskripsi,
            tingkatUrgensi,
            isUrgent,
            izinMasuk,
            uploadedFoto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaporanResponse> updateLaporan(
        @PathVariable Integer id,
        @RequestBody UpdateLaporanRequest request
    ) {
        return ResponseEntity.ok(laporanService.updateLaporan(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLaporan(@PathVariable Integer id) {
        laporanService.deleteLaporan(id);
        return ResponseEntity.ok(Map.of("message", "Laporan berhasil dihapus."));
    }

    private Integer firstNonNull(Integer a, Integer b, Integer c) {
        if (a != null) return a;
        if (b != null) return b;
        return c;
    }

    private Integer parseOptionalInt(String raw) {
        if (raw == null) return null;
        String clean = raw.trim();
        if (clean.isEmpty() || "null".equalsIgnoreCase(clean) || "LAINNYA".equalsIgnoreCase(clean)) {
            return null;
        }
        try {
            return Integer.valueOf(clean);
        } catch (NumberFormatException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Format id_fasilitas tidak valid: " + raw
            );
        }
    }

    private String firstNonNullStr(String a, String b) {
        return a != null ? a : b;
    }

    private Boolean firstNonNullBool(Boolean a, Boolean b) {
        return a != null ? a : b;
    }
}
