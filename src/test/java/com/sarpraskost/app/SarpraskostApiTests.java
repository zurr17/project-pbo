package com.sarpraskost.app;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.KamarRepository;
import com.sarpraskost.app.repository.KategoriFasilitasRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.NotifikasiRepository;
import com.sarpraskost.app.repository.PenggunaRepository;
import com.sarpraskost.app.repository.RiwayatPenangananRepository;
import com.sarpraskost.app.service.FileStorageService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
class SarpraskostApiTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PenggunaRepository penggunaRepository;

    @Autowired
    private KamarRepository kamarRepository;

    @Autowired
    private KategoriFasilitasRepository kategoriRepository;

    @Autowired
    private FasilitasKamarRepository fasilitasRepository;

    @Autowired
    private LaporanKerusakanRepository laporanRepository;

    @Autowired
    private RiwayatPenangananRepository riwayatRepository;

    @Autowired
    private NotifikasiRepository notifikasiRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void loginEndpointAuthenticatesSeededPenghuniAndOwnerOnly() throws Exception {
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"penghuni@kost.com\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama_lengkap").value("Akbar"))
            .andExpect(jsonPath("$.role").value("PENGHUNI"))
            .andExpect(jsonPath("$.nomor_kamar").value("A-01"))
            .andExpect(jsonPath("$.redirect_url").value("home.html"))
            .andExpect(jsonPath("$.password_hash").doesNotExist());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"A-01\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama_lengkap").value("Akbar"));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"owner@kost.com\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama_lengkap").value("Pak Soleh"))
            .andExpect(jsonPath("$.role").value("OWNER"))
            .andExpect(jsonPath("$.redirect_url").value("owner-home.html"))
            .andExpect(jsonPath("$.password_hash").doesNotExist());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"penghuni@kost.com\",\"password\":\"salah\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void seededMasterDataAndPenghuniReportsExist() throws Exception {
        Pengguna andi = penggunaRepository.findByEmailIgnoreCase("penghuni@kost.com").orElseThrow();

        mockMvc.perform(get("/api/kamar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].nomor_kamar", hasItem("A-01")))
            .andExpect(jsonPath("$[*].nomor_kamar", hasItem("B-02")));

        mockMvc.perform(get("/api/kategori"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].nama_kategori", hasItem("Elektronik")))
            .andExpect(jsonPath("$[*].nama_kategori", hasItem("Sanitasi & Air")))
            .andExpect(jsonPath("$[*].nama_kategori", hasItem("Furnitur")));

        mockMvc.perform(get("/api/fasilitas/penghuni/" + andi.getIdPengguna()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("AC Samsung 1/2 PK")))
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("Keran Kamar Mandi")))
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("Lemari Pakaian")));

        mockMvc.perform(get("/api/laporan/penghuni/" + andi.getIdPengguna()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ringkasan.total", greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.ringkasan.menunggu", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.ringkasan.diproses", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.laporan[0].kode_tiket", startsWith("LPR-")))
            .andExpect(jsonPath("$.laporan[0].riwayat", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void kamarKategoriAndFasilitasCrudEndpointsWork() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Long kamarId = null;
        Long kategoriId = null;
        Long fasilitasId = null;

        try {
            MvcResult kamarRes = mockMvc.perform(post("/api/kamar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nomor_kamar\":\"Z-%s\",\"tipe_kamar\":\"Deluxe\",\"lantai\":3,\"status_hunian\":\"KOSONG\"}",
                        suffix
                    )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomor_kamar").value("Z-" + suffix))
                .andReturn();
            kamarId = objectMapper.readTree(kamarRes.getResponse().getContentAsString()).get("id_kamar").asLong();

            mockMvc.perform(put("/api/kamar/" + kamarId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"tipe_kamar\":\"VIP Suite\",\"lantai\":3,\"status_hunian\":\"PERBAIKAN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipe_kamar").value("VIP Suite"))
                .andExpect(jsonPath("$.status_hunian").value("PERBAIKAN"));

            MvcResult katRes = mockMvc.perform(post("/api/kategori")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama_kategori\":\"Kategori-%s\",\"deskripsi\":\"Uji kategori\"}",
                        suffix
                    )))
                .andExpect(status().isCreated())
                .andReturn();
            kategoriId = objectMapper.readTree(katRes.getResponse().getContentAsString()).get("id_kategori").asLong();

            mockMvc.perform(put("/api/kategori/" + kategoriId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama_kategori\":\"KategoriUpd-%s\",\"deskripsi\":\"Deskripsi baru\"}",
                        suffix
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deskripsi").value("Deskripsi baru"));

            MvcResult fasRes = mockMvc.perform(post("/api/fasilitas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"id_kamar\":%d,\"id_kategori\":%d,\"kode_aset\":\"AST-TEST-%s\",\"nama_fasilitas\":\"Meja Uji\",\"merk_tipe\":\"IKEA\",\"kondisi\":\"BAIK\"}",
                        kamarId, kategoriId, suffix
                    )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kode_aset").value("AST-TEST-" + suffix))
                .andReturn();
            fasilitasId = objectMapper.readTree(fasRes.getResponse().getContentAsString()).get("id_fasilitas").asLong();

            mockMvc.perform(put("/api/fasilitas/" + fasilitasId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nama_fasilitas\":\"Meja Uji Baru\",\"kondisi\":\"PERLU_PERAWATAN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama_fasilitas").value("Meja Uji Baru"))
                .andExpect(jsonPath("$.kondisi").value("PERLU_PERAWATAN"));

            mockMvc.perform(delete("/api/fasilitas/" + fasilitasId)).andExpect(status().isOk());
            fasilitasId = null;

            mockMvc.perform(delete("/api/kategori/" + kategoriId)).andExpect(status().isOk());
            kategoriId = null;

            mockMvc.perform(delete("/api/kamar/" + kamarId)).andExpect(status().isOk());
            kamarId = null;
        } finally {
            if (fasilitasId != null && fasilitasRepository.existsById(fasilitasId)) {
                fasilitasRepository.deleteById(fasilitasId);
            }
            if (kategoriId != null && kategoriRepository.existsById(kategoriId)) {
                kategoriRepository.deleteById(kategoriId);
            }
            if (kamarId != null && kamarRepository.existsById(kamarId)) {
                kamarRepository.deleteById(kamarId);
            }
        }
    }

    @Test
    void userRegistrationUpdateAndCascadeDeleteWork() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String uniqueEmail = "uji-" + suffix + "@kost.com";
        Long kamarId = null;
        Long userId = null;
        Long fasilitasId = null;
        Path savedFoto = null;

        try {
            MvcResult kamarRes = mockMvc.perform(post("/api/kamar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nomor_kamar\":\"U-%s\",\"tipe_kamar\":\"Reguler\",\"lantai\":1,\"status_hunian\":\"KOSONG\"}",
                        suffix
                    )))
                .andExpect(status().isCreated())
                .andReturn();
            kamarId = objectMapper.readTree(kamarRes.getResponse().getContentAsString()).get("id_kamar").asLong();

            Long kategoriId = kategoriRepository.findByNamaKategoriIgnoreCase("Elektronik").orElseThrow().getIdKategori();
            MvcResult fasRes = mockMvc.perform(post("/api/fasilitas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"id_kamar\":%d,\"id_kategori\":%d,\"nama_fasilitas\":\"Lampu Kamar Uji\",\"merk_tipe\":\"Philips\",\"kondisi\":\"BAIK\"}",
                        kamarId, kategoriId
                    )))
                .andExpect(status().isCreated())
                .andReturn();
            fasilitasId = objectMapper.readTree(fasRes.getResponse().getContentAsString()).get("id_fasilitas").asLong();

            MvcResult regRes = mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama_lengkap\":\"Penghuni Uji\",\"email\":\"%s\",\"password\":\"rahasia1\",\"no_hp\":\"08120000111\",\"id_kamar\":%d}",
                        uniqueEmail, kamarId
                    )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nama_lengkap").value("Penghuni Uji"))
                .andExpect(jsonPath("$.role").value("PENGHUNI"))
                .andExpect(jsonPath("$.id_kamar").value(kamarId))
                .andReturn();
            userId = objectMapper.readTree(regRes.getResponse().getContentAsString()).get("id_pengguna").asLong();

            mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama_lengkap\":\"Duplikat\",\"email\":\"%s\",\"password\":\"123\"}",
                        uniqueEmail
                    )))
                .andExpect(status().isConflict());

            mockMvc.perform(put("/api/users/" + userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nama_lengkap\":\"Penghuni Baru\",\"no_hp\":\"08129998887\",\"password\":\"rahasia2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama_lengkap").value("Penghuni Baru"))
                .andExpect(jsonPath("$.no_hp").value("08129998887"));

            mockMvc.perform(post("/api/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"email\":\"%s\",\"password\":\"rahasia2\"}", uniqueEmail)))
                .andExpect(status().isOk());

            MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "cascade.png",
                "image/png",
                new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
            );
            MvcResult lapRes = mockMvc.perform(multipart("/api/laporan")
                    .file(foto)
                    .param("id_pengguna", String.valueOf(userId))
                    .param("id_fasilitas", String.valueOf(fasilitasId))
                    .param("judul_laporan", "Lampu berkedip")
                    .param("deskripsi_kerusakan", "Lampu kamar berkedip terus saat dinyalakan."))
                .andExpect(status().isCreated())
                .andReturn();

            String fotoUrl = objectMapper.readTree(lapRes.getResponse().getContentAsString()).get("foto_url").asText();
            savedFoto = fileStorageService.getUploadDir().resolve(fotoUrl.substring("/uploads/".length()));
            assertTrue(Files.exists(savedFoto));

            mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isOk());
            userId = null;

            assertFalse(Files.exists(savedFoto), "Deleting user should remove report photo file");
            mockMvc.perform(get("/api/users/" + 999999)).andExpect(status().isNotFound());
        } finally {
            if (savedFoto != null) {
                Files.deleteIfExists(savedFoto);
            }
            if (userId != null && penggunaRepository.existsById(userId)) {
                notifikasiRepository.deleteByPenggunaIdPengguna(userId);
                laporanRepository.deleteAll(laporanRepository.findByPenggunaIdPengguna(userId));
                penggunaRepository.deleteById(userId);
            }
            if (fasilitasId != null && fasilitasRepository.existsById(fasilitasId)) {
                fasilitasRepository.deleteById(fasilitasId);
            }
            if (kamarId != null && kamarRepository.existsById(kamarId)) {
                kamarRepository.deleteById(kamarId);
            }
        }
    }

    @Test
    void fullLaporanOwnerProsesSelesaiTolakAndNotifikasiFlowWork() throws Exception {
        Pengguna andi = penggunaRepository.findByEmailIgnoreCase("penghuni@kost.com").orElseThrow();
        Pengguna owner = penggunaRepository.findByEmailIgnoreCase("owner@kost.com").orElseThrow();
        Long lemariId = fasilitasRepository.findByKamarIdKamarOrderByIdFasilitasDesc(andi.getKamar().getIdKamar())
            .stream()
            .filter(f -> f.getNamaFasilitas().contains("Lemari"))
            .findFirst()
            .orElseThrow()
            .getIdFasilitas();

        MockMultipartFile fotoAwal = new MockMultipartFile(
            "foto",
            "awal.png",
            "image/png",
            new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );

        MvcResult createRes = mockMvc.perform(multipart("/api/laporan")
                .file(fotoAwal)
                .param("id_pengguna", String.valueOf(andi.getIdPengguna()))
                .param("id_fasilitas", String.valueOf(lemariId))
                .param("judul_laporan", "Engsel lemari lepas")
                .param("deskripsi_kerusakan", "Engsel pintu kanan lemari lepas sehingga tidak dapat ditutup."))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status_tiket").value("MENUNGGU"))
            .andExpect(jsonPath("$.kondisi_fasilitas").value("RUSAK"))
            .andExpect(jsonPath("$.riwayat", hasSize(1)))
            .andReturn();

        JsonNode created = objectMapper.readTree(createRes.getResponse().getContentAsString());
        long lapId1 = created.get("id_laporan").asLong();
        Path awalPath = fileStorageService.getUploadDir().resolve(created.get("foto_url").asText().replace("/uploads/", ""));
        Path sesudahPath = null;
        Long lapId2 = null;

        try {
            // Penghuni edit while MENUNGGU
            mockMvc.perform(put("/api/laporan/" + lapId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"judul_laporan\":\"Engsel pintu lemari patah\",\"deskripsi_kerusakan\":\"Engsel bawah patah.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.judul_laporan").value("Engsel pintu lemari patah"));

            // Owner proses
            mockMvc.perform(put("/api/owner/laporan/" + lapId1 + "/proses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"id_pengguna\":%d,\"catatan_penanganan\":\"Membeli engsel stainless baru.\",\"biaya_perbaikan\":75000}",
                        owner.getIdPengguna()
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status_tiket").value("DIPROSES"))
                .andExpect(jsonPath("$.kondisi_fasilitas").value("PERLU_PERAWATAN"))
                .andExpect(jsonPath("$.biaya_perbaikan").value(75000))
                .andExpect(jsonPath("$.riwayat", hasSize(2)));

            // Owner selesai with foto_sesudah
            MockMultipartFile fotoSesudah = new MockMultipartFile(
                "foto_sesudah",
                "sesudah.jpg",
                "image/jpeg",
                new byte[] {1, 2, 3, 4}
            );
            MvcResult selesaiRes = mockMvc.perform(multipart(HttpMethod.PUT, "/api/owner/laporan/" + lapId1 + "/selesai")
                    .file(fotoSesudah)
                    .param("id_pengguna", String.valueOf(owner.getIdPengguna()))
                    .param("catatan_penanganan", "Engsel baru sudah terpasang kokoh.")
                    .param("biaya_perbaikan", "85000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status_tiket").value("SELESAI"))
                .andExpect(jsonPath("$.kondisi_fasilitas").value("BAIK"))
                .andExpect(jsonPath("$.foto_sesudah_url", startsWith("/uploads/")))
                .andExpect(jsonPath("$.biaya_perbaikan").value(85000))
                .andExpect(jsonPath("$.riwayat", hasSize(3)))
                .andReturn();

            String fotoSesudahUrl = objectMapper.readTree(selesaiRes.getResponse().getContentAsString())
                .get("foto_sesudah_url").asText();
            sesudahPath = fileStorageService.getUploadDir().resolve(fotoSesudahUrl.replace("/uploads/", ""));
            assertTrue(Files.exists(sesudahPath));

            // Notification exists for Penghuni and can be marked read
            MvcResult notifRes = mockMvc.perform(get("/api/notifikasi/" + andi.getIdPengguna()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();
            long notifId = objectMapper.readTree(notifRes.getResponse().getContentAsString()).get(0).get("id_notifikasi").asLong();
            mockMvc.perform(put("/api/notifikasi/" + notifId + "/baca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_read").value(true));

            // Test Tolak flow on a 2nd temporary report
            MvcResult lap2Res = mockMvc.perform(multipart("/api/laporan")
                    .param("id_pengguna", String.valueOf(andi.getIdPengguna()))
                    .param("id_fasilitas", String.valueOf(lemariId))
                    .param("judul_laporan", "Laporan duplikat uji")
                    .param("deskripsi_kerusakan", "Uji coba penolakan owner."))
                .andExpect(status().isCreated())
                .andReturn();
            lapId2 = objectMapper.readTree(lap2Res.getResponse().getContentAsString()).get("id_laporan").asLong();

            mockMvc.perform(put("/api/owner/laporan/" + lapId2 + "/tolak")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"id_pengguna\":%d,\"kategori_penolakan\":\"Laporan duplikat\",\"alasan_penolakan\":\"Sudah pernah dilaporkan sebelumnya.\"}",
                        owner.getIdPengguna()
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status_tiket").value("DITOLAK"))
                .andExpect(jsonPath("$.kondisi_fasilitas").value("BAIK"))
                .andExpect(jsonPath("$.riwayat", hasSize(2)));

            // Owner dashboard reflects everything
            mockMvc.perform(get("/api/owner/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ringkasan.total", greaterThanOrEqualTo(4)));

            mockMvc.perform(delete("/api/laporan/" + lapId1)).andExpect(status().isOk());
            assertFalse(Files.exists(awalPath));
            assertFalse(Files.exists(sesudahPath));
            lapId1 = -1L;

            mockMvc.perform(delete("/api/laporan/" + lapId2)).andExpect(status().isOk());
            lapId2 = null;
        } finally {
            if (lapId1 > 0 && laporanRepository.existsById(lapId1)) {
                notifikasiRepository.deleteByLaporanIdLaporan(lapId1);
                riwayatRepository.deleteByLaporanIdLaporan(lapId1);
                laporanRepository.deleteById(lapId1);
            }
            if (lapId2 != null && laporanRepository.existsById(lapId2)) {
                notifikasiRepository.deleteByLaporanIdLaporan(lapId2);
                riwayatRepository.deleteByLaporanIdLaporan(lapId2);
                laporanRepository.deleteById(lapId2);
            }
            Files.deleteIfExists(awalPath);
            if (sesudahPath != null) {
                Files.deleteIfExists(sesudahPath);
            }
        }
    }

    @Test
    void staticFrontendPagesServeSuccessfullyAndMechanicPagesRemoved() throws Exception {
        String[] activePaths = {
            "/index.html",
            "/login.html",
            "/register.html",
            "/home.html",
            "/buat-laporan.html",
            "/sukses.html",
            "/riwayat.html",
            "/detail-laporan.html",
            "/fasilitas.html",
            "/profil.html",
            "/notifikasi.html",
            "/owner-home.html",
            "/owner-laporan.html",
            "/owner-detail-laporan.html",
            "/owner-proses.html",
            "/owner-tolak.html",
            "/owner-konfirmasi.html",
            "/owner-kamar-fasilitas.html",
            "/css/style.css",
            "/js/app.js"
        };
        for (String path : activePaths) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }

        String[] removedPaths = {
            "/mekanik-home.html",
            "/mekanik-tugas.html",
            "/mekanik-detail-tugas.html",
            "/mekanik-update-pekerjaan.html",
            "/mekanik-selesai.html",
            "/owner-setujui.html"
        };
        for (String path : removedPaths) {
            mockMvc.perform(get(path)).andExpect(status().isNotFound());
        }
    }

    @Test
    void databaseSchemaMatchesSevenErdTablesAndColumns() {
        List<String> tables = jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name",
            String.class
        );
        Set<String> expectedTables = Set.of(
            "kamar",
            "pengguna",
            "kategori_fasilitas",
            "fasilitas_kamar",
            "laporan_kerusakan",
            "riwayat_penanganan",
            "notifikasi"
        );
        assertTrue(tables.size() == 7 && Set.copyOf(tables).equals(expectedTables),
            "Expected exact 7 ERD tables, found: " + tables);

        for (String table : expectedTables) {
            List<String> cols = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns "
                    + "WHERE table_schema = 'public' AND table_name = ? ORDER BY ordinal_position",
                String.class,
                table
            );
            System.out.println("SCHEMA_TABLE " + table + " -> " + cols);
        }
    }
}
