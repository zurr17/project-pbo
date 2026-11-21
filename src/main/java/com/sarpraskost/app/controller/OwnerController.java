package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.LaporanResponse;
import com.sarpraskost.app.dto.OwnerDashboardResponse;
import com.sarpraskost.app.dto.OwnerLaporanListResponse;
import com.sarpraskost.app.dto.OwnerSetujuiRequest;
import com.sarpraskost.app.dto.OwnerTolakRequest;
import com.sarpraskost.app.service.LaporanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final LaporanService laporanService;

    public OwnerController(LaporanService laporanService) {
        this.laporanService = laporanService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<OwnerDashboardResponse> getDashboard() {
        return ResponseEntity.ok(laporanService.getOwnerDashboard());
    }

    @GetMapping("/laporan")
    public ResponseEntity<OwnerLaporanListResponse> getLaporan(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(laporanService.getOwnerLaporan(status, q));
    }

    @PutMapping("/laporan/{id}/setujui")
    public ResponseEntity<LaporanResponse> setujuiLaporan(
        @PathVariable Long id,
        @RequestBody OwnerSetujuiRequest request
    ) {
        return ResponseEntity.ok(laporanService.setujuiLaporan(id, request));
    }

    @PutMapping("/laporan/{id}/tolak")
    public ResponseEntity<LaporanResponse> tolakLaporan(
        @PathVariable Long id,
        @RequestBody OwnerTolakRequest request
    ) {
        return ResponseEntity.ok(laporanService.tolakLaporan(id, request));
    }
}
