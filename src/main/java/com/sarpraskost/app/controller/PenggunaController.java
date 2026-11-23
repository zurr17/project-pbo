package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.CreatePenggunaRequest;
import com.sarpraskost.app.dto.PenggunaResponse;
import com.sarpraskost.app.dto.RegisterPenggunaRequest;
import com.sarpraskost.app.dto.UpdatePenggunaRequest;
import com.sarpraskost.app.service.PenggunaService;
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
@RequestMapping("/api/users")
public class PenggunaController {

    private final PenggunaService penggunaService;

    public PenggunaController(PenggunaService penggunaService) {
        this.penggunaService = penggunaService;
    }

    @GetMapping
    public ResponseEntity<List<PenggunaResponse>> listAll(@RequestParam(required = false) String role) {
        return ResponseEntity.ok(penggunaService.listAll(role));
    }

    @PostMapping("/register")
    public ResponseEntity<PenggunaResponse> register(@RequestBody RegisterPenggunaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(penggunaService.register(request));
    }

    @PostMapping
    public ResponseEntity<PenggunaResponse> create(@RequestBody CreatePenggunaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(penggunaService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenggunaResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(penggunaService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PenggunaResponse> update(
        @PathVariable Integer id,
        @RequestBody UpdatePenggunaRequest request
    ) {
        return ResponseEntity.ok(penggunaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        penggunaService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Akun berhasil dihapus."));
    }
}
