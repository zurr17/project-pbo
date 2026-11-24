-- Migrasi tambahan untuk menyesuaikan schema PostgreSQL Sarpraskost (Kost Lodaya 9)
-- Menambahkan identitas gedung & keterangan pembangunan pada tabel `kamar`, serta kolom aturan bisnis pada `laporan_kerusakan`.

ALTER TABLE public.kamar
    ADD COLUMN IF NOT EXISTS gedung character varying(20) NOT NULL DEFAULT 'Gedung 1',
    ADD COLUMN IF NOT EXISTS keterangan text;

ALTER TABLE public.kamar
    DROP CONSTRAINT IF EXISTS kamar_nomor_kamar_key;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'kamar_gedung_nomor_key'
    ) THEN
        ALTER TABLE public.kamar
            ADD CONSTRAINT kamar_gedung_nomor_key UNIQUE (gedung, nomor_kamar);
    END IF;
END $$;

ALTER TABLE public.kamar
    DROP CONSTRAINT IF EXISTS kamar_status_hunian_check;

ALTER TABLE public.kamar
    ADD CONSTRAINT kamar_status_hunian_check
    CHECK ((status_hunian)::text = ANY ((ARRAY['Terisi'::character varying, 'Kosong'::character varying, 'Perbaikan'::character varying, 'Pembangunan'::character varying])::text[]));

ALTER TABLE public.laporan_kerusakan
    ADD COLUMN IF NOT EXISTS jumlah_terdampak integer NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS jadwal_pengerjaan timestamp without time zone,
    ADD COLUMN IF NOT EXISTS is_urgent boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS izin_masuk_kamar boolean NOT NULL DEFAULT false;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'laporan_kerusakan_jumlah_terdampak_check'
    ) THEN
        ALTER TABLE public.laporan_kerusakan
            ADD CONSTRAINT laporan_kerusakan_jumlah_terdampak_check
            CHECK (jumlah_terdampak >= 1);
    END IF;
END $$;

UPDATE public.laporan_kerusakan
SET is_urgent = true
WHERE tingkat_urgensi = 'Darurat' AND is_urgent = false;

UPDATE public.kamar
SET gedung = 'Umum',
    keterangan = COALESCE(keterangan, 'Area fasilitas bersama penghuni Kost Lodaya 9')
WHERE UPPER(nomor_kamar) = 'UMUM';

INSERT INTO public.kamar (gedung, nomor_kamar, lantai, tipe_kamar, status_hunian, keterangan)
SELECT 'Umum', 'UMUM', 1, 'Fasilitas Umum', 'Terisi', 'Area fasilitas bersama penghuni Kost Lodaya 9'
WHERE NOT EXISTS (
    SELECT 1 FROM public.kamar WHERE UPPER(nomor_kamar) = 'UMUM'
);

INSERT INTO public.fasilitas_kamar (kode_aset, nama_fasilitas, merk_tipe, kondisi, tgl_pasang, id_kamar, id_kategori)
SELECT 'UMM-001', 'Router WiFi Lantai 1', 'TP-Link Omada EAP225', 'Baik', DATE '2025-06-15',
       (SELECT id_kamar FROM public.kamar WHERE UPPER(nomor_kamar) = 'UMUM' LIMIT 1),
       (SELECT id_kategori FROM public.kategori_fasilitas WHERE nama_kategori = 'Elektronik' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM public.fasilitas_kamar WHERE kode_aset = 'UMM-001')
  AND EXISTS (SELECT 1 FROM public.kamar WHERE UPPER(nomor_kamar) = 'UMUM')
  AND EXISTS (SELECT 1 FROM public.kategori_fasilitas WHERE nama_kategori = 'Elektronik');

INSERT INTO public.fasilitas_kamar (kode_aset, nama_fasilitas, merk_tipe, kondisi, tgl_pasang, id_kamar, id_kategori)
SELECT 'UMM-002', 'Mesin Air / Pompa Utama', 'Shimizu PS-230 BIT', 'Baik', DATE '2025-06-15',
       (SELECT id_kamar FROM public.kamar WHERE UPPER(nomor_kamar) = 'UMUM' LIMIT 1),
       (SELECT id_kategori FROM public.kategori_fasilitas WHERE nama_kategori = 'Sanitasi & Air' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM public.fasilitas_kamar WHERE kode_aset = 'UMM-002')
  AND EXISTS (SELECT 1 FROM public.kamar WHERE UPPER(nomor_kamar) = 'UMUM')
  AND EXISTS (SELECT 1 FROM public.kategori_fasilitas WHERE nama_kategori = 'Sanitasi & Air');
