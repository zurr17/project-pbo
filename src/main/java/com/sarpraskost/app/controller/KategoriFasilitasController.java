package com.sarpraskost.app.controller;

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

import com.sarpraskost.app.dto.KategoriFasilitasRequest;
import com.sarpraskost.app.dto.KategoriFasilitasResponse;
import com.sarpraskost.app.service.KategoriFasilitasService;

@RestController
@RequestMapping("/api/kategori")
public class KategoriFasilitasController {

    private final KategoriFasilitasService kategoriService;

    public KategoriFasilitasController(KategoriFasilitasService kategoriService) {
        this.kategoriService = kategoriService;
    }

    @GetMapping
    public ResponseEntity<List<KategoriFasilitasResponse>> listAll() {
        return ResponseEntity.ok(kategoriService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KategoriFasilitasResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(kategoriService.getById(id));
    }

    @PostMapping
    public ResponseEntity<KategoriFasilitasResponse> create(@RequestBody KategoriFasilitasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kategoriService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KategoriFasilitasResponse> update(
        @PathVariable Integer id,
        @RequestBody KategoriFasilitasRequest request
    ) {
        return ResponseEntity.ok(kategoriService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        kategoriService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Kategori fasilitas berhasil dihapus."));
    }
}
