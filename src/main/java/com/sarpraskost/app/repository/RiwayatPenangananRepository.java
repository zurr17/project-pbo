package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.RiwayatPenanganan;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiwayatPenangananRepository extends JpaRepository<RiwayatPenanganan, Integer> {

    @EntityGraph(attributePaths = {"pengguna"})
    List<RiwayatPenanganan> findByLaporanIdLaporanOrderByWaktuUpdateAscIdRiwayatAsc(Integer idLaporan);

    @EntityGraph(attributePaths = {"pengguna"})
    List<RiwayatPenanganan> findByLaporanIdLaporanInOrderByWaktuUpdateAscIdRiwayatAsc(Collection<Integer> laporanIds);

    void deleteByLaporanIdLaporan(Integer idLaporan);

    long countByPenggunaIdPengguna(Integer idPengguna);
}
