package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.LaporanKerusakan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaporanKerusakanRepository extends JpaRepository<LaporanKerusakan, Long> {

    @EntityGraph(attributePaths = "penghuni")
    List<LaporanKerusakan> findByPenghuniIdOrderByTanggalLaporDescIdDesc(Long penghuniId);

    @Query("SELECT l FROM LaporanKerusakan l JOIN FETCH l.penghuni WHERE l.id = :id")
    Optional<LaporanKerusakan> findDetailById(@Param("id") Long id);

    List<LaporanKerusakan> findByKodeLaporanStartingWith(String prefix);

    long countByPenghuniId(Long penghuniId);

    void deleteByPenghuniId(Long penghuniId);
}
