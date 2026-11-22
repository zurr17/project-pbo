package com.sarpraskost.app.controller;

import com.sarpraskost.app.dto.NotifikasiResponse;
import com.sarpraskost.app.service.NotifikasiService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifikasi")
public class NotifikasiController {

    private final NotifikasiService notifikasiService;

    public NotifikasiController(NotifikasiService notifikasiService) {
        this.notifikasiService = notifikasiService;
    }

    @GetMapping("/{idPengguna}")
    public ResponseEntity<List<NotifikasiResponse>> listByPengguna(@PathVariable Long idPengguna) {
        return ResponseEntity.ok(notifikasiService.listByPengguna(idPengguna));
    }

    @GetMapping("/{idPengguna}/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@PathVariable Long idPengguna) {
        return ResponseEntity.ok(Map.of("unread", notifikasiService.countUnread(idPengguna)));
    }

    @PutMapping("/{id}/baca")
    public ResponseEntity<NotifikasiResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notifikasiService.markAsRead(id));
    }

    @PutMapping("/baca-semua/{idPengguna}")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long idPengguna) {
        notifikasiService.markAllAsRead(idPengguna);
        return ResponseEntity.ok(Map.of("message", "Semua notifikasi telah ditandai sudah dibaca."));
    }
}
