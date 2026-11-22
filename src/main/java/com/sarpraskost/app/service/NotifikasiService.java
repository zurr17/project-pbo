package com.sarpraskost.app.service;

import com.sarpraskost.app.dto.NotifikasiResponse;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Notifikasi;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.repository.NotifikasiRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotifikasiService {

    private final NotifikasiRepository notifikasiRepository;
    private final PenggunaRepository penggunaRepository;

    public NotifikasiService(
        NotifikasiRepository notifikasiRepository,
        PenggunaRepository penggunaRepository
    ) {
        this.notifikasiRepository = notifikasiRepository;
        this.penggunaRepository = penggunaRepository;
    }

    @Transactional(readOnly = true)
    public List<NotifikasiResponse> listByPengguna(Long idPengguna) {
        if (!penggunaRepository.existsById(idPengguna)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan.");
        }
        return notifikasiRepository.findByPenggunaIdPenggunaOrderByWaktuKirimDescIdNotifikasiDesc(idPengguna)
            .stream()
            .map(NotifikasiResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Long idPengguna) {
        if (!penggunaRepository.existsById(idPengguna)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan.");
        }
        return notifikasiRepository.countUnreadByPenggunaId(idPengguna);
    }

    @Transactional
    public NotifikasiResponse markAsRead(Long idNotifikasi) {
        Notifikasi notif = notifikasiRepository.findDetailById(idNotifikasi)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notifikasi tidak ditemukan."));
        if (!notif.isRead()) {
            notif.setRead(true);
            notif = notifikasiRepository.save(notif);
        }
        return NotifikasiResponse.from(notif);
    }

    @Transactional
    public void markAllAsRead(Long idPengguna) {
        if (!penggunaRepository.existsById(idPengguna)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pengguna tidak ditemukan.");
        }
        List<Notifikasi> list = notifikasiRepository
            .findByPenggunaIdPenggunaOrderByWaktuKirimDescIdNotifikasiDesc(idPengguna);
        for (Notifikasi item : list) {
            if (!item.isRead()) {
                item.setRead(true);
            }
        }
        notifikasiRepository.saveAll(list);
    }

    @Transactional
    public void kirimNotifikasi(
        Pengguna penerima,
        LaporanKerusakan laporan,
        String judulNotif,
        String pesanNotif,
        LocalDateTime waktuKirim
    ) {
        Notifikasi notif = new Notifikasi();
        notif.setPengguna(penerima);
        notif.setLaporan(laporan);
        notif.setJudulNotif(judulNotif);
        notif.setPesanNotif(pesanNotif);
        notif.setRead(false);
        notif.setWaktuKirim(waktuKirim != null ? waktuKirim : LocalDateTime.now());
        notifikasiRepository.save(notif);
    }
}
