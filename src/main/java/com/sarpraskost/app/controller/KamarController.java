package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.KamarRequest;
import com.sarpraskost.app.dto.KamarResponse;
import com.sarpraskost.app.service.KamarService;
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
@RequestMapping("/api/kamar")
public class KamarController {

    private final KamarService kamarService;

    public KamarController(KamarService kamarService) {
        this.kamarService = kamarService;
    }

    @GetMapping
    public ResponseEntity<List<KamarResponse>> listAll() {
        return ResponseEntity.ok(kamarService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KamarResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(kamarService.getById(id));
    }

    @PostMapping
    public ResponseEntity<KamarResponse> create(@RequestBody KamarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(kamarService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KamarResponse> update(
        @PathVariable Long id,
        @RequestBody KamarRequest request
    ) {
        return ResponseEntity.ok(kamarService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        kamarService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Kamar berhasil dihapus."));
    }
}
