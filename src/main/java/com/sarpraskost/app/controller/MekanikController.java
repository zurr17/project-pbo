package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.MekanikDashboardResponse;
import com.sarpraskost.app.dto.MekanikTugasListResponse;
import com.sarpraskost.app.service.LaporanService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mekanik")
public class MekanikController {

    private final LaporanService laporanService;

    public MekanikController(LaporanService laporanService) {
        this.laporanService = laporanService;
    }

    @GetMapping("/{mekanikId}/dashboard")
    public MekanikDashboardResponse getDashboard(@PathVariable Long mekanikId) {
        return laporanService.getMekanikDashboard(mekanikId);
    }

    @GetMapping("/{mekanikId}/tugas")
    public MekanikTugasListResponse getTugas(
        @PathVariable Long mekanikId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false, name = "q") String q
    ) {
        return laporanService.getMekanikTugas(mekanikId, status, q);
    }

    @PutMapping(value = "/tugas/{id}/selesai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LaporanResponse selesaikanTugas(
        @PathVariable Long id,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "catatanMekanik", required = false) String catatanMekanik,
        @RequestParam(value = "catatan_mekanik", required = false) String catatanMekanikSnake,
        @RequestParam(value = "fotoSelesai", required = false) MultipartFile fotoSelesai,
        @RequestParam(value = "foto_selesai", required = false) MultipartFile fotoSelesaiSnake,
        @RequestParam(value = "foto", required = false) MultipartFile fotoFallback
    ) {
        String note = hasText(catatanMekanik) ? catatanMekanik : catatanMekanikSnake;
        MultipartFile uploadedFile = firstNonEmptyFile(fotoSelesai, fotoSelesaiSnake, fotoFallback);
        return laporanService.updateTugasMekanik(id, status, note, uploadedFile);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private MultipartFile firstNonEmptyFile(MultipartFile... files) {
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return file;
            }
        }
        return null;
    }
}
