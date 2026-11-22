package com.sarpraskost.app.config;

import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.Kamar;
import com.sarpraskost.app.entity.KategoriFasilitas;
import com.sarpraskost.app.entity.KondisiFasilitas;
import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.Notifikasi;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.RiwayatPenanganan;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.entity.StatusHunian;
import com.sarpraskost.app.entity.StatusTiket;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.KamarRepository;
import com.sarpraskost.app.repository.KategoriFasilitasRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.NotifikasiRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import com.sarpraskost.app.repository.RiwayatPenangananRepository;
import com.sarpraskost.app.service.LaporanService;
import com.sarpraskost.app.util.PasswordHasher;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final KamarRepository kamarRepository;
    private final PenggunaRepository penggunaRepository;
    private final KategoriFasilitasRepository kategoriRepository;
    private final FasilitasKamarRepository fasilitasRepository;
    private final LaporanKerusakanRepository laporanRepository;
    private final RiwayatPenangananRepository riwayatRepository;
    private final NotifikasiRepository notifikasiRepository;
    private final LaporanService laporanService;

    public DataSeeder(
        KamarRepository kamarRepository,
        PenggunaRepository penggunaRepository,
        KategoriFasilitasRepository kategoriRepository,
        FasilitasKamarRepository fasilitasRepository,
        LaporanKerusakanRepository laporanRepository,
        RiwayatPenangananRepository riwayatRepository,
        NotifikasiRepository notifikasiRepository,
        LaporanService laporanService
    ) {
        this.kamarRepository = kamarRepository;
        this.penggunaRepository = penggunaRepository;
        this.kategoriRepository = kategoriRepository;
        this.fasilitasRepository = fasilitasRepository;
        this.laporanRepository = laporanRepository;
        this.riwayatRepository = riwayatRepository;
        this.notifikasiRepository = notifikasiRepository;
        this.laporanService = laporanService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Kamar kamarA01 = kamarRepository.findByNomorKamarIgnoreCase("A-01")
            .orElseGet(() -> kamarRepository.save(new Kamar(
                "A-01",
                "VIP",
                1,
                StatusHunian.TERISI
            )));

        kamarRepository.findByNomorKamarIgnoreCase("B-02")
            .orElseGet(() -> kamarRepository.save(new Kamar(
                "B-02",
                "Reguler",
                2,
                StatusHunian.KOSONG
            )));

        Pengguna penghuni = penggunaRepository.findByEmailIgnoreCase("penghuni@kost.com")
            .orElseGet(() -> penggunaRepository.save(new Pengguna(
                kamarA01,
                "Akbar",
                "penghuni@kost.com",
                PasswordHasher.hash("123"),
                "081234567890",
                Role.PENGHUNI
            )));
        if (!"Akbar".equals(penghuni.getNamaLengkap())) {
            penghuni.setNamaLengkap("Akbar");
            penghuni = penggunaRepository.save(penghuni);
        }

        Pengguna owner = penggunaRepository.findByEmailIgnoreCase("owner@kost.com")
            .orElseGet(() -> penggunaRepository.save(new Pengguna(
                null,
                "Pak Soleh",
                "owner@kost.com",
                PasswordHasher.hash("123"),
                "081198765432",
                Role.OWNER
            )));
        if (!"Pak Soleh".equals(owner.getNamaLengkap())) {
            owner.setNamaLengkap("Pak Soleh");
            owner = penggunaRepository.save(owner);
        }

        KategoriFasilitas katElektronik = kategoriRepository.findByNamaKategoriIgnoreCase("Elektronik")
            .orElseGet(() -> kategoriRepository.save(new KategoriFasilitas(
                "Elektronik",
                "Perangkat elektronik kamar seperti AC, lampu, TV, dan stopkontak."
            )));

        KategoriFasilitas katSanitasi = kategoriRepository.findByNamaKategoriIgnoreCase("Sanitasi & Air")
            .orElseGet(() -> kategoriRepository.save(new KategoriFasilitas(
                "Sanitasi & Air",
                "Perlengkapan kamar mandi, keran air, shower, wastafel, dan saluran air."
            )));

        KategoriFasilitas katFurnitur = kategoriRepository.findByNamaKategoriIgnoreCase("Furnitur")
            .orElseGet(() -> kategoriRepository.save(new KategoriFasilitas(
                "Furnitur",
                "Perabotan kamar seperti lemari pakaian, tempat tidur, meja belajar, dan kursi."
            )));

        if (fasilitasRepository.countByKamarIdKamar(kamarA01.getIdKamar()) == 0) {
            seedFasilitasKamarA01(kamarA01, katElektronik, katSanitasi, katFurnitur);
        }

        if (laporanRepository.count() == 0) {
            seedSampleLaporan(kamarA01, penghuni, owner);
        } else {
            for (Notifikasi notif : notifikasiRepository.findAll()) {
                String updatedMsg = notif.getPesanNotif()
                    .replace("Andi Pratama", "Akbar")
                    .replace("Bu Ratna", "Pak Soleh");
                if (!updatedMsg.equals(notif.getPesanNotif())) {
                    notif.setPesanNotif(updatedMsg);
                    notifikasiRepository.save(notif);
                }
            }
        }
    }

    private void seedFasilitasKamarA01(
        Kamar kamarA01,
        KategoriFasilitas katElektronik,
        KategoriFasilitas katSanitasi,
        KategoriFasilitas katFurnitur
    ) {
        LocalDate today = LocalDate.now();
        fasilitasRepository.saveAll(List.of(
            new FasilitasKamar(
                kamarA01,
                katElektronik,
                "AST-A01-01",
                "AC Samsung 1/2 PK",
                "Samsung AR05BGHQASINSE",
                KondisiFasilitas.PERLU_PERAWATAN,
                today.minusMonths(8)
            ),
            new FasilitasKamar(
                kamarA01,
                katSanitasi,
                "AST-A01-02",
                "Keran Kamar Mandi",
                "Onda Brass 1/2 Inch",
                KondisiFasilitas.RUSAK,
                today.minusMonths(14)
            ),
            new FasilitasKamar(
                kamarA01,
                katFurnitur,
                "AST-A01-03",
                "Lemari Pakaian",
                "Olympic 2 Pintu Minimalis",
                KondisiFasilitas.BAIK,
                today.minusMonths(12)
            )
        ));
    }

    private void seedSampleLaporan(Kamar kamarA01, Pengguna penghuni, Pengguna owner) {
        List<FasilitasKamar> fasilitasList = fasilitasRepository.findByKamarIdKamarOrderByIdFasilitasDesc(kamarA01.getIdKamar());
        FasilitasKamar ac = fasilitasList.stream()
            .filter(f -> f.getNamaFasilitas().contains("AC"))
            .findFirst()
            .orElse(fasilitasList.get(0));
        FasilitasKamar keran = fasilitasList.stream()
            .filter(f -> f.getNamaFasilitas().contains("Keran"))
            .findFirst()
            .orElse(fasilitasList.get(0));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Laporan 1: MENUNGGU
        LaporanKerusakan lap1 = new LaporanKerusakan();
        lap1.setPengguna(penghuni);
        lap1.setFasilitas(keran);
        lap1.setKodeTiket(laporanService.generateNextKodeTiket(today));
        lap1.setJudulLaporan("Keran kamar mandi bocor");
        lap1.setDeskripsiKerusakan(
            "Air terus menetes dari sambungan leher keran kamar mandi meskipun tuas sudah ditutup rapat sejak pagi."
        );
        lap1.setTglLapor(now.minusHours(3));
        lap1.setStatusTiket(StatusTiket.MENUNGGU);
        lap1 = laporanRepository.save(lap1);

        RiwayatPenanganan r1 = new RiwayatPenanganan();
        r1.setLaporan(lap1);
        r1.setPengguna(penghuni);
        r1.setStatusBaru(StatusTiket.MENUNGGU);
        r1.setCatatanPenanganan("Laporan kerusakan dikirim oleh penghuni.");
        r1.setWaktuUpdate(now.minusHours(3));
        riwayatRepository.save(r1);

        Notifikasi n1 = new Notifikasi();
        n1.setPengguna(owner);
        n1.setLaporan(lap1);
        n1.setJudulNotif("Laporan Baru: Keran kamar mandi bocor");
        n1.setPesanNotif("Akbar (Kamar A-01) melaporkan kerusakan pada Keran Kamar Mandi.");
        n1.setRead(false);
        n1.setWaktuKirim(now.minusHours(3));
        notifikasiRepository.save(n1);

        // Laporan 2: DIPROSES
        LaporanKerusakan lap2 = new LaporanKerusakan();
        lap2.setPengguna(penghuni);
        lap2.setFasilitas(ac);
        lap2.setKodeTiket(laporanService.generateNextKodeTiket(today));
        lap2.setJudulLaporan("AC kamar kurang dingin dan menetes");
        lap2.setDeskripsiKerusakan(
            "Hembusan udara AC terasa tidak dingin sejak semalam dan terdapat tetesan air dari unit indoor."
        );
        lap2.setTglLapor(now.minusDays(1));
        lap2.setStatusTiket(StatusTiket.DIPROSES);
        lap2 = laporanRepository.save(lap2);

        RiwayatPenanganan r2a = new RiwayatPenanganan();
        r2a.setLaporan(lap2);
        r2a.setPengguna(penghuni);
        r2a.setStatusBaru(StatusTiket.MENUNGGU);
        r2a.setCatatanPenanganan("Laporan kerusakan dikirim oleh penghuni.");
        r2a.setWaktuUpdate(now.minusDays(1));

        RiwayatPenanganan r2b = new RiwayatPenanganan();
        r2b.setLaporan(lap2);
        r2b.setPengguna(owner);
        r2b.setStatusBaru(StatusTiket.DIPROSES);
        r2b.setCatatanPenanganan(
            "Laporan disetujui pengelola. Jadwal pembersihan filter dan pengecekan freon dilakukan sore ini."
        );
        r2b.setBiayaPerbaikan(BigDecimal.valueOf(150000));
        r2b.setWaktuUpdate(now.minusHours(18));
        riwayatRepository.saveAll(List.of(r2a, r2b));

        Notifikasi n2Owner = new Notifikasi();
        n2Owner.setPengguna(owner);
        n2Owner.setLaporan(lap2);
        n2Owner.setJudulNotif("Laporan Baru: AC kamar kurang dingin dan menetes");
        n2Owner.setPesanNotif("Akbar (Kamar A-01) melaporkan kerusakan pada AC Samsung 1/2 PK.");
        n2Owner.setRead(true);
        n2Owner.setWaktuKirim(now.minusDays(1));

        Notifikasi n2Penghuni = new Notifikasi();
        n2Penghuni.setPengguna(penghuni);
        n2Penghuni.setLaporan(lap2);
        n2Penghuni.setJudulNotif("Laporan Diproses");
        n2Penghuni.setPesanNotif(
            "Laporan AC kamar kurang dingin dan menetes sedang ditangani oleh Pak Soleh. Estimasi biaya: Rp 150.000."
        );
        n2Penghuni.setRead(false);
        n2Penghuni.setWaktuKirim(now.minusHours(18));
        notifikasiRepository.saveAll(List.of(n2Owner, n2Penghuni));
    }
}
