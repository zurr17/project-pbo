package com.sarpraskost.app;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
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

import com.sarpraskost.app.entity.User;
import com.sarpraskost.app.repository.FasilitasKamarRepository;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.UserRepository;
import com.sarpraskost.app.service.FileStorageService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
    private UserRepository userRepository;

    @Autowired
    private LaporanKerusakanRepository laporanRepository;

    @Autowired
    private FasilitasKamarRepository fasilitasRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void loginEndpointAuthenticatesSeededPenghuniAndRejectsInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"penghuni@kost.com\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama").value("Mori"))
            .andExpect(jsonPath("$.role").value("PENGHUNI"))
            .andExpect(jsonPath("$.nomor_kamar").value("A-12"))
            .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"penghuni@kost.com\",\"password\":\"salah\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void penghuniReportsSummaryFilterSearchAndDetailWork() throws Exception {
        User mori = userRepository.findByEmailIgnoreCase("penghuni@kost.com").orElseThrow();
        long expectedTotal = laporanRepository.countByPenghuniId(mori.getId());
        assertTrue(expectedTotal >= 3, "Seeded demo user should have at least 3 reports");

        MvcResult listResult = mockMvc.perform(get("/api/laporan/penghuni/" + mori.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ringkasan.total").value(expectedTotal))
            .andExpect(jsonPath("$.ringkasan.menunggu", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.ringkasan.diproses", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.ringkasan.selesai", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.laporan", hasSize((int) expectedTotal)))
            .andReturn();

        JsonNode root = objectMapper.readTree(listResult.getResponse().getContentAsString());
        long firstId = root.get("laporan").get(0).get("id").asLong();

        mockMvc.perform(get("/api/laporan/penghuni/" + mori.getId())
                .param("status", "DIPROSES")
                .param("q", "keran"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.laporan", hasSize(1)))
            .andExpect(jsonPath("$.laporan[0].status").value("DIPROSES"));

        mockMvc.perform(get("/api/laporan/" + firstId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(firstId))
            .andExpect(jsonPath("$.kode_laporan", startsWith("LPR-")));
    }

    @Test
    void seededFasilitasExistForDemoPenghuni() throws Exception {
        User mori = userRepository.findByEmailIgnoreCase("penghuni@kost.com").orElseThrow();

        mockMvc.perform(get("/api/fasilitas/penghuni/" + mori.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("AC LG 1/2 PK")))
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("Kasur Springbed")))
            .andExpect(jsonPath("$[*].nama_fasilitas", hasItem("Keran Air Kamar Mandi")));
    }

    @Test
    void userCrudLifecycleAndCascadeCleanupWork() throws Exception {
        String uniqueEmail = "uji-" + UUID.randomUUID() + "@kost.com";
        Long userId = null;
        Path savedFile = null;

        try {
            MvcResult regResult = mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama\":\"Penghuni Uji\",\"email\":\"%s\",\"password\":\"rahasia1\",\"nomor_kamar\":\"B-08\"}",
                        uniqueEmail
                    )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nama").value("Penghuni Uji"))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("PENGHUNI"))
                .andExpect(jsonPath("$.nomor_kamar").value("B-08"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

            userId = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("id").asLong();

            // Duplicate email returns 409
            mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama\":\"Duplikat\",\"email\":\"%s\",\"password\":\"123\",\"nomor_kamar\":\"B-09\"}",
                        uniqueEmail
                    )))
                .andExpect(status().isConflict());

            // Read user profile
            mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.nama").value("Penghuni Uji"))
                .andExpect(jsonPath("$.nomor_kamar").value("B-08"));

            // Update profile without changing password
            mockMvc.perform(put("/api/users/" + userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nama\":\"Penghuni Baru\",\"nomor_kamar\":\"C-01\",\"password\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama").value("Penghuni Baru"))
                .andExpect(jsonPath("$.nomor_kamar").value("C-01"));

            // Old password still works
            mockMvc.perform(post("/api/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"email\":\"%s\",\"password\":\"rahasia1\"}", uniqueEmail)))
                .andExpect(status().isOk());

            // Update password and verify login with new password
            mockMvc.perform(put("/api/users/" + userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nama\":\"Penghuni Final\",\"nomor_kamar\":\"C-02\",\"password\":\"rahasia2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama").value("Penghuni Final"))
                .andExpect(jsonPath("$.nomor_kamar").value("C-02"));

            mockMvc.perform(post("/api/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"email\":\"%s\",\"password\":\"rahasia2\"}", uniqueEmail)))
                .andExpect(status().isOk());

            // Create child facility & child report with uploaded photo to verify cascade delete
            mockMvc.perform(post("/api/fasilitas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"penghuni_id\":%d,\"nama_fasilitas\":\"Meja Belajar\",\"merk_tipe\":\"Olympic\",\"kondisi\":\"BAIK\",\"keterangan\":\"Bersih\"}",
                        userId
                    )))
                .andExpect(status().isCreated());

            MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "cascade.png",
                "image/png",
                new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
            );
            MvcResult lapResult = mockMvc.perform(multipart("/api/laporan")
                    .file(foto)
                    .param("penghuni_id", String.valueOf(userId))
                    .param("kategori_fasilitas", "Listrik & Pencahayaan")
                    .param("lokasi_kamar", "Kamar C-02")
                    .param("deskripsi", "Stop kontak longgar"))
                .andExpect(status().isCreated())
                .andReturn();

            String fotoUrl = objectMapper.readTree(lapResult.getResponse().getContentAsString()).get("foto_url").asText();
            savedFile = fileStorageService.getUploadDir().resolve(fotoUrl.substring("/uploads/".length()));
            assertTrue(Files.exists(savedFile));

            // Delete account cascades facilities, reports, photo file, and user
            mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

            assertFalse(Files.exists(savedFile), "Cascade user delete should remove report photo file");
            mockMvc.perform(get("/api/users/" + userId)).andExpect(status().isNotFound());
            userId = null;
        } finally {
            if (savedFile != null) {
                Files.deleteIfExists(savedFile);
            }
            if (userId != null) {
                fasilitasRepository.deleteByPenghuniId(userId);
                laporanRepository.deleteByPenghuniId(userId);
                userRepository.deleteById(userId);
            }
        }
    }

    @Test
    void fasilitasCrudEndpointsWorkAndCleanUp() throws Exception {
        String uniqueEmail = "fasilitas-" + UUID.randomUUID() + "@kost.com";
        Long userId = null;

        try {
            MvcResult regResult = mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"nama\":\"Uji Fasilitas\",\"email\":\"%s\",\"password\":\"123\",\"nomor_kamar\":\"D-01\"}",
                        uniqueEmail
                    )))
                .andExpect(status().isCreated())
                .andReturn();
            userId = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("id").asLong();

            MvcResult createRes = mockMvc.perform(post("/api/fasilitas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"penghuni_id\":%d,\"nama_fasilitas\":\"Lemari Kayu\",\"merk_tipe\":\"Informa\",\"kondisi\":\"BAIK\",\"keterangan\":\"2 pintu\"}",
                        userId
                    )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.penghuni_id").value(userId))
                .andExpect(jsonPath("$.nama_fasilitas").value("Lemari Kayu"))
                .andExpect(jsonPath("$.kondisi").value("BAIK"))
                .andReturn();

            long fasId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asLong();

            mockMvc.perform(get("/api/fasilitas/penghuni/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(fasId));

            mockMvc.perform(put("/api/fasilitas/" + fasId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nama_fasilitas\":\"Lemari Jati\",\"merk_tipe\":\"Informa Plus\",\"kondisi\":\"PERLU_PERAWATAN\",\"keterangan\":\"Engsel perlu pelumas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama_fasilitas").value("Lemari Jati"))
                .andExpect(jsonPath("$.merk_tipe").value("Informa Plus"))
                .andExpect(jsonPath("$.kondisi").value("PERLU_PERAWATAN"))
                .andExpect(jsonPath("$.keterangan").value("Engsel perlu pelumas"));

            mockMvc.perform(delete("/api/fasilitas/" + fasId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

            mockMvc.perform(get("/api/fasilitas/penghuni/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(jsonPath("$[*].id", not(hasItem((int) fasId))));
        } finally {
            if (userId != null) {
                mockMvc.perform(delete("/api/users/" + userId));
            }
        }
    }

    @Test
    void laporanCreateUpdateAndDeleteWithPhotoCleanupWork() throws Exception {
        User mori = userRepository.findByEmailIgnoreCase("penghuni@kost.com").orElseThrow();
        MockMultipartFile foto = new MockMultipartFile(
            "foto",
            "bukti.png",
            "image/png",
            new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );

        MvcResult createResult = mockMvc.perform(multipart("/api/laporan")
                .file(foto)
                .param("penghuni_id", String.valueOf(mori.getId()))
                .param("kategori_fasilitas", "AC & Ventilasi")
                .param("lokasi_kamar", "Kamar A-12 · Area kamar")
                .param("deskripsi", "AC tidak dingin sejak semalam."))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.kode_laporan", startsWith("LPR-")))
            .andExpect(jsonPath("$.status").value("MENUNGGU"))
            .andExpect(jsonPath("$.foto_url", startsWith("/uploads/")))
            .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long createdId = created.get("id").asLong();
        String fotoUrl = created.get("foto_url").asText();
        String filename = fotoUrl.substring("/uploads/".length());
        Path savedFile = fileStorageService.getUploadDir().resolve(filename);
        boolean deletedViaApi = false;

        try {
            assertTrue(Files.exists(savedFile), "Uploaded file should exist in uploads/");
            mockMvc.perform(get(fotoUrl)).andExpect(status().isOk());

            mockMvc.perform(put("/api/laporan/" + createdId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"kategori_fasilitas\":\"Listrik & Pencahayaan\",\"lokasi_kamar\":\"Kamar A-12 · Teras\",\"deskripsi\":\"Lampu kamar berkedip dan saklar panas.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.kategori_fasilitas").value("Listrik & Pencahayaan"))
                .andExpect(jsonPath("$.lokasi_kamar").value("Kamar A-12 · Teras"))
                .andExpect(jsonPath("$.deskripsi").value("Lampu kamar berkedip dan saklar panas."));

            mockMvc.perform(delete("/api/laporan/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
            deletedViaApi = true;

            assertFalse(Files.exists(savedFile), "Deleting laporan should delete its uploaded photo");
            mockMvc.perform(get("/api/laporan/" + createdId)).andExpect(status().isNotFound());
        } finally {
            if (!deletedViaApi && laporanRepository.existsById(createdId)) {
                laporanRepository.deleteById(createdId);
            }
            Files.deleteIfExists(savedFile);
        }
    }

    @Test
    void ownerAndMekanikEndpointsAndWorkflowWork() throws Exception {
        // 1. Seeded Owner login works
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"owner@kost.com\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama").value("Bu Ratna"))
            .andExpect(jsonPath("$.role").value("OWNER"))
            .andExpect(jsonPath("$.password").doesNotExist());

        // 2. Seeded Mekanik accounts exist and GET /api/users/mekanik returns them
        MvcResult mekanikResult = mockMvc.perform(get("/api/users/mekanik"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$[*].nama", hasItem("Pak Budi")))
            .andExpect(jsonPath("$[*].nama", hasItem("Mas Dedi")))
            .andExpect(jsonPath("$[*].role", hasItem("MEKANIK")))
            .andReturn();

        JsonNode mekanikArray = objectMapper.readTree(mekanikResult.getResponse().getContentAsString());
        long mekanikId = mekanikArray.get(0).get("id").asLong();
        String mekanikNama = mekanikArray.get(0).get("nama").asText();

        // 3. Owner dashboard & filter endpoints work
        long totalReports = laporanRepository.count();
        mockMvc.perform(get("/api/owner/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ringkasan.total").value(totalReports))
            .andExpect(jsonPath("$.ringkasan.menunggu", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.ringkasan.diproses", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.ringkasan.selesai", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.laporan", hasSize((int) totalReports)))
            .andExpect(jsonPath("$.laporan[0].nomor_kamar_penghuni").exists());

        mockMvc.perform(get("/api/owner/laporan")
                .param("status", "DIPROSES")
                .param("q", "Mori"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.laporan", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.laporan[0].status").value("DIPROSES"))
            .andExpect(jsonPath("$.laporan[0].nama_penghuni").value("Mori"));

        // 4. Create temporary report to test approve & reject without dirtying seeded rows
        User mori = userRepository.findByEmailIgnoreCase("penghuni@kost.com").orElseThrow();
        MvcResult createResult = mockMvc.perform(multipart("/api/laporan")
                .param("penghuni_id", String.valueOf(mori.getId()))
                .param("kategori_fasilitas", "Air & Sanitasi")
                .param("lokasi_kamar", "Kamar A-12 · Wastafel")
                .param("deskripsi", "Pipa wastafel uji coba owner rembes."))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long tempId = createJson.get("id").asLong();
        String tempKode = createJson.get("kode_laporan").asText();

        try {
            // Invalid mekanik role rejected (penghuni is not mekanik)
            mockMvc.perform(put("/api/owner/laporan/" + tempId + "/setujui")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"mekanik_id\":%d,\"prioritas\":\"Tinggi\"}", mori.getId())))
                .andExpect(status().isBadRequest());

            // Blank priority defaults to "Sedang"
            mockMvc.perform(put("/api/owner/laporan/" + tempId + "/setujui")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"mekanik_id\":%d,\"prioritas\":\"  \"}", mekanikId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DIPROSES"))
                .andExpect(jsonPath("$.prioritas").value("Sedang"));

            // Valid approve & assign mechanic with Darurat priority
            mockMvc.perform(put("/api/owner/laporan/" + tempId + "/setujui")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"mekanik_id\":%d,\"prioritas\":\"Darurat\",\"catatan_owner\":\"Bawa seal pipa cadangan\"}",
                        mekanikId
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DIPROSES"))
                .andExpect(jsonPath("$.mekanik_id").value(mekanikId))
                .andExpect(jsonPath("$.nama_mekanik").value(mekanikNama))
                .andExpect(jsonPath("$.prioritas").value("Darurat"))
                .andExpect(jsonPath("$.catatan_owner").value("Bawa seal pipa cadangan"))
                .andExpect(jsonPath("$.tanggal_diproses").isNotEmpty())
                .andExpect(jsonPath("$.catatan_penanganan", org.hamcrest.Matchers.containsString(mekanikNama)))
                .andExpect(jsonPath("$.catatan_penanganan", org.hamcrest.Matchers.containsString("Bawa seal pipa cadangan")));

            // Blank rejection reason rejected
            mockMvc.perform(put("/api/owner/laporan/" + tempId + "/tolak")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"kategori_penolakan\":\"Lainnya\",\"alasan_penolakan\":\"   \"}"))
                .andExpect(status().isBadRequest());

            // Valid rejection
            mockMvc.perform(put("/api/owner/laporan/" + tempId + "/tolak")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"kategori_penolakan\":\"Bukan tanggung jawab fasilitas kost\",\"alasan_penolakan\":\"Pipa bukan bagian fasilitas inventaris kost.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DITOLAK"))
                .andExpect(jsonPath("$.kategori_penolakan").value("Bukan tanggung jawab fasilitas kost"))
                .andExpect(jsonPath("$.alasan_penolakan").value("Pipa bukan bagian fasilitas inventaris kost."))
                .andExpect(jsonPath("$.tanggal_diproses").isNotEmpty());

            mockMvc.perform(get("/api/owner/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ringkasan.ditolak", greaterThanOrEqualTo(1)));

            mockMvc.perform(get("/api/owner/laporan")
                    .param("status", "DITOLAK")
                    .param("q", tempKode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laporan", hasSize(1)))
                .andExpect(jsonPath("$.laporan[0].id").value(tempId));
        } finally {
            if (laporanRepository.existsById(tempId)) {
                laporanRepository.deleteById(tempId);
            }
        }
    }

    @Test
    void mekanikLoginDashboardFilterAndCompletionFlowWork() throws Exception {
        // 1. Seeded Mekanik login works
        MvcResult loginResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"mekanik@kost.com\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nama").value("Pak Budi"))
            .andExpect(jsonPath("$.role").value("MEKANIK"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andReturn();

        long mekanikId = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Mekanik dashboard returns seeded DIPROSES tasks
        mockMvc.perform(get("/api/mekanik/" + mekanikId + "/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ringkasan.total", greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.ringkasan.diproses", greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.laporan", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$.laporan[0].mekanik_id").value(mekanikId));

        // Non-mechanic ID rejected on mechanic endpoints
        User mori = userRepository.findByEmailIgnoreCase("penghuni@kost.com").orElseThrow();
        mockMvc.perform(get("/api/mekanik/" + mori.getId() + "/dashboard"))
            .andExpect(status().isBadRequest());

        // Filter & search mechanic tasks
        mockMvc.perform(get("/api/mekanik/" + mekanikId + "/tugas")
                .param("status", "DIPROSES")
                .param("q", "AC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.laporan", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.laporan[0].status").value("DIPROSES"))
            .andExpect(jsonPath("$.laporan[0].catatan_owner").isNotEmpty());

        // 3. Full multipart progress + completion flow on temporary task
        MvcResult createResult = mockMvc.perform(multipart("/api/laporan")
                .param("penghuni_id", String.valueOf(mori.getId()))
                .param("kategori_fasilitas", "Furnitur & Bangunan")
                .param("lokasi_kamar", "Kamar A-12 · Lemari Pakaian")
                .param("deskripsi", "Engsel pintu lemari patah untuk uji alur teknisi mekanik."))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long tempId = createdJson.get("id").asLong();
        String tempKode = createdJson.get("kode_laporan").asText();

        Path firstPhotoPath = null;
        Path secondPhotoPath = null;
        boolean deletedViaApi = false;

        try {
            mockMvc.perform(put("/api/owner/laporan/" + tempId + "/setujui")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format(
                        "{\"mekanik_id\":%d,\"prioritas\":\"Tinggi\",\"catatan_owner\":\"Ganti engsel stainless baru.\"}",
                        mekanikId
                    )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DIPROSES"))
                .andExpect(jsonPath("$.mekanik_id").value(mekanikId));

            // Invalid status MENUNGGU rejected
            mockMvc.perform(multipart(HttpMethod.PUT, "/api/mekanik/tugas/" + tempId + "/selesai")
                    .param("status", "MENUNGGU")
                    .param("catatanMekanik", "Status tidak valid"))
                .andExpect(status().isBadRequest());

            // Progress update stays DIPROSES without setting tanggal_selesai
            mockMvc.perform(multipart(HttpMethod.PUT, "/api/mekanik/tugas/" + tempId + "/selesai")
                    .param("status", "DIPROSES")
                    .param("catatanMekanik", "Engsel lama sudah dilepas, menunggu baut cadangan."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DIPROSES"))
                .andExpect(jsonPath("$.catatan_mekanik").value("Engsel lama sudah dilepas, menunggu baut cadangan."))
                .andExpect(jsonPath("$.tanggal_selesai").isEmpty());

            // Complete task with photo upload
            MockMultipartFile firstPhoto = new MockMultipartFile(
                "fotoSelesai",
                "bukti-selesai-1.jpg",
                "image/jpeg",
                new byte[] {1, 2, 3, 4}
            );
            MvcResult finishResult = mockMvc.perform(multipart(HttpMethod.PUT, "/api/mekanik/tugas/" + tempId + "/selesai")
                    .file(firstPhoto)
                    .param("status", "SELESAI")
                    .param("catatanMekanik", "Engsel lemari baru terpasang kokoh dan berfungsi normal."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SELESAI"))
                .andExpect(jsonPath("$.catatan_mekanik").value("Engsel lemari baru terpasang kokoh dan berfungsi normal."))
                .andExpect(jsonPath("$.foto_selesai_url", startsWith("/uploads/")))
                .andExpect(jsonPath("$.tanggal_selesai").isNotEmpty())
                .andReturn();

            String firstUrl = objectMapper.readTree(finishResult.getResponse().getContentAsString())
                .get("foto_selesai_url").asText();
            firstPhotoPath = fileStorageService.getUploadDir().resolve(firstUrl.replace("/uploads/", ""));
            assertTrue(Files.exists(firstPhotoPath), "Uploaded completion photo should exist on disk");
            mockMvc.perform(get(firstUrl)).andExpect(status().isOk());

            // Re-upload replacement photo and verify old file is cleaned up
            MockMultipartFile secondPhoto = new MockMultipartFile(
                "fotoSelesai",
                "bukti-selesai-2.png",
                "image/png",
                new byte[] {5, 6, 7, 8}
            );
            MvcResult updatePhotoResult = mockMvc.perform(multipart(HttpMethod.PUT, "/api/mekanik/tugas/" + tempId + "/selesai")
                    .file(secondPhoto)
                    .param("status", "SELESAI")
                    .param("catatanMekanik", "Foto selesai diperbarui dengan pencahayaan lebih jelas."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foto_selesai_url", not(firstUrl)))
                .andReturn();

            String secondUrl = objectMapper.readTree(updatePhotoResult.getResponse().getContentAsString())
                .get("foto_selesai_url").asText();
            secondPhotoPath = fileStorageService.getUploadDir().resolve(secondUrl.replace("/uploads/", ""));
            assertFalse(Files.exists(firstPhotoPath), "Old completion photo should be deleted when replaced");
            assertTrue(Files.exists(secondPhotoPath), "New completion photo should exist on disk");

            // Search completed task in mechanic tasks list
            mockMvc.perform(get("/api/mekanik/" + mekanikId + "/tugas")
                    .param("status", "SELESAI")
                    .param("q", tempKode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laporan", hasSize(1)))
                .andExpect(jsonPath("$.laporan[0].id").value(tempId))
                .andExpect(jsonPath("$.ringkasan.selesai", greaterThanOrEqualTo(1)));

            // Delete temporary report and ensure completion photo file is deleted too
            mockMvc.perform(delete("/api/laporan/" + tempId)).andExpect(status().isOk());
            deletedViaApi = true;
            assertFalse(Files.exists(secondPhotoPath), "Deleting laporan should delete foto_selesai_url file too");
        } finally {
            if (!deletedViaApi && laporanRepository.existsById(tempId)) {
                laporanRepository.deleteById(tempId);
            }
            if (firstPhotoPath != null) {
                Files.deleteIfExists(firstPhotoPath);
            }
            if (secondPhotoPath != null) {
                Files.deleteIfExists(secondPhotoPath);
            }
        }
    }

    @Test
    void staticFrontendPagesServeSuccessfully() throws Exception {
        String[] paths = {
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
            "/owner-home.html",
            "/owner-laporan.html",
            "/owner-detail-laporan.html",
            "/owner-setujui.html",
            "/owner-tolak.html",
            "/owner-konfirmasi.html",
            "/mekanik-home.html",
            "/mekanik-tugas.html",
            "/mekanik-detail-tugas.html",
            "/mekanik-update-pekerjaan.html",
            "/mekanik-selesai.html",
            "/css/style.css",
            "/js/app.js"
        };
        for (String path : paths) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }
}
