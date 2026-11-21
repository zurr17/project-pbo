package com.sarpraskost.app.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.entity.StatusLaporan;
import com.sarpraskost.app.entity.User;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.UserRepository;
import com.sarpraskost.app.service.LaporanService;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LaporanKerusakanRepository laporanRepository;
    private final LaporanService laporanService;

    public DataSeeder(
        UserRepository userRepository,
        LaporanKerusakanRepository laporanRepository,
        LaporanService laporanService
    ) {
        this.userRepository = userRepository;
        this.laporanRepository = laporanRepository;
        this.laporanService = laporanService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User demoPenghuni = userRepository.findByEmailIgnoreCase("penghuni@kost.com")
            .orElseGet(() -> userRepository.save(new User(
                "Mori",
                "penghuni@kost.com",
                "123",
                Role.PENGHUNI,
                "A-12"
            )));

        if (laporanRepository.countByPenghuniId(demoPenghuni.getId()) > 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        LaporanKerusakan laporanMenunggu = new LaporanKerusakan();
        laporanMenunggu.setKodeLaporan(laporanService.generateNextKodeLaporan(today));
        laporanMenunggu.setPenghuni(demoPenghuni);
        laporanMenunggu.setKategoriFasilitas("Listrik & Pencahayaan");
        laporanMenunggu.setLokasiKamar("Teras Gedung A · Kamar A-12");
        laporanMenunggu.setDeskripsi(
            "Lampu teras mati. Lampu utama di teras depan gedung A berkedip lalu padam total sejak kemarin malam."
        );
        laporanMenunggu.setStatus(StatusLaporan.MENUNGGU);
        laporanMenunggu.setTanggalLapor(now.minusHours(2));
        laporanRepository.save(laporanMenunggu);

        LaporanKerusakan laporanDiproses = new LaporanKerusakan();
        laporanDiproses.setKodeLaporan(laporanService.generateNextKodeLaporan(today));
        laporanDiproses.setPenghuni(demoPenghuni);
        laporanDiproses.setKategoriFasilitas("Air & Sanitasi");
        laporanDiproses.setLokasiKamar("Kamar A-12 · Kamar mandi");
        laporanDiproses.setDeskripsi(
            "Keran kamar mandi bocor. Air terus menetes dari sambungan keran meskipun sudah ditutup rapat sejak pagi."
        );
        laporanDiproses.setStatus(StatusLaporan.DIPROSES);
        laporanDiproses.setCatatanPenanganan(
            "Ditugaskan ke teknisi air (Budi Santoso). Sedang penggantian seal dan sambungan keran."
        );
        laporanDiproses.setTanggalLapor(now.minusDays(1).withHour(9).withMinute(15));
        laporanDiproses.setTanggalDiproses(now.minusDays(1).withHour(10).withMinute(30));
        laporanRepository.save(laporanDiproses);

        LaporanKerusakan laporanSelesai = new LaporanKerusakan();
        laporanSelesai.setKodeLaporan(laporanService.generateNextKodeLaporan(today));
        laporanSelesai.setPenghuni(demoPenghuni);
        laporanSelesai.setKategoriFasilitas("Pintu & Kunci");
        laporanSelesai.setLokasiKamar("Kamar A-12 · Pintu utama");
        laporanSelesai.setDeskripsi(
            "Gagang pintu longgar. Sekrup pengunci pada gagang pintu kamar kendor sehingga pintu sulit dikunci dari luar."
        );
        laporanSelesai.setStatus(StatusLaporan.SELESAI);
        laporanSelesai.setCatatanPenanganan(
            "Sekrup gagang pintu telah dikencangkan kembali dan engsel sudah dilumasi."
        );
        laporanSelesai.setTanggalLapor(now.minusDays(3).withHour(14).withMinute(0));
        laporanSelesai.setTanggalDiproses(now.minusDays(3).withHour(16).withMinute(20));
        laporanSelesai.setTanggalSelesai(now.minusDays(2).withHour(11).withMinute(10));
        laporanRepository.saveAll(List.of(laporanSelesai));
    }
}
