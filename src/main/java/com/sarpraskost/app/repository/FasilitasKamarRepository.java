package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.FasilitasKamar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FasilitasKamarRepository extends JpaRepository<FasilitasKamar, Long> {

    @EntityGraph(attributePaths = "penghuni")
    List<FasilitasKamar> findByPenghuniIdOrderByIdDesc(Long penghuniId);

    @Query("SELECT f FROM FasilitasKamar f JOIN FETCH f.penghuni WHERE f.id = :id")
    Optional<FasilitasKamar> findDetailById(@Param("id") Long id);

    long countByPenghuniId(Long penghuniId);

    void deleteByPenghuniId(Long penghuniId);
}
