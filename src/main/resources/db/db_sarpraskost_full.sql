-- Skrip setup penuh PostgreSQL untuk Sarpraskost (Kost Lodaya 9)
-- Selaras 100% dengan 7 tabel ERD, konfigurasi 65 kamar (Gedung 1: 34 kamar, Gedung 2: 31 kamar), & aturan bisnis final.

DROP TABLE IF EXISTS public.notifikasi CASCADE;
DROP TABLE IF EXISTS public.riwayat_penanganan CASCADE;
DROP TABLE IF EXISTS public.laporan_kerusakan CASCADE;
DROP TABLE IF EXISTS public.fasilitas_kamar CASCADE;
DROP TABLE IF EXISTS public.kategori_fasilitas CASCADE;
DROP TABLE IF EXISTS public.pengguna CASCADE;
DROP TABLE IF EXISTS public.kamar CASCADE;

CREATE TABLE public.kamar (
    id_kamar integer NOT NULL GENERATED ALWAYS AS IDENTITY,
    gedung character varying(20) DEFAULT 'Gedung 1'::character varying NOT NULL,
    nomor_kamar character varying(10) NOT NULL,
    lantai smallint NOT NULL,
    tipe_kamar character varying(30) NOT NULL,
    status_hunian character varying(15) DEFAULT 'Kosong'::character varying NOT NULL,
    keterangan text,
    CONSTRAINT kamar_pkey PRIMARY KEY (id_kamar),
    CONSTRAINT kamar_gedung_nomor_key UNIQUE (gedung, nomor_kamar),
    CONSTRAINT kamar_lantai_check CHECK (lantai > 0),
    CONSTRAINT kamar_status_hunian_check CHECK ((status_hunian)::text = ANY ((ARRAY['Terisi'::character varying, 'Kosong'::character varying, 'Perbaikan'::character varying, 'Pembangunan'::character varying])::text[]))
);

CREATE TABLE public.pengguna (
    id_pengguna integer NOT NULL GENERATED ALWAYS AS IDENTITY,
    nama_lengkap character varying(80) NOT NULL,
    email character varying(100) NOT NULL,
    no_hp character varying(20) NOT NULL,
    password_hash character varying(255) NOT NULL,
    role character varying(15) NOT NULL,
    id_kamar integer,
    CONSTRAINT pengguna_pkey PRIMARY KEY (id_pengguna),
    CONSTRAINT pengguna_email_key UNIQUE (email),
    CONSTRAINT pengguna_no_hp_key UNIQUE (no_hp),
    CONSTRAINT pengguna_role_check CHECK ((role)::text = ANY ((ARRAY['Penghuni'::character varying, 'Pengelola'::character varying])::text[])),
    CONSTRAINT pengguna_id_kamar_fkey FOREIGN KEY (id_kamar) REFERENCES public.kamar(id_kamar) ON DELETE SET NULL
);

CREATE TABLE public.kategori_fasilitas (
    id_kategori integer NOT NULL GENERATED ALWAYS AS IDENTITY,
    nama_kategori character varying(50) NOT NULL,
    deskripsi text,
    CONSTRAINT kategori_fasilitas_pkey PRIMARY KEY (id_kategori),
    CONSTRAINT kategori_fasilitas_nama_kategori_key UNIQUE (nama_kategori)
);

CREATE TABLE public.fasilitas_kamar (
    id_fasilitas integer NOT NULL GENERATED ALWAYS AS IDENTITY,
    kode_aset character varying(20) NOT NULL,
    nama_fasilitas character varying(80) NOT NULL,
    merk_tipe character varying(60),
    kondisi character varying(20) DEFAULT 'Baik'::character varying NOT NULL,
    tgl_pasang date DEFAULT CURRENT_DATE,
    id_kamar integer NOT NULL,
    id_kategori integer NOT NULL,
    CONSTRAINT fasilitas_kamar_pkey PRIMARY KEY (id_fasilitas),
    CONSTRAINT fasilitas_kamar_kode_aset_key UNIQUE (kode_aset),
    CONSTRAINT fasilitas_kamar_kondisi_check CHECK ((kondisi)::text = ANY ((ARRAY['Baik'::character varying, 'Rusak Ringan'::character varying, 'Rusak Berat'::character varying, 'Dalam Perbaikan'::character varying])::text[])),
    CONSTRAINT fasilitas_kamar_id_kamar_fkey FOREIGN KEY (id_kamar) REFERENCES public.kamar(id_kamar) ON DELETE CASCADE,
    CONSTRAINT fasilitas_kamar_id_kategori_fkey FOREIGN KEY (id_kategori) REFERENCES public.kategori_fasilitas(id_kategori) ON DELETE RESTRICT
);

CREATE TABLE public.laporan_kerusakan (
    id_laporan integer NOT NULL GENERATED ALWAYS AS IDENTITY,
    kode_tiket character varying(25) NOT NULL,
    judul_laporan character varying(100) NOT NULL,
    deskripsi_kerusakan text NOT NULL,
    foto_url character varying(255) NOT NULL,
    tingkat_urgensi character varying(15) DEFAULT 'Sedang'::character varying NOT NULL,
    status_tiket character varying(15) DEFAULT 'Menunggu'::character varying NOT NULL,
    tgl_lapor timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_pengguna integer NOT NULL,
    id_fasilitas integer NOT NULL,
    jumlah_terdampak integer DEFAULT 1 NOT NULL,
    jadwal_pengerjaan timestamp without time zone,
    is_urgent boolean DEFAULT false NOT NULL,
    izin_masuk_kamar boolean DEFAULT false NOT NULL,
    CONSTRAINT laporan_kerusakan_pkey PRIMARY KEY (id_laporan),
    CONSTRAINT laporan_kerusakan_kode_tiket_key UNIQUE (kode_tiket),
    CONSTRAINT laporan_kerusakan_tingkat_urgensi_check CHECK ((tingkat_urgensi)::text = ANY ((ARRAY['Rendah'::character varying, 'Sedang'::character varying, 'Tinggi'::character varying, 'Darurat'::character varying])::text[])),
    CONSTRAINT laporan_kerusakan_status_tiket_check CHECK ((status_tiket)::text = ANY ((ARRAY['Menunggu'::character varying, 'Diproses'::character varying, 'Selesai'::character varying, 'Ditolak'::character varying])::text[])),
    CONSTRAINT laporan_kerusakan_jumlah_terdampak_check CHECK (jumlah_terdampak >= 1),
    CONSTRAINT laporan_kerusakan_id_pengguna_fkey FOREIGN KEY (id_pengguna) REFERENCES public.pengguna(id_pengguna) ON DELETE RESTRICT,
    CONSTRAINT laporan_kerusakan_id_fasilitas_fkey FOREIGN KEY (id_fasilitas) REFERENCES public.fasilitas_kamar(id_fasilitas) ON DELETE RESTRICT
);

CREATE TABLE public.riwayat_penanganan (
    id_riwayat integer NOT NULL GENERATED ALWAYS AS IDENTITY,
    id_laporan integer NOT NULL,
    id_pengguna integer NOT NULL,
    status_baru character varying(15) NOT NULL,
    catatan_penanganan text NOT NULL,
    foto_sesudah_url character varying(255),
    biaya_perbaikan numeric(12,2) DEFAULT 0 NOT NULL,
    waktu_update timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT riwayat_penanganan_pkey PRIMARY KEY (id_riwayat),
    CONSTRAINT riwayat_penanganan_status_baru_check CHECK ((status_baru)::text = ANY ((ARRAY['Menunggu'::character varying, 'Diproses'::character varying, 'Selesai'::character varying, 'Ditolak'::character varying])::text[])),
    CONSTRAINT riwayat_penanganan_biaya_perbaikan_check CHECK (biaya_perbaikan >= (0)::numeric),
    CONSTRAINT riwayat_penanganan_id_laporan_fkey FOREIGN KEY (id_laporan) REFERENCES public.laporan_kerusakan(id_laporan) ON DELETE CASCADE,
    CONSTRAINT riwayat_penanganan_id_pengguna_fkey FOREIGN KEY (id_pengguna) REFERENCES public.pengguna(id_pengguna) ON DELETE RESTRICT
);

CREATE TABLE public.notifikasi (
    id_notifikasi integer NOT NULL GENERATED ALWAYS AS IDENTITY,
    id_pengguna integer NOT NULL,
    id_laporan integer NOT NULL,
    judul_notif character varying(100) NOT NULL,
    pesan_notif text NOT NULL,
    is_read boolean DEFAULT false NOT NULL,
    waktu_kirim timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT notifikasi_pkey PRIMARY KEY (id_notifikasi),
    CONSTRAINT notifikasi_id_pengguna_fkey FOREIGN KEY (id_pengguna) REFERENCES public.pengguna(id_pengguna) ON DELETE CASCADE,
    CONSTRAINT notifikasi_id_laporan_fkey FOREIGN KEY (id_laporan) REFERENCES public.laporan_kerusakan(id_laporan) ON DELETE CASCADE
);

-- Master Data 65 Kamar Kost Lodaya 9
-- Gedung 1 (34 Kamar Aktif / Tersedia: Lantai 1 = 8, Lantai 2 = 13, Lantai 3 = 13)
INSERT INTO public.kamar (id_kamar, gedung, nomor_kamar, lantai, tipe_kamar, status_hunian, keterangan) OVERRIDING SYSTEM VALUE VALUES
(1,  'Gedung 1', '101', 1, 'Standar', 'Terisi', NULL),
(2,  'Gedung 1', '102', 1, 'Standar', 'Terisi', NULL),
(3,  'Gedung 1', '103', 1, 'Standar', 'Kosong', NULL),
(4,  'Gedung 1', '104', 1, 'Standar', 'Kosong', NULL),
(5,  'Gedung 1', '105', 1, 'Standar', 'Kosong', NULL),
(6,  'Gedung 1', '106', 1, 'Standar', 'Kosong', NULL),
(7,  'Gedung 1', '107', 1, 'Standar', 'Kosong', NULL),
(8,  'Gedung 1', '108', 1, 'Standar', 'Kosong', NULL),
(9,  'Gedung 1', '201', 2, 'Standar', 'Terisi', NULL),
(10, 'Gedung 1', '202', 2, 'Standar', 'Kosong', NULL),
(11, 'Gedung 1', '203', 2, 'Standar', 'Kosong', NULL),
(12, 'Gedung 1', '204', 2, 'Standar', 'Kosong', NULL),
(13, 'Gedung 1', '205', 2, 'Standar', 'Kosong', NULL),
(14, 'Gedung 1', '206', 2, 'Standar', 'Kosong', NULL),
(15, 'Gedung 1', '207', 2, 'Standar', 'Kosong', NULL),
(16, 'Gedung 1', '208', 2, 'Standar', 'Kosong', NULL),
(17, 'Gedung 1', '209', 2, 'Standar', 'Kosong', NULL),
(18, 'Gedung 1', '210', 2, 'Standar', 'Kosong', NULL),
(19, 'Gedung 1', '211', 2, 'Standar', 'Kosong', NULL),
(20, 'Gedung 1', '212', 2, 'Standar', 'Kosong', NULL),
(21, 'Gedung 1', '213', 2, 'Standar', 'Kosong', NULL),
(22, 'Gedung 1', '301', 3, 'Standar', 'Kosong', NULL),
(23, 'Gedung 1', '302', 3, 'Standar', 'Kosong', NULL),
(24, 'Gedung 1', '303', 3, 'Standar', 'Kosong', NULL),
(25, 'Gedung 1', '304', 3, 'Standar', 'Kosong', NULL),
(26, 'Gedung 1', '305', 3, 'Standar', 'Kosong', NULL),
(27, 'Gedung 1', '306', 3, 'Standar', 'Kosong', NULL),
(28, 'Gedung 1', '307', 3, 'Standar', 'Kosong', NULL),
(29, 'Gedung 1', '308', 3, 'Standar', 'Kosong', NULL),
(30, 'Gedung 1', '309', 3, 'Standar', 'Kosong', NULL),
(31, 'Gedung 1', '310', 3, 'Standar', 'Kosong', NULL),
(32, 'Gedung 1', '311', 3, 'Standar', 'Kosong', NULL),
(33, 'Gedung 1', '312', 3, 'Standar', 'Kosong', NULL),
(34, 'Gedung 1', '313', 3, 'Standar', 'Kosong', NULL),
-- Gedung 2 (31 Kamar - On Progress Pembangunan: Lantai 1 = 9, Lantai 2 = 11, Lantai 3 = 11)
(35, 'Gedung 2', '101', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(36, 'Gedung 2', '102', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(37, 'Gedung 2', '103', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(38, 'Gedung 2', '104', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(39, 'Gedung 2', '105', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(40, 'Gedung 2', '106', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(41, 'Gedung 2', '107', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(42, 'Gedung 2', '108', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(43, 'Gedung 2', '109', 1, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(44, 'Gedung 2', '201', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(45, 'Gedung 2', '202', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(46, 'Gedung 2', '203', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(47, 'Gedung 2', '204', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(48, 'Gedung 2', '205', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(49, 'Gedung 2', '206', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(50, 'Gedung 2', '207', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(51, 'Gedung 2', '208', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(52, 'Gedung 2', '209', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(53, 'Gedung 2', '210', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(54, 'Gedung 2', '211', 2, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(55, 'Gedung 2', '301', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(56, 'Gedung 2', '302', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(57, 'Gedung 2', '303', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(58, 'Gedung 2', '304', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(59, 'Gedung 2', '305', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(60, 'Gedung 2', '306', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(61, 'Gedung 2', '307', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(62, 'Gedung 2', '308', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(63, 'Gedung 2', '309', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(64, 'Gedung 2', '310', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli'),
(65, 'Gedung 2', '311', 3, 'Standar', 'Pembangunan', 'On Progress Pembangunan - Perkiraan Ready Awal Juli');

INSERT INTO public.pengguna (id_pengguna, nama_lengkap, email, no_hp, password_hash, role, id_kamar) OVERRIDING SYSTEM VALUE VALUES
(1, 'Raka Pratama', 'raka@kost.id', '0812-3456-7890', '$2y$10$hashedpass01', 'Penghuni', 1),
(2, 'Nadia Putri', 'nadia@kost.id', '0857-9876-5432', '$2y$10$hashedpass02', 'Penghuni', 2),
(3, 'Dimas Anggara', 'dimas@kost.id', '0896-1122-3344', '$2y$10$hashedpass03', 'Penghuni', 9),
(4, 'Pak Soleh', 'soleh.pengelola@kost.id', '0811-2233-4455', '$2y$10$hashedpass04', 'Pengelola', NULL);

INSERT INTO public.kategori_fasilitas (id_kategori, nama_kategori, deskripsi) OVERRIDING SYSTEM VALUE VALUES
(1, 'Elektronik', 'Perangkat pendingin udara, pemanas air, dan elektronik kamar'),
(2, 'Kelistrikan', 'Lampu, saklar, stop kontak, dan instalasi MCB'),
(3, 'Sanitasi & Air', 'Keran, shower, wastafel, kloset, dan pipa saluran air'),
(4, 'Furnitur', 'Kasur, lemari pakaian, meja belajar, dan kursi');

INSERT INTO public.fasilitas_kamar (id_fasilitas, kode_aset, nama_fasilitas, merk_tipe, kondisi, tgl_pasang, id_kamar, id_kategori) OVERRIDING SYSTEM VALUE VALUES
(1, 'AST-001', 'AC Split 1 PK', 'Daikin FTC25', 'Baik', '2025-06-15', 1, 1),
(2, 'AST-002', 'Lampu LED 18W', 'Philips MyCare', 'Baik', '2025-06-15', 1, 2),
(3, 'AST-003', 'Keran Wastafel', 'Onda V688', 'Dalam Perbaikan', '2025-07-01', 2, 3),
(4, 'AST-004', 'Lemari Pakaian 2 Pintu', 'Olympic', 'Rusak Ringan', '2025-05-20', 9, 4);

INSERT INTO public.laporan_kerusakan (id_laporan, kode_tiket, judul_laporan, deskripsi_kerusakan, foto_url, tingkat_urgensi, status_tiket, tgl_lapor, id_pengguna, id_fasilitas, jumlah_terdampak, jadwal_pengerjaan, is_urgent, izin_masuk_kamar) OVERRIDING SYSTEM VALUE VALUES
(1, 'TKT-20261101-01', 'AC Tidak Dingin', 'AC hanya keluar angin biasa dan unit indoor berisik.', '/uploads/laporan/l001.jpg', 'Tinggi', 'Selesai', '2026-11-01 08:30:00', 1, 1, 1, NULL, false, false),
(2, 'TKT-20261103-02', 'Keran Wastafel Patah', 'Air bocor terus menerus membasahi lantai kamar mandi.', '/uploads/laporan/l002.jpg', 'Darurat', 'Diproses', '2026-11-03 07:45:00', 2, 3, 1, '2026-11-04 09:00:00', true, true),
(3, 'TKT-20261105-03', 'Engsel Lemari Lepas', 'Pintu lemari sebelah kanan miring dan sulit ditutup.', '/uploads/laporan/l003.jpg', 'Sedang', 'Menunggu', '2026-11-05 16:20:00', 3, 4, 1, NULL, false, false);

INSERT INTO public.riwayat_penanganan (id_riwayat, id_laporan, id_pengguna, status_baru, catatan_penanganan, foto_sesudah_url, biaya_perbaikan, waktu_update) OVERRIDING SYSTEM VALUE VALUES
(1, 1, 4, 'Diproses', 'Pengelola melakukan pengecekan tekanan freon dan evaporator.', NULL, 0.00, '2026-11-01 10:15:00'),
(2, 1, 4, 'Selesai', 'Pengisian ulang freon R32 dan pembersihan filter AC selesai.', '/uploads/selesai/s001.jpg', 150000.00, '2026-11-02 14:00:00'),
(3, 2, 4, 'Diproses', 'Penutupan katup air sementara dan pembelian unit keran baru.', NULL, 45000.00, '2026-11-03 09:30:00');

INSERT INTO public.notifikasi (id_notifikasi, id_pengguna, id_laporan, judul_notif, pesan_notif, is_read, waktu_kirim) OVERRIDING SYSTEM VALUE VALUES
(1, 4, 1, 'Laporan Baru: AC Tidak Dingin', 'Penghuni kamar 101 (Gedung 1) melaporkan kerusakan pada AC Split 1 PK.', true, '2026-11-01 08:31:00'),
(2, 1, 1, 'Perbaikan Tiket Selesai', 'Tiket TKT-20261101-01 telah selesai diperbaiki oleh pengelola.', true, '2026-11-02 14:01:00'),
(3, 4, 2, 'Laporan Darurat Masuk', 'Terdapat laporan kerusakan sanitasi darurat di kamar 102 (Gedung 1).', false, '2026-11-03 07:46:00');

SELECT pg_catalog.setval('public.kamar_id_kamar_seq', (SELECT COALESCE(MAX(id_kamar), 1) FROM public.kamar), true);
SELECT pg_catalog.setval('public.pengguna_id_pengguna_seq', (SELECT COALESCE(MAX(id_pengguna), 1) FROM public.pengguna), true);
SELECT pg_catalog.setval('public.kategori_fasilitas_id_kategori_seq', (SELECT COALESCE(MAX(id_kategori), 1) FROM public.kategori_fasilitas), true);
SELECT pg_catalog.setval('public.fasilitas_kamar_id_fasilitas_seq', (SELECT COALESCE(MAX(id_fasilitas), 1) FROM public.fasilitas_kamar), true);
SELECT pg_catalog.setval('public.laporan_kerusakan_id_laporan_seq', (SELECT COALESCE(MAX(id_laporan), 1) FROM public.laporan_kerusakan), true);
SELECT pg_catalog.setval('public.riwayat_penanganan_id_riwayat_seq', (SELECT COALESCE(MAX(id_riwayat), 1) FROM public.riwayat_penanganan), true);
SELECT pg_catalog.setval('public.notifikasi_id_notifikasi_seq', (SELECT COALESCE(MAX(id_notifikasi), 1) FROM public.notifikasi), true);

-- Tambahan Fasilitas Umum / Bersama agar mendukung fitur Gangguan Fasilitas Umum (+1 Terdampak)
INSERT INTO public.kamar (gedung, nomor_kamar, lantai, tipe_kamar, status_hunian, keterangan)
VALUES ('Umum', 'UMUM', 1, 'Fasilitas Umum', 'Terisi', 'Area fasilitas bersama penghuni Kost Lodaya 9')
ON CONFLICT (gedung, nomor_kamar) DO NOTHING;

INSERT INTO public.fasilitas_kamar (kode_aset, nama_fasilitas, merk_tipe, kondisi, tgl_pasang, id_kamar, id_kategori)
SELECT 'UMM-001', 'Router WiFi Lantai 1', 'TP-Link Omada EAP225', 'Baik', DATE '2025-06-15',
       (SELECT id_kamar FROM public.kamar WHERE nomor_kamar = 'UMUM' LIMIT 1), 1
ON CONFLICT (kode_aset) DO NOTHING;

INSERT INTO public.fasilitas_kamar (kode_aset, nama_fasilitas, merk_tipe, kondisi, tgl_pasang, id_kamar, id_kategori)
SELECT 'UMM-002', 'Mesin Air / Pompa Utama', 'Shimizu PS-230 BIT', 'Baik', DATE '2025-06-15',
       (SELECT id_kamar FROM public.kamar WHERE nomor_kamar = 'UMUM' LIMIT 1), 3
ON CONFLICT (kode_aset) DO NOTHING;
