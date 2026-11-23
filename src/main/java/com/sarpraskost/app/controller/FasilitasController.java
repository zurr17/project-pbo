package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.FasilitasRequest;
import com.sarpraskost.app.dto.FasilitasResponse;
import com.sarpraskost.app.service.FasilitasService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/fasilitas")
public class FasilitasController {

    private final FasilitasService fasilitasService;

    public FasilitasController(FasilitasService fasilitasService) {
        this.fasilitasService = fasilitasService;
    }

    @GetMapping
    public ResponseEntity<List<FasilitasResponse>> listAll(
        @RequestParam(value = "kamarId", required = false) Integer kamarId,
        @RequestParam(value = "kategoriId", required = false) Integer kategoriId
    ) {
        return ResponseEntity.ok(fasilitasService.listAll(kamarId, kategoriId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FasilitasResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(fasilitasService.getById(id));
    }

    @GetMapping("/kamar/{idKamar}")
    public ResponseEntity<List<FasilitasResponse>> listByKamar(@PathVariable Integer idKamar) {
        return ResponseEntity.ok(fasilitasService.listByKamar(idKamar));
    }

    @GetMapping("/umum")
    public ResponseEntity<List<FasilitasResponse>> listByUmum() {
        return ResponseEntity.ok(fasilitasService.listByUmum());
    }

    @GetMapping("/penghuni/{idPengguna}")
    public ResponseEntity<List<FasilitasResponse>> listByPenghuni(@PathVariable Integer idPengguna) {
        return ResponseEntity.ok(fasilitasService.listByPenghuni(idPengguna));
    }

    @PostMapping
    public ResponseEntity<FasilitasResponse> create(@RequestBody FasilitasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fasilitasService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FasilitasResponse> update(
        @PathVariable Integer id,
        @RequestBody FasilitasRequest request
    ) {
        return ResponseEntity.ok(fasilitasService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        fasilitasService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Fasilitas berhasil dihapus."));
    }
}
