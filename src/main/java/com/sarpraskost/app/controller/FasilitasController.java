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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fasilitas")
public class FasilitasController {

    private final FasilitasService fasilitasService;

    public FasilitasController(FasilitasService fasilitasService) {
        this.fasilitasService = fasilitasService;
    }

    @PostMapping
    public ResponseEntity<FasilitasResponse> create(@RequestBody FasilitasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fasilitasService.create(request));
    }

    @GetMapping("/penghuni/{penghuniId}")
    public ResponseEntity<List<FasilitasResponse>> listByPenghuni(@PathVariable Long penghuniId) {
        return ResponseEntity.ok(fasilitasService.listByPenghuni(penghuniId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FasilitasResponse> update(
        @PathVariable Long id,
        @RequestBody FasilitasRequest request
    ) {
        return ResponseEntity.ok(fasilitasService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        fasilitasService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Fasilitas berhasil dihapus."));
    }
}
