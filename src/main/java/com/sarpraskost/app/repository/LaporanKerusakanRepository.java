package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.LaporanKerusakan;
import com.sarpraskost.app.entity.StatusTiket;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaporanKerusakanRepository extends JpaRepository<LaporanKerusakan, Long> {

    @EntityGraph(attributePaths = {"pengguna", "pengguna.kamar", "fasilitas", "fasilitas.kamar", "fasilitas.kategori"})
    List<LaporanKerusakan> findByPenggunaIdPenggunaOrderByTglLaporDescIdLaporanDesc(Long idPengguna);

    @EntityGraph(attributePaths = {"pengguna", "pengguna.kamar", "fasilitas", "fasilitas.kamar", "fasilitas.kategori"})
    List<LaporanKerusakan> findAllByOrderByTglLaporDescIdLaporanDesc();

    @Query("SELECT l FROM LaporanKerusakan l JOIN FETCH l.pengguna p LEFT JOIN FETCH p.kamar JOIN FETCH l.fasilitas f JOIN FETCH f.kamar JOIN FETCH f.kategori WHERE l.idLaporan = :id")
    Optional<LaporanKerusakan> findDetailById(@Param("id") Long id);

    List<LaporanKerusakan> findByKodeTiketStartingWith(String prefix);

    long countByPenggunaIdPengguna(Long idPengguna);

    long countByFasilitasIdFasilitas(Long idFasilitas);

    long countByFasilitasIdFasilitasAndStatusTiketIn(Long idFasilitas, Collection<StatusTiket> statuses);

    List<LaporanKerusakan> findByPenggunaIdPengguna(Long idPengguna);
}
