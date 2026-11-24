package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.RiwayatPenanganan;
import com.sarpraskost.app.entity.Role;
import com.sarpraskost.app.entity.StatusTiket;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RiwayatPenangananResponse(
    @JsonProperty("id_riwayat") Integer idRiwayat,
    @JsonProperty("id_laporan") Integer idLaporan,
    @JsonProperty("id_pengguna") Integer idPengguna,
    @JsonProperty("nama_pengguna") String namaPengguna,
    @JsonProperty("role_pengguna") Role rolePengguna,
    @JsonProperty("status_baru") StatusTiket statusBaru,
    @JsonProperty("catatan_penanganan") String catatanPenanganan,
    @JsonProperty("foto_sesudah_url") String fotoSesudahUrl,
    @JsonProperty("biaya_perbaikan") BigDecimal biayaPerbaikan,
    @JsonProperty("waktu_update") LocalDateTime waktuUpdate
) {
    public static RiwayatPenangananResponse from(RiwayatPenanganan r) {
        Pengguna actor = r.getPengguna();
        return new RiwayatPenangananResponse(
            r.getIdRiwayat(),
            r.getLaporan().getIdLaporan(),
            actor.getIdPengguna(),
            actor.getNamaLengkap(),
            actor.getRole(),
            r.getStatusBaru(),
            r.getCatatanPenanganan(),
            r.getFotoSesudahUrl(),
            r.getBiayaPerbaikan(),
            r.getWaktuUpdate()
        );
    }

    public RiwayatPenangananResponse withoutBiaya() {
        return new RiwayatPenangananResponse(
            idRiwayat,
            idLaporan,
            idPengguna,
            namaPengguna,
            rolePengguna,
            statusBaru,
            catatanPenanganan,
            fotoSesudahUrl,
            null,
            waktuUpdate
        );
    }
}
