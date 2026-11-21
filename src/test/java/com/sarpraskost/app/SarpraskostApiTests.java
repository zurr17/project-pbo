package com.sarpraskost.app;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.sarpraskost.app.entity.User;
import com.sarpraskost.app.repository.LaporanKerusakanRepository;
import com.sarpraskost.app.repository.UserRepository;
import com.sarpraskost.app.service.FileStorageService;

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

        MvcResult listResult = mockMvc.perform(get("/api/laporan/penghuni/" + mori.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ringkasan.total").value(3))
            .andExpect(jsonPath("$.ringkasan.menunggu").value(1))
            .andExpect(jsonPath("$.ringkasan.diproses").value(1))
            .andExpect(jsonPath("$.ringkasan.selesai").value(1))
            .andExpect(jsonPath("$.laporan", hasSize(3)))
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
    void createLaporanWithMultipartUploadPersistsAndCleansUp() throws Exception {
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

        try {
            assertTrue(Files.exists(savedFile), "Uploaded file should exist in uploads/");
            mockMvc.perform(get(fotoUrl)).andExpect(status().isOk());
        } finally {
            laporanRepository.deleteById(createdId);
            Files.deleteIfExists(savedFile);
        }
    }

    @Test
    void staticFrontendPagesServeSuccessfully() throws Exception {
        String[] paths = {
            "/index.html",
            "/login.html",
            "/home.html",
            "/buat-laporan.html",
            "/sukses.html",
            "/riwayat.html",
            "/detail-laporan.html",
            "/css/style.css",
            "/js/app.js"
        };
        for (String path : paths) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }
}
