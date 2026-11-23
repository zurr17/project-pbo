-- Migrasi tambahan untuk menyesuaikan tabel `laporan_kerusakan` dari dump `db_sapraskost`
-- dengan aturan bisnis terbaru (Fasilitas Umum +1 Terdampak, Penjadwalan Pasti, Akses Darurat).

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

-- Update data existing yang tingkat_urgensi = 'Darurat' agar flag is_urgent konsisten
UPDATE public.laporan_kerusakan
SET is_urgent = true
WHERE tingkat_urgensi = 'Darurat' AND is_urgent = false;

-- Pastikan kamar khusus Fasilitas Umum dan beberapa aset bersama tersedia bila belum ada
INSERT INTO public.kamar (nomor_kamar, lantai, tipe_kamar, status_hunian)
SELECT 'UMUM', 1, 'Fasilitas Umum', 'Terisi'
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
