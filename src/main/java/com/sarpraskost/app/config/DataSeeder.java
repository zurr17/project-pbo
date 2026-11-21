package com.sarpraskost.app.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.KondisiFasilitas;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.entity.StatusLaporan;
import com.sarpraskost.app.entity.User;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.UserRepository;
import com.sarpraskost.app.service.LaporanService;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LaporanKerusakanRepository laporanRepository;
    private final FasilitasKamarRepository fasilitasKamarRepository;
    private final LaporanService laporanService;

    public DataSeeder(
        UserRepository userRepository,
        LaporanKerusakanRepository laporanRepository,
        FasilitasKamarRepository fasilitasKamarRepository,
        LaporanService laporanService
    ) {
        this.userRepository = userRepository;
        this.laporanRepository = laporanRepository;
        this.fasilitasKamarRepository = fasilitasKamarRepository;
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

        userRepository.findByEmailIgnoreCase("owner@kost.com")
            .orElseGet(() -> userRepository.save(new User(
                "Bu Ratna",
                "owner@kost.com",
                "123",
                Role.OWNER,
                null
            )));

        userRepository.findByEmailIgnoreCase("budi@kost.com")
            .orElseGet(() -> userRepository.save(new User(
                "Pak Budi",
                "budi@kost.com",
                "123",
                Role.MEKANIK,
                "Teknisi Air & Sanitasi"
            )));

        userRepository.findByEmailIgnoreCase("dedi@kost.com")
            .orElseGet(() -> userRepository.save(new User(
                "Mas Dedi",
                "dedi@kost.com",
                "123",
                Role.MEKANIK,
                "Teknisi Listrik & Umum"
            )));

        User demoMekanik = userRepository.findByEmailIgnoreCase("mekanik@kost.com")
            .orElseGet(() -> userRepository.save(new User(
                "Pak Budi",
                "mekanik@kost.com",
                "123",
                Role.MEKANIK,
                "Teknisi Utama Kost"
            )));

        if (laporanRepository.countByPenghuniId(demoPenghuni.getId()) == 0) {
            seedDemoLaporan(demoPenghuni);
        }

        if (laporanRepository.countByMekanikId(demoMekanik.getId()) == 0) {
            seedDemoTugasMekanik(demoPenghuni, demoMekanik);
        }

        if (fasilitasKamarRepository.countByPenghuniId(demoPenghuni.getId()) == 0) {
            seedDemoFasilitas(demoPenghuni);
        }
    }

    private void seedDemoLaporan(User demoPenghuni) {

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

    private void seedDemoTugasMekanik(User demoPenghuni, User demoMekanik) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        LaporanKerusakan tugasAc = new LaporanKerusakan();
        tugasAc.setKodeLaporan(laporanService.generateNextKodeLaporan(today));
        tugasAc.setPenghuni(demoPenghuni);
        tugasAc.setKategoriFasilitas("AC & Pendingin");
        tugasAc.setLokasiKamar("Kamar A-12 · Unit AC Dinding");
        tugasAc.setDeskripsi(
            "AC kamar tidak dingin dan menetes. Udara terasa hangat sejak malam serta keluar tetesan air dari unit indoor."
        );
        tugasAc.setStatus(StatusLaporan.DIPROSES);
        tugasAc.setMekanikId(demoMekanik.getId());
        tugasAc.setNamaMekanik(demoMekanik.getNama());
        tugasAc.setPrioritas("Tinggi");
        tugasAc.setCatatanOwner("Periksa saluran drainase AC dan bersihkan filter evaporator.");
        tugasAc.setCatatanPenanganan(
            "Ditugaskan ke teknisi " + demoMekanik.getNama()
                + " (Prioritas: Tinggi). Catatan: Periksa saluran drainase AC dan bersihkan filter evaporator."
        );
        tugasAc.setTanggalLapor(now.minusHours(5));
        tugasAc.setTanggalDiproses(now.minusHours(3));
        laporanRepository.save(tugasAc);

        LaporanKerusakan tugasListrik = new LaporanKerusakan();
        tugasListrik.setKodeLaporan(laporanService.generateNextKodeLaporan(today));
        tugasListrik.setPenghuni(demoPenghuni);
        tugasListrik.setKategoriFasilitas("Listrik & Pencahayaan");
        tugasListrik.setLokasiKamar("Kamar A-12 · Meja Belajar");
        tugasListrik.setDeskripsi(
            "Stopkontak meja belajar longgar. Dudukan soket bergoyang saat steker dipasang dan bodi stopkontak terasa hangat."
        );
        tugasListrik.setStatus(StatusLaporan.DIPROSES);
        tugasListrik.setMekanikId(demoMekanik.getId());
        tugasListrik.setNamaMekanik(demoMekanik.getNama());
        tugasListrik.setPrioritas("Darurat");
        tugasListrik.setCatatanOwner("Matikan MCB kamar terlebih dahulu lalu kencangkan terminal kabel dan ganti inbow dus bila aus.");
        tugasListrik.setCatatanPenanganan(
            "Ditugaskan ke teknisi " + demoMekanik.getNama()
                + " (Prioritas: Darurat). Catatan: Matikan MCB kamar terlebih dahulu lalu kencangkan terminal kabel dan ganti inbow dus bila aus."
        );
        tugasListrik.setTanggalLapor(now.minusHours(8));
        tugasListrik.setTanggalDiproses(now.minusHours(6));
        laporanRepository.save(tugasListrik);
    }

    private void seedDemoFasilitas(User demoPenghuni) {
        fasilitasKamarRepository.saveAll(List.of(
            new FasilitasKamar(
                demoPenghuni,
                "AC LG 1/2 PK",
                "LG Dual Cool H05TN4",
                KondisiFasilitas.BAIK,
                "Unit AC dinding kamar, suhu dingin normal dan filter baru dibersihkan."
            ),
            new FasilitasKamar(
                demoPenghuni,
                "Kasur Springbed",
                "Central Deluxe 120x200",
                KondisiFasilitas.BAIK,
                "Kasur single lengkap dengan dipan kayu dan pelindung matras."
            ),
            new FasilitasKamar(
                demoPenghuni,
                "Keran Air Kamar Mandi",
                "Onda Brass 1/2 Inch",
                KondisiFasilitas.PERLU_PERAWATAN,
                "Air sedikit menetes dari sambungan leher keran saat ditutup rapat."
            )
        ));
    }
}
