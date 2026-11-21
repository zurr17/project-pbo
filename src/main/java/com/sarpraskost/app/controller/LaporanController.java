package com.sarpraskost.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.PenghuniLaporanListResponse;
import com.sarpraskost.app.service.LaporanService;

@RestController
@RequestMapping("/api/laporan")
public class LaporanController {

    private final LaporanService laporanService;

    public LaporanController(LaporanService laporanService) {
        this.laporanService = laporanService;
    }   

    @GetMapping("/penghuni/{penghuniId}")
    public ResponseEntity<PenghuniLaporanListResponse> getByPenghuni(
        @PathVariable Long penghuniId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(laporanService.getLaporanByPenghuni(penghuniId, status, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaporanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(laporanService.getLaporanById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LaporanResponse> createLaporan(
        @RequestParam(value = "penghuni_id", required = false) Long penghuniIdSnake,
        @RequestParam(value = "penghuniId", required = false) Long penghuniIdCamel,
        @RequestParam(value = "kategori_fasilitas", required = false) String kategoriSnake,
        @RequestParam(value = "kategoriFasilitas", required = false) String kategoriCamel,
        @RequestParam(value = "lokasi_kamar", required = false) String lokasiSnake,
        @RequestParam(value = "lokasiKamar", required = false) String lokasiCamel,
        @RequestParam("deskripsi") String deskripsi,
        @RequestParam(value = "foto", required = false) MultipartFile foto,
        @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Long penghuniId = penghuniIdSnake != null ? penghuniIdSnake : penghuniIdCamel;
        String kategori = kategoriSnake != null ? kategoriSnake : kategoriCamel;
        String lokasi = lokasiSnake != null ? lokasiSnake : lokasiCamel;
        MultipartFile uploadedFoto = (foto != null && !foto.isEmpty()) ? foto : file;

        LaporanResponse created = laporanService.createLaporan(
            penghuniId,
            kategori,
            lokasi,
            deskripsi,
            uploadedFoto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
