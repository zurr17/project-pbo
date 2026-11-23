package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.LaporanListResponse;
import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.OwnerProsesRequest;
import com.sarpraskost.app.dto.OwnerTolakRequest;
import com.sarpraskost.app.service.LaporanService;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final LaporanService laporanService;

    public OwnerController(LaporanService laporanService) {
        this.laporanService = laporanService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<LaporanListResponse> getDashboard() {
        return ResponseEntity.ok(laporanService.getOwnerDashboard());
    }

    @GetMapping("/laporan")
    public ResponseEntity<LaporanListResponse> getLaporan(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(laporanService.getOwnerLaporan(status, q));
    }

    @PutMapping("/laporan/{id}/proses")
    public ResponseEntity<LaporanResponse> prosesLaporan(
        @PathVariable Integer id,
        @RequestBody(required = false) OwnerProsesRequest request
    ) {
        return ResponseEntity.ok(laporanService.prosesLaporan(id, request));
    }

    @PutMapping("/laporan/{id}/tolak")
    public ResponseEntity<LaporanResponse> tolakLaporan(
        @PathVariable Integer id,
        @RequestBody OwnerTolakRequest request
    ) {
        return ResponseEntity.ok(laporanService.tolakLaporan(id, request));
    }

    @PutMapping(value = "/laporan/{id}/selesai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LaporanResponse> selesaikanLaporan(
        @PathVariable Integer id,
        @RequestParam(value = "id_pengguna", required = false) Integer idPenggunaSnake,
        @RequestParam(value = "id_owner", required = false) Integer idOwnerAlias,
        @RequestParam(value = "catatan_penanganan", required = false) String catatanSnake,
        @RequestParam(value = "catatanPenanganan", required = false) String catatanCamel,
        @RequestParam(value = "biaya_perbaikan", required = false) String biayaRaw,
        @RequestParam(value = "kondisi_fasilitas", required = false) String kondisiFasilitas,
        @RequestParam(value = "foto_sesudah", required = false) MultipartFile fotoSesudah,
        @RequestParam(value = "fotoSesudah", required = false) MultipartFile fotoSesudahCamel,
        @RequestParam(value = "foto", required = false) MultipartFile fotoFallback
    ) {
        Integer idPengguna = idPenggunaSnake != null ? idPenggunaSnake : idOwnerAlias;
        String catatan = catatanSnake != null ? catatanSnake : catatanCamel;
        BigDecimal biaya = parseBiaya(biayaRaw);
        MultipartFile bukti = selectPhoto(fotoSesudah, fotoSesudahCamel, fotoFallback);

        return ResponseEntity.ok(
            laporanService.selesaikanLaporan(id, idPengguna, catatan, biaya, kondisiFasilitas, bukti)
        );
    }

    private BigDecimal parseBiaya(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nominal biaya perbaikan tidak valid.");
        }
    }

    private MultipartFile selectPhoto(MultipartFile a, MultipartFile b, MultipartFile c) {
        if (a != null && !a.isEmpty()) return a;
        if (b != null && !b.isEmpty()) return b;
        if (c != null && !c.isEmpty()) return c;
        return null;
    }
}
