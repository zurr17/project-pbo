package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.LaporanListResponse;
import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.UpdateLaporanRequest;
import com.sarpraskost.app.service.LaporanService;
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

    @GetMapping("/penghuni/{idPengguna}")
    public ResponseEntity<LaporanListResponse> getByPenghuni(
        @PathVariable Long idPengguna,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(laporanService.getLaporanByPenghuni(idPengguna, status, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaporanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(laporanService.getLaporanById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LaporanResponse> createLaporan(
        @RequestParam(value = "id_pengguna", required = false) Long idPenggunaSnake,
        @RequestParam(value = "penghuni_id", required = false) Long penghuniIdAlias,
        @RequestParam(value = "idPengguna", required = false) Long idPenggunaCamel,
        @RequestParam(value = "id_fasilitas", required = false) Long idFasilitasSnake,
        @RequestParam(value = "idFasilitas", required = false) Long idFasilitasCamel,
        @RequestParam(value = "judul_laporan", required = false) String judulSnake,
        @RequestParam(value = "judulLaporan", required = false) String judulCamel,
        @RequestParam(value = "deskripsi_kerusakan", required = false) String deskripsiSnake,
        @RequestParam(value = "deskripsi", required = false) String deskripsiShort,
        @RequestParam(value = "foto", required = false) MultipartFile foto,
        @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Long idPengguna = firstNonNull(idPenggunaSnake, penghuniIdAlias, idPenggunaCamel);
        Long idFasilitas = firstNonNull(idFasilitasSnake, idFasilitasCamel, null);
        String judul = judulSnake != null ? judulSnake : judulCamel;
        String deskripsi = deskripsiSnake != null ? deskripsiSnake : deskripsiShort;
        MultipartFile uploadedFoto = (foto != null && !foto.isEmpty()) ? foto : file;

        LaporanResponse created = laporanService.createLaporan(
            idPengguna,
            idFasilitas,
            judul,
            deskripsi,
            uploadedFoto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaporanResponse> updateLaporan(
        @PathVariable Long id,
        @RequestBody UpdateLaporanRequest request
    ) {
        return ResponseEntity.ok(laporanService.updateLaporan(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLaporan(@PathVariable Long id) {
        laporanService.deleteLaporan(id);
        return ResponseEntity.ok(Map.of("message", "Laporan berhasil dihapus."));
    }

    private Long firstNonNull(Long a, Long b, Long c) {
        if (a != null) return a;
        if (b != null) return b;
        return c;
    }
}
