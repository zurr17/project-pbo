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

public interface LaporanKerusakanRepository extends JpaRepository<LaporanKerusakan, Integer> {

    @EntityGraph(attributePaths = {"pengguna", "pengguna.kamar", "fasilitas", "fasilitas.kamar", "fasilitas.kategori"})
    List<LaporanKerusakan> findByPenggunaIdPenggunaOrderByTglLaporDescIdLaporanDesc(Integer idPengguna);

    @EntityGraph(attributePaths = {"pengguna", "pengguna.kamar", "fasilitas", "fasilitas.kamar", "fasilitas.kategori"})
    List<LaporanKerusakan> findAllByOrderByTglLaporDescIdLaporanDesc();

    @Query("SELECT l FROM LaporanKerusakan l JOIN FETCH l.pengguna p LEFT JOIN FETCH p.kamar JOIN FETCH l.fasilitas f JOIN FETCH f.kamar JOIN FETCH f.kategori WHERE l.idLaporan = :id")
    Optional<LaporanKerusakan> findDetailById(@Param("id") Integer id);

    @Query("SELECT l FROM LaporanKerusakan l JOIN FETCH l.pengguna p LEFT JOIN FETCH p.kamar JOIN FETCH l.fasilitas f JOIN FETCH f.kamar k JOIN FETCH f.kategori "
        + "WHERE (UPPER(k.nomorKamar) = 'UMUM' OR LOWER(k.tipeKamar) LIKE '%umum%') AND l.statusTiket IN :statuses "
        + "ORDER BY l.isUrgent DESC, l.jumlahTerdampak DESC, l.tglLapor DESC")
    List<LaporanKerusakan> findActiveFasilitasUmum(@Param("statuses") Collection<StatusTiket> statuses);

    Optional<LaporanKerusakan> findByKodeTiketIgnoreCase(String kodeTiket);

    Optional<LaporanKerusakan> findFirstByFasilitasIdFasilitasAndStatusTiketInOrderByTglLaporDescIdLaporanDesc(
        Integer idFasilitas,
        Collection<StatusTiket> statuses
    );

    List<LaporanKerusakan> findByKodeTiketStartingWith(String prefix);

    long countByPenggunaIdPengguna(Integer idPengguna);

    long countByFasilitasIdFasilitas(Integer idFasilitas);

    long countByFasilitasIdFasilitasAndStatusTiketIn(Integer idFasilitas, Collection<StatusTiket> statuses);

    List<LaporanKerusakan> findByPenggunaIdPengguna(Integer idPengguna);
}
