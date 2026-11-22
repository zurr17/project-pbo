package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.RiwayatPenanganan;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiwayatPenangananRepository extends JpaRepository<RiwayatPenanganan, Long> {

    @EntityGraph(attributePaths = {"pengguna"})
    List<RiwayatPenanganan> findByLaporanIdLaporanOrderByWaktuUpdateAscIdRiwayatAsc(Long idLaporan);

    @EntityGraph(attributePaths = {"pengguna"})
    List<RiwayatPenanganan> findByLaporanIdLaporanInOrderByWaktuUpdateAscIdRiwayatAsc(Collection<Long> laporanIds);

    void deleteByLaporanIdLaporan(Long idLaporan);

    long countByPenggunaIdPengguna(Long idPengguna);
}
