package com.sarpraskost.app;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
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

import com.sarpraskost.app.entity.FasilitasKamar;
import com.sarpraskost.app.entity.Kamar;
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
                .content("{\"email\":\"raka@kost.id\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama_lengkap").value("Raka Pratama"))
            .andExpect(jsonPath("$.role").value("PENGHUNI"))
            .andExpect(jsonPath("$.nomor_kamar").value("A-101"))
            .andExpect(jsonPath("$.redirect_url").value("home.html"))
            .andExpect(jsonPath("$.password_hash").doesNotExist());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"A-101\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama_lengkap").value("Raka Pratama"));

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"budi.pengelola@kost.id\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama_lengkap").value("Budi Santoso"))
            .andExpect(jsonPath("$.role").value("OWNER"))
            .andExpect(jsonPath("$.redirect_url").value("owner-home.html"))
            .andExpect(jsonPath("$.password_hash").doesNotExist());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"raka@kost.id\",\"password\":\"salah\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void seededMasterDataPenghuniReportsAndActiveUmumExistWithoutReporterIdentity() throws Exception {
        Pengguna raka = penggunaRepository.findByEmailIgnoreCase("raka@kost.id").orElseThrow();

        mockMvc.perform(get("/api/kamar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].nomor_kamar", hasItem("A-101")))
            .andExpect(jsonPath("$[*].nomor_kamar", hasItem("B-202")))
            .andExpect(jsonPath("$[*].nomor_kamar", hasItem("UMUM")));

        mockMvc.perform(get("/api/kategori"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].nama_kategori", hasItem("Elektronik")))
            .andExpect(jsonPath("$[*].nama_kategori", hasItem("Sanitasi & Air")))
            .andExpect(jsonPath("$[*].nama_kategori", hasItem("Furnitur")));

        mockMvc.perform(get("/api/fasilitas/penghuni/" + raka.getIdPengguna()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("AC Split 1 PK")))
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("Lampu LED 18W")))
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("Router WiFi Lantai 1")))
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("Mesin Air / Pompa Utama")));

        mockMvc.perform(get("/api/laporan/penghuni/" + raka.getIdPengguna()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ringkasan.total", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.ringkasan.total_biaya_internal").value(nullValue()))
            .andExpect(jsonPath("$.laporan[0].kode_tiket", startsWith("TKT-")))
            .andExpect(jsonPath("$.laporan[0].biaya_perbaikan").value(nullValue()))
            .andExpect(jsonPath("$.laporan[0].riwayat", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.laporan[0].riwayat[0].biaya_perbaikan").value(nullValue()));

        MvcResult umumRes = mockMvc.perform(get("/api/laporan/fasilitas-umum/aktif"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode umumArr = objectMapper.readTree(umumRes.getResponse().getContentAsString());
        Integer tempUmumFasId = null;
        Integer tempUmumLapId = null;
        try {
            if (umumArr.isEmpty()) {
                Kamar umum = kamarRepository.findByNomorKamarIgnoreCase("UMUM").orElseThrow();
                Integer katId = kategoriRepository.findByNamaKategoriIgnoreCase("Elektronik").orElseThrow().getIdKategori();
                String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                MvcResult fasRes = mockMvc.perform(post("/api/fasilitas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                            "{\"id_kamar\":%d,\"id_kategori\":%d,\"nama_fasilitas\":\"WiFi Uji %s\"}",
                            umum.getIdKamar(), katId, suffix
                        )))
                    .andExpect(status().isCreated())
                    .andReturn();
                tempUmumFasId = objectMapper.readTree(fasRes.getResponse().getContentAsString()).get("id_fasilitas").asInt();

                MvcResult lapRes = mockMvc.perform(multipart("/api/laporan")
                        .param("id_pengguna", String.valueOf(raka.getIdPengguna()))
                        .param("id_fasilitas", String.valueOf(tempUmumFasId))
                        .param("judul_laporan", "WiFi uji gangguan")
                        .param("deskripsi_kerusakan", "Koneksi terputus untuk verifikasi endpoint aktif."))
                    .andExpect(status().isCreated())
                    .andReturn();
                tempUmumLapId = objectMapper.readTree(lapRes.getResponse().getContentAsString()).get("id_laporan").asInt();

                umumRes = mockMvc.perform(get("/api/laporan/fasilitas-umum/aktif"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andReturn();
                umumArr = objectMapper.readTree(umumRes.getResponse().getContentAsString());
            }

            JsonNode firstUmum = umumArr.get(0);
            assertFalse(firstUmum.has("nama_pelapor"), "Public shared alert must NOT expose reporter name");
            assertFalse(firstUmum.has("id_pengguna"), "Public shared alert must NOT expose reporter id");
            assertFalse(firstUmum.has("no_hp_pelapor"), "Public shared alert must NOT expose reporter phone");

            int sharedId = firstUmum.get("id_laporan").asInt();
            mockMvc.perform(get("/api/laporan/" + sharedId + "?viewer=PENGHUNI&viewerId=-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.biaya_perbaikan").value(nullValue()))
                .andExpect(jsonPath("$.nama_pelapor").value("Penghuni Kost"))
                .andExpect(jsonPath("$.id_pengguna").value(nullValue()));
        } finally {
            safeDeleteLaporan(tempUmumLapId);
            if (tempUmumFasId != null && fasilitasRepository.existsById(tempUmumFasId)) {
                fasilitasRepository.deleteById(tempUmumFasId);
            }
        }
    }

    @Test
    void kamarKategoriAndFasilitasCrudEndpointsWork() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Integer kamarId = null;
        Integer kategoriId = null;
        Integer fasilitasId = null;

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
            kamarId = objectMapper.readTree(kamarRes.getResponse().getContentAsString()).get("id_kamar").asInt();

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
            kategoriId = objectMapper.readTree(katRes.getResponse().getContentAsString()).get("id_kategori").asInt();

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
            fasilitasId = objectMapper.readTree(fasRes.getResponse().getContentAsString()).get("id_fasilitas").asInt();

            mockMvc.perform(put("/api/fasilitas/" + fasilitasId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nama_fasilitas\":\"Meja Uji Baru\",\"kondisi\":\"RUSAK_RINGAN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama_fasilitas").value("Meja Uji Baru"))
                .andExpect(jsonPath("$.kondisi").value("RUSAK_RINGAN"));

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
    void userRegistrationValidationsAndCascadeDeleteWork() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String uniqueEmail = "uji-" + suffix + "@kost.com";
        String uniquePhone = "0819-" + suffix;
        Integer kamarId = null;
        Integer userId = null;
        Integer fasilitasId = null;
        Path savedFoto = null;

        try {
            Kamar umumKamar = kamarRepository.findByNomorKamarIgnoreCase("UMUM").orElseThrow();
            mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama_lengkap\":\"Invalid Umum\",\"email\":\"invalid-%s@kost.com\",\"password\":\"123\",\"no_hp\":\"0819000%s\",\"id_kamar\":%d}",
                        suffix, suffix.substring(0, 4), umumKamar.getIdKamar()
                    )))
                .andExpect(status().isBadRequest());

            MvcResult kamarRes = mockMvc.perform(post("/api/kamar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nomor_kamar\":\"U-%s\",\"tipe_kamar\":\"Reguler\",\"lantai\":1,\"status_hunian\":\"KOSONG\"}",
                        suffix
                    )))
                .andExpect(status().isCreated())
                .andReturn();
            kamarId = objectMapper.readTree(kamarRes.getResponse().getContentAsString()).get("id_kamar").asInt();

            Integer kategoriId = kategoriRepository.findByNamaKategoriIgnoreCase("Elektronik").orElseThrow().getIdKategori();
            MvcResult fasRes = mockMvc.perform(post("/api/fasilitas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"id_kamar\":%d,\"id_kategori\":%d,\"nama_fasilitas\":\"Lampu Kamar Uji\",\"merk_tipe\":\"Philips\",\"kondisi\":\"BAIK\"}",
                        kamarId, kategoriId
                    )))
                .andExpect(status().isCreated())
                .andReturn();
            fasilitasId = objectMapper.readTree(fasRes.getResponse().getContentAsString()).get("id_fasilitas").asInt();

            MvcResult regRes = mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama_lengkap\":\"Penghuni Uji\",\"email\":\"%s\",\"password\":\"rahasia1\",\"no_hp\":\"%s\",\"id_kamar\":%d}",
                        uniqueEmail, uniquePhone, kamarId
                    )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nama_lengkap").value("Penghuni Uji"))
                .andExpect(jsonPath("$.role").value("PENGHUNI"))
                .andExpect(jsonPath("$.id_kamar").value(kamarId))
                .andReturn();
            userId = objectMapper.readTree(regRes.getResponse().getContentAsString()).get("id_pengguna").asInt();

            mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama_lengkap\":\"Duplikat\",\"email\":\"%s\",\"no_hp\":\"08999999999\",\"password\":\"123\"}",
                        uniqueEmail
                    )))
                .andExpect(status().isConflict());

            mockMvc.perform(put("/api/users/" + userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama_lengkap\":\"Penghuni Baru\",\"no_hp\":\"0818-%s\",\"password\":\"rahasia2\"}",
                        suffix
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama_lengkap").value("Penghuni Baru"));

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
    void fasilitasUmumDuplicatePreventionAndTambahTerdampakWork() throws Exception {
        Pengguna raka = penggunaRepository.findByEmailIgnoreCase("raka@kost.id").orElseThrow();
        Pengguna nadia = penggunaRepository.findByEmailIgnoreCase("nadia@kost.id").orElseThrow();
        Kamar umum = kamarRepository.findByNomorKamarIgnoreCase("UMUM").orElseThrow();
        Integer kategoriId = kategoriRepository.findByNamaKategoriIgnoreCase("Sanitasi & Air").orElseThrow().getIdKategori();
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Integer tempFasilitasId = null;
        Integer tempLaporanId = null;

        try {
            MvcResult fasRes = mockMvc.perform(post("/api/fasilitas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"id_kamar\":%d,\"id_kategori\":%d,\"nama_fasilitas\":\"Dispenser Koridor %s\",\"merk_tipe\":\"Miyako\",\"kondisi\":\"BAIK\"}",
                        umum.getIdKamar(), kategoriId, suffix
                    )))
                .andExpect(status().isCreated())
                .andReturn();
            tempFasilitasId = objectMapper.readTree(fasRes.getResponse().getContentAsString()).get("id_fasilitas").asInt();

            MvcResult createRes = mockMvc.perform(multipart("/api/laporan")
                    .param("id_pengguna", String.valueOf(raka.getIdPengguna()))
                    .param("id_fasilitas", String.valueOf(tempFasilitasId))
                    .param("judul_laporan", "Dispenser air panas mati")
                    .param("deskripsi_kerusakan", "Pemanas dispenser bersama di lantai 1 tidak menyala."))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.is_umum").value(true))
                .andExpect(jsonPath("$.jumlah_terdampak").value(1))
                .andReturn();
            tempLaporanId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id_laporan").asInt();

            // Duplicate shared ticket must fail with 409 Conflict
            mockMvc.perform(multipart("/api/laporan")
                    .param("id_pengguna", String.valueOf(nadia.getIdPengguna()))
                    .param("id_fasilitas", String.valueOf(tempFasilitasId))
                    .param("judul_laporan", "Dispenser rusak juga")
                    .param("deskripsi_kerusakan", "Air panas dispenser mati sejak pagi."))
                .andExpect(status().isConflict());

            // +1 Terdampak endpoint increments without returning reporter identity
            MvcResult plusRes = mockMvc.perform(post("/api/laporan/" + tempLaporanId + "/terdampak"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jumlah_terdampak").value(2))
                .andReturn();
            JsonNode plusJson = objectMapper.readTree(plusRes.getResponse().getContentAsString());
            assertFalse(plusJson.has("nama_pelapor"));
            assertFalse(plusJson.has("id_pengguna"));

            mockMvc.perform(delete("/api/laporan/" + tempLaporanId)).andExpect(status().isOk());
            tempLaporanId = null;

            mockMvc.perform(delete("/api/fasilitas/" + tempFasilitasId)).andExpect(status().isOk());
            tempFasilitasId = null;
        } finally {
            safeDeleteLaporan(tempLaporanId);
            if (tempFasilitasId != null && fasilitasRepository.existsById(tempFasilitasId)) {
                fasilitasRepository.deleteById(tempFasilitasId);
            }
        }
    }

    @Test
    void urgentIzinValidationScheduleProsesSelesaiAndTolakWork() throws Exception {
        Pengguna raka = penggunaRepository.findByEmailIgnoreCase("raka@kost.id").orElseThrow();
        Pengguna owner = penggunaRepository.findByEmailIgnoreCase("budi.pengelola@kost.id").orElseThrow();
        FasilitasKamar acA101 = fasilitasRepository.findByKamarIdKamarOrderByIdFasilitasDesc(raka.getKamar().getIdKamar())
            .stream()
            .filter(f -> f.getNamaFasilitas().contains("AC"))
            .findFirst()
            .orElseThrow();

        // Urgent private room report WITHOUT izin_masuk_kamar must be rejected (400)
        mockMvc.perform(multipart("/api/laporan")
                .param("id_pengguna", String.valueOf(raka.getIdPengguna()))
                .param("id_fasilitas", String.valueOf(acA101.getIdFasilitas()))
                .param("judul_laporan", "AC bocor parah")
                .param("deskripsi_kerusakan", "Air AC menggenangi kamar.")
                .param("tingkat_urgensi", "DARURAT")
                .param("is_urgent", "true")
                .param("izin_masuk_kamar", "false"))
            .andExpect(status().isBadRequest());

        MockMultipartFile fotoAwal = new MockMultipartFile(
            "foto",
            "awal.png",
            "image/png",
            new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );

        MvcResult createRes = mockMvc.perform(multipart("/api/laporan")
                .file(fotoAwal)
                .param("id_pengguna", String.valueOf(raka.getIdPengguna()))
                .param("id_fasilitas", String.valueOf(acA101.getIdFasilitas()))
                .param("judul_laporan", "AC korsleting & bocor")
                .param("deskripsi_kerusakan", "Air keluar dari unit indoor AC menetes ke colokan listrik.")
                .param("tingkat_urgensi", "DARURAT")
                .param("is_urgent", "true")
                .param("izin_masuk_kamar", "true"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status_tiket").value("MENUNGGU"))
            .andExpect(jsonPath("$.tingkat_urgensi").value("DARURAT"))
            .andExpect(jsonPath("$.is_urgent").value(true))
            .andExpect(jsonPath("$.izin_masuk_kamar").value(true))
            .andExpect(jsonPath("$.kondisi_fasilitas").value("RUSAK_BERAT"))
            .andExpect(jsonPath("$.riwayat", hasSize(1)))
            .andReturn();

        JsonNode created = objectMapper.readTree(createRes.getResponse().getContentAsString());
        Integer lapId1 = created.get("id_laporan").asInt();
        Path awalPath = fileStorageService.getUploadDir().resolve(created.get("foto_url").asText().replace("/uploads/", ""));
        Path sesudahPath = null;
        Integer lapId2 = null;

        try {
            // Penghuni edit while MENUNGGU
            mockMvc.perform(put("/api/laporan/" + lapId1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"judul_laporan\":\"AC indoor bocor deras\",\"deskripsi_kerusakan\":\"Drainase AC mampet total.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.judul_laporan").value("AC indoor bocor deras"));

            // Owner /proses WITHOUT jadwal_pengerjaan must fail (400)
            mockMvc.perform(put("/api/owner/laporan/" + lapId1 + "/proses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"id_pengguna\":%d,\"catatan_penanganan\":\"Pembersihan pipa pembuangan.\",\"biaya_perbaikan\":75000}",
                        owner.getIdPengguna()
                    )))
                .andExpect(status().isBadRequest());

            // Owner /proses WITH jadwal_pengerjaan succeeds
            mockMvc.perform(put("/api/owner/laporan/" + lapId1 + "/proses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"id_pengguna\":%d,\"catatan_penanganan\":\"Teknisi AC segera datang.\",\"biaya_perbaikan\":75000,\"jadwal_pengerjaan\":\"2026-11-25T10:30:00\"}",
                        owner.getIdPengguna()
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status_tiket").value("DIPROSES"))
                .andExpect(jsonPath("$.kondisi_fasilitas").value("DALAM_PERBAIKAN"))
                .andExpect(jsonPath("$.jadwal_pengerjaan").value("2026-11-25T10:30:00"))
                .andExpect(jsonPath("$.biaya_perbaikan").value(75000))
                .andExpect(jsonPath("$.riwayat", hasSize(2)));

            // Owner /selesai with foto_sesudah
            MockMultipartFile fotoSesudah = new MockMultipartFile(
                "foto_sesudah",
                "sesudah.jpg",
                "image/jpeg",
                new byte[] {1, 2, 3, 4}
            );
            MvcResult selesaiRes = mockMvc.perform(multipart(HttpMethod.PUT, "/api/owner/laporan/" + lapId1 + "/selesai")
                    .file(fotoSesudah)
                    .param("id_pengguna", String.valueOf(owner.getIdPengguna()))
                    .param("catatan_penanganan", "Saluran drainase AC bersih dan normal kembali.")
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

            // Notification exists for Owner (with urgent + izin_masuk_kamar) and for Penghuni (WITHOUT biaya)
            MvcResult ownerNotifRes = mockMvc.perform(get("/api/notifikasi/" + owner.getIdPengguna()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();
            JsonNode latestOwnerNotif = objectMapper.readTree(ownerNotifRes.getResponse().getContentAsString()).get(0);
            assertTrue(latestOwnerNotif.get("is_urgent").asBoolean());
            assertTrue(latestOwnerNotif.get("izin_masuk_kamar").asBoolean());
            assertTrue(latestOwnerNotif.get("has_foto_bukti").asBoolean());

            MvcResult notifRes = mockMvc.perform(get("/api/notifikasi/" + raka.getIdPengguna()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();
            JsonNode notifArr = objectMapper.readTree(notifRes.getResponse().getContentAsString());
            for (JsonNode n : notifArr) {
                if (n.get("id_laporan").asInt() == lapId1) {
                    String pesan = n.get("pesan_notif").asText().toLowerCase();
                    assertFalse(pesan.contains("biaya"), "Penghuni notification must NOT expose repair cost");
                    assertFalse(pesan.contains("rp "), "Penghuni notification must NOT expose Rupiah cost");
                }
            }
            int notifId = notifArr.get(0).get("id_notifikasi").asInt();
            mockMvc.perform(put("/api/notifikasi/" + notifId + "/baca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_read").value(true));

            // Test Tolak flow on a 2nd temporary report
            MvcResult lap2Res = mockMvc.perform(multipart("/api/laporan")
                    .param("id_pengguna", String.valueOf(raka.getIdPengguna()))
                    .param("id_fasilitas", String.valueOf(acA101.getIdFasilitas()))
                    .param("judul_laporan", "Laporan duplikat uji")
                    .param("deskripsi_kerusakan", "Uji coba penolakan owner."))
                .andExpect(status().isCreated())
                .andReturn();
            lapId2 = objectMapper.readTree(lap2Res.getResponse().getContentAsString()).get("id_laporan").asInt();

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

            mockMvc.perform(get("/api/owner/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ringkasan.total", greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$.ringkasan.total_biaya_internal").isNumber());

            mockMvc.perform(delete("/api/laporan/" + lapId1)).andExpect(status().isOk());
            assertFalse(Files.exists(awalPath));
            assertFalse(Files.exists(sesudahPath));
            lapId1 = null;

            mockMvc.perform(delete("/api/laporan/" + lapId2)).andExpect(status().isOk());
            lapId2 = null;
        } finally {
            safeDeleteLaporan(lapId1);
            safeDeleteLaporan(lapId2);
            Files.deleteIfExists(awalPath);
            if (sesudahPath != null) {
                Files.deleteIfExists(sesudahPath);
            }
        }
    }

    private void safeDeleteLaporan(Integer id) {
        if (id != null && laporanRepository.existsById(id)) {
            try {
                mockMvc.perform(delete("/api/laporan/" + id));
            } catch (Exception ignored) {
                // Keep original test failure unmasked
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
    void databaseSchemaMatchesSevenErdTablesAndNewColumns() {
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

        List<String> laporanCols = jdbcTemplate.queryForList(
            "SELECT column_name FROM information_schema.columns "
                + "WHERE table_schema = 'public' AND table_name = 'laporan_kerusakan' ORDER BY ordinal_position",
            String.class
        );
        assertTrue(laporanCols.containsAll(List.of(
            "tingkat_urgensi",
            "jumlah_terdampak",
            "jadwal_pengerjaan",
            "is_urgent",
            "izin_masuk_kamar"
        )), "Expected new columns in laporan_kerusakan, found: " + laporanCols);
    }
}
