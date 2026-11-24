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
import com.sarpraskost.app.entity.TingkatUrgensi;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.KamarRepository;
import com.sarpraskost.app.repository.KategoriFasilitasRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.NotifikasiRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import com.sarpraskost.app.repository.RiwayatPenangananRepository;
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

    public DataSeeder(
        KamarRepository kamarRepository,
        PenggunaRepository penggunaRepository,
        KategoriFasilitasRepository kategoriRepository,
        FasilitasKamarRepository fasilitasRepository,
        LaporanKerusakanRepository laporanRepository,
        RiwayatPenangananRepository riwayatRepository,
        NotifikasiRepository notifikasiRepository
    ) {
        this.kamarRepository = kamarRepository;
        this.penggunaRepository = penggunaRepository;
        this.kategoriRepository = kategoriRepository;
        this.fasilitasRepository = fasilitasRepository;
        this.laporanRepository = laporanRepository;
        this.riwayatRepository = riwayatRepository;
        this.notifikasiRepository = notifikasiRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (kamarRepository.count() == 0
            && penggunaRepository.count() == 0
            && kategoriRepository.count() == 0) {
            seedBaseSqlEquivalentData();
        } else {
            upgradeLegacyPlaceholderPasswords();
        }

        ensureFasilitasUmumAssets();
        syncUrgentFlagsFromTingkatUrgensi();
    }

    private void upgradeLegacyPlaceholderPasswords() {
        for (Pengguna user : penggunaRepository.findAll()) {
            String stored = user.getPasswordHash();
            if (stored == null || !stored.contains(":")) {
                user.setPasswordHash(PasswordHasher.hash("123"));
                penggunaRepository.save(user);
            }
        }
    }

    private void ensureFasilitasUmumAssets() {
        Kamar kamarUmum = kamarRepository.findByNomorKamarIgnoreCase("UMUM")
            .orElseGet(() -> kamarRepository.save(new Kamar(
                "UMUM",
                "Fasilitas Umum",
                (short) 1,
                StatusHunian.TERISI
            )));

        KategoriFasilitas katElektronik = kategoriRepository.findByNamaKategoriIgnoreCase("Elektronik")
            .orElseGet(() -> kategoriRepository.save(new KategoriFasilitas(
                "Elektronik",
                "Perangkat pendingin udara, pemanas air, dan elektronik kamar"
            )));

        KategoriFasilitas katSanitasi = kategoriRepository.findByNamaKategoriIgnoreCase("Sanitasi & Air")
            .orElseGet(() -> kategoriRepository.save(new KategoriFasilitas(
                "Sanitasi & Air",
                "Keran, shower, wastafel, kloset, dan pipa saluran air"
            )));

        FasilitasKamar routerWifi = fasilitasRepository.findByKodeAsetIgnoreCase("UMM-001")
            .orElseGet(() -> fasilitasRepository.save(new FasilitasKamar(
                kamarUmum,
                katElektronik,
                "UMM-001",
                "Router WiFi Lantai 1",
                "TP-Link Omada EAP225",
                KondisiFasilitas.BAIK,
                LocalDate.of(2025, 6, 15)
            )));

        if (!fasilitasRepository.existsByKodeAsetIgnoreCase("UMM-002")) {
            fasilitasRepository.save(new FasilitasKamar(
                kamarUmum,
                katSanitasi,
                "UMM-002",
                "Mesin Air / Pompa Utama",
                "Shimizu PS-230 BIT",
                KondisiFasilitas.BAIK,
                LocalDate.of(2025, 6, 15)
            ));
        }

        if (laporanRepository.countByFasilitasIdFasilitas(routerWifi.getIdFasilitas()) == 0) {
            seedSampleFasilitasUmumReport(routerWifi);
        }
    }

    private void seedSampleFasilitasUmumReport(FasilitasKamar routerWifi) {
        Pengguna pelapor = penggunaRepository.findByEmailIgnoreCase("raka@kost.id")
            .or(() -> penggunaRepository.findFirstByRoleOrderByIdPenggunaAsc(Role.PENGHUNI))
            .orElse(null);
        Pengguna owner = penggunaRepository.findFirstByRoleOrderByIdPenggunaAsc(Role.OWNER).orElse(null);
        if (pelapor == null || owner == null) {
            return;
        }
        String kode = "TKT-20261106-01";
        if (laporanRepository.findByKodeTiketIgnoreCase(kode).isPresent()) {
            return;
        }

        LocalDateTime waktu = LocalDateTime.of(2026, 11, 6, 9, 15);
        LaporanKerusakan lap = new LaporanKerusakan();
        lap.setKodeTiket(kode);
        lap.setJudulLaporan("WiFi Lantai 1 Sering Putus");
        lap.setDeskripsiKerusakan("Koneksi WiFi area koridor lantai 1 putus-putus sejak pagi dan lampu indikator berkedip merah.");
        lap.setFotoUrl("");
        lap.setTingkatUrgensi(TingkatUrgensi.TINGGI);
        lap.setStatusTiket(StatusTiket.MENUNGGU);
        lap.setTglLapor(waktu);
        lap.setPengguna(pelapor);
        lap.setFasilitas(routerWifi);
        lap.setJumlahTerdampak(2);
        lap.setUrgent(false);
        lap.setIzinMasukKamar(false);
        lap = laporanRepository.save(lap);

        if (routerWifi.getKondisi() == KondisiFasilitas.BAIK) {
            routerWifi.setKondisi(KondisiFasilitas.RUSAK_RINGAN);
            fasilitasRepository.save(routerWifi);
        }

        RiwayatPenanganan riwayat = new RiwayatPenanganan();
        riwayat.setLaporan(lap);
        riwayat.setPengguna(pelapor);
        riwayat.setStatusBaru(StatusTiket.MENUNGGU);
        riwayat.setCatatanPenanganan("Laporan kerusakan fasilitas umum dikirim oleh penghuni.");
        riwayat.setBiayaPerbaikan(BigDecimal.ZERO);
        riwayat.setWaktuUpdate(waktu);
        riwayatRepository.save(riwayat);

        Notifikasi notifOwner = new Notifikasi();
        notifOwner.setPengguna(owner);
        notifOwner.setLaporan(lap);
        notifOwner.setJudulNotif("Laporan Baru: WiFi Lantai 1 Sering Putus");
        notifOwner.setPesanNotif("Penghuni melaporkan gangguan fasilitas bersama pada Router WiFi Lantai 1.");
        notifOwner.setRead(false);
        notifOwner.setWaktuKirim(waktu);
        notifikasiRepository.save(notifOwner);
    }

    private void syncUrgentFlagsFromTingkatUrgensi() {
        for (LaporanKerusakan laporan : laporanRepository.findAll()) {
            if (laporan.getTingkatUrgensi() == TingkatUrgensi.DARURAT && !laporan.isUrgent()) {
                laporan.setUrgent(true);
                laporanRepository.save(laporan);
            }
        }
    }

    private void seedBaseSqlEquivalentData() {
        Kamar k1 = kamarRepository.save(new Kamar("A-101", "VIP AC", (short) 1, StatusHunian.TERISI));
        Kamar k2 = kamarRepository.save(new Kamar("A-102", "Reguler", (short) 1, StatusHunian.TERISI));
        Kamar k3 = kamarRepository.save(new Kamar("B-201", "VIP AC", (short) 2, StatusHunian.TERISI));
        kamarRepository.save(new Kamar("B-202", "Reguler", (short) 2, StatusHunian.KOSONG));

        Pengguna raka = penggunaRepository.save(new Pengguna(
            k1, "Raka Pratama", "raka@kost.id", PasswordHasher.hash("123"), "0812-3456-7890", Role.PENGHUNI
        ));
        Pengguna nadia = penggunaRepository.save(new Pengguna(
            k2, "Nadia Putri", "nadia@kost.id", PasswordHasher.hash("123"), "0857-9876-5432", Role.PENGHUNI
        ));
        Pengguna dimas = penggunaRepository.save(new Pengguna(
            k3, "Dimas Anggara", "dimas@kost.id", PasswordHasher.hash("123"), "0896-1122-3344", Role.PENGHUNI
        ));
        Pengguna budi = penggunaRepository.save(new Pengguna(
            null, "Budi Santoso", "budi.pengelola@kost.id", PasswordHasher.hash("123"), "0811-2233-4455", Role.OWNER
        ));

        KategoriFasilitas katElektronik = kategoriRepository.save(new KategoriFasilitas(
            "Elektronik", "Perangkat pendingin udara, pemanas air, dan elektronik kamar"
        ));
        KategoriFasilitas katKelistrikan = kategoriRepository.save(new KategoriFasilitas(
            "Kelistrikan", "Lampu, saklar, stop kontak, dan instalasi MCB"
        ));
        KategoriFasilitas katSanitasi = kategoriRepository.save(new KategoriFasilitas(
            "Sanitasi & Air", "Keran, shower, wastafel, kloset, dan pipa saluran air"
        ));
        KategoriFasilitas katFurnitur = kategoriRepository.save(new KategoriFasilitas(
            "Furnitur", "Kasur, lemari pakaian, meja belajar, dan kursi"
        ));

        FasilitasKamar f1 = fasilitasRepository.save(new FasilitasKamar(
            k1, katElektronik, "AST-001", "AC Split 1 PK", "Daikin FTC25", KondisiFasilitas.BAIK, LocalDate.of(2025, 6, 15)
        ));
        fasilitasRepository.save(new FasilitasKamar(
            k1, katKelistrikan, "AST-002", "Lampu LED 18W", "Philips MyCare", KondisiFasilitas.BAIK, LocalDate.of(2025, 6, 15)
        ));
        FasilitasKamar f3 = fasilitasRepository.save(new FasilitasKamar(
            k2, katSanitasi, "AST-003", "Keran Wastafel", "Onda V688", KondisiFasilitas.DALAM_PERBAIKAN, LocalDate.of(2025, 7, 1)
        ));
        FasilitasKamar f4 = fasilitasRepository.save(new FasilitasKamar(
            k3, katFurnitur, "AST-004", "Lemari Pakaian 2 Pintu", "Olympic", KondisiFasilitas.RUSAK_RINGAN, LocalDate.of(2025, 5, 20)
        ));

        LaporanKerusakan l1 = new LaporanKerusakan();
        l1.setKodeTiket("TKT-20261101-01");
        l1.setJudulLaporan("AC Tidak Dingin");
        l1.setDeskripsiKerusakan("AC hanya keluar angin biasa dan unit indoor berisik.");
        l1.setFotoUrl("/uploads/laporan/l001.jpg");
        l1.setTingkatUrgensi(TingkatUrgensi.TINGGI);
        l1.setStatusTiket(StatusTiket.SELESAI);
        l1.setTglLapor(LocalDateTime.of(2026, 11, 1, 8, 30));
        l1.setPengguna(raka);
        l1.setFasilitas(f1);
        l1 = laporanRepository.save(l1);

        LaporanKerusakan l2 = new LaporanKerusakan();
        l2.setKodeTiket("TKT-20261103-02");
        l2.setJudulLaporan("Keran Wastafel Patah");
        l2.setDeskripsiKerusakan("Air bocor terus menerus membasahi lantai kamar mandi.");
        l2.setFotoUrl("/uploads/laporan/l002.jpg");
        l2.setTingkatUrgensi(TingkatUrgensi.DARURAT);
        l2.setStatusTiket(StatusTiket.DIPROSES);
        l2.setTglLapor(LocalDateTime.of(2026, 11, 3, 7, 45));
        l2.setPengguna(nadia);
        l2.setFasilitas(f3);
        l2.setUrgent(true);
        l2.setIzinMasukKamar(true);
        l2.setJadwalPengerjaan(LocalDateTime.of(2026, 11, 4, 9, 0));
        l2 = laporanRepository.save(l2);

        LaporanKerusakan l3 = new LaporanKerusakan();
        l3.setKodeTiket("TKT-20261105-03");
        l3.setJudulLaporan("Engsel Lemari Lepas");
        l3.setDeskripsiKerusakan("Pintu lemari sebelah kanan miring dan sulit ditutup.");
        l3.setFotoUrl("/uploads/laporan/l003.jpg");
        l3.setTingkatUrgensi(TingkatUrgensi.SEDANG);
        l3.setStatusTiket(StatusTiket.MENUNGGU);
        l3.setTglLapor(LocalDateTime.of(2026, 11, 5, 16, 20));
        l3.setPengguna(dimas);
        l3.setFasilitas(f4);
        l3 = laporanRepository.save(l3);

        RiwayatPenanganan r1 = new RiwayatPenanganan();
        r1.setLaporan(l1);
        r1.setPengguna(budi);
        r1.setStatusBaru(StatusTiket.DIPROSES);
        r1.setCatatanPenanganan("Pengelola melakukan pengecekan tekanan freon dan evaporator.");
        r1.setBiayaPerbaikan(BigDecimal.ZERO);
        r1.setWaktuUpdate(LocalDateTime.of(2026, 11, 1, 10, 15));

        RiwayatPenanganan r2 = new RiwayatPenanganan();
        r2.setLaporan(l1);
        r2.setPengguna(budi);
        r2.setStatusBaru(StatusTiket.SELESAI);
        r2.setCatatanPenanganan("Pengisian ulang freon R32 dan pembersihan filter AC selesai.");
        r2.setFotoSesudahUrl("/uploads/selesai/s001.jpg");
        r2.setBiayaPerbaikan(new BigDecimal("150000.00"));
        r2.setWaktuUpdate(LocalDateTime.of(2026, 11, 2, 14, 0));

        RiwayatPenanganan r3 = new RiwayatPenanganan();
        r3.setLaporan(l2);
        r3.setPengguna(budi);
        r3.setStatusBaru(StatusTiket.DIPROSES);
        r3.setCatatanPenanganan("Penutupan katup air sementara dan pembelian unit keran baru.");
        r3.setBiayaPerbaikan(new BigDecimal("45000.00"));
        r3.setWaktuUpdate(LocalDateTime.of(2026, 11, 3, 9, 30));
        riwayatRepository.saveAll(List.of(r1, r2, r3));

        Notifikasi n1 = new Notifikasi();
        n1.setPengguna(budi);
        n1.setLaporan(l1);
        n1.setJudulNotif("Laporan Baru: AC Tidak Dingin");
        n1.setPesanNotif("Penghuni kamar A-101 melaporkan kerusakan pada AC Split 1 PK.");
        n1.setRead(true);
        n1.setWaktuKirim(LocalDateTime.of(2026, 11, 1, 8, 31));

        Notifikasi n2 = new Notifikasi();
        n2.setPengguna(raka);
        n2.setLaporan(l1);
        n2.setJudulNotif("Perbaikan Tiket Selesai");
        n2.setPesanNotif("Tiket TKT-20261101-01 telah selesai diperbaiki oleh pengelola.");
        n2.setRead(true);
        n2.setWaktuKirim(LocalDateTime.of(2026, 11, 2, 14, 1));

        Notifikasi n3 = new Notifikasi();
        n3.setPengguna(budi);
        n3.setLaporan(l2);
        n3.setJudulNotif("Laporan Darurat Masuk");
        n3.setPesanNotif("Terdapat laporan kerusakan sanitasi darurat di kamar A-102. Izin masuk kamar telah disetujui.");
        n3.setRead(false);
        n3.setWaktuKirim(LocalDateTime.of(2026, 11, 3, 7, 46));
        notifikasiRepository.saveAll(List.of(n1, n2, n3));
    }
}
