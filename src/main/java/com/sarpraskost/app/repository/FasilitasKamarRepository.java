package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.FasilitasKamar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FasilitasKamarRepository extends JpaRepository<FasilitasKamar, Integer> {

    @EntityGraph(attributePaths = {"kamar", "kategori"})
    List<FasilitasKamar> findAllByOrderByIdFasilitasDesc();

    @EntityGraph(attributePaths = {"kamar", "kategori"})
    List<FasilitasKamar> findByKamarIdKamarOrderByIdFasilitasDesc(Integer idKamar);

    @EntityGraph(attributePaths = {"kamar", "kategori"})
    List<FasilitasKamar> findByKamarNomorKamarIgnoreCaseOrderByIdFasilitasDesc(String nomorKamar);

    @EntityGraph(attributePaths = {"kamar", "kategori"})
    List<FasilitasKamar> findByKategoriIdKategoriOrderByIdFasilitasDesc(Integer idKategori);

    @Query("SELECT f FROM FasilitasKamar f JOIN FETCH f.kamar JOIN FETCH f.kategori WHERE f.idFasilitas = :id")
    Optional<FasilitasKamar> findDetailById(@Param("id") Integer id);

    Optional<FasilitasKamar> findByKodeAsetIgnoreCase(String kodeAset);

    boolean existsByKodeAsetIgnoreCase(String kodeAset);

    long countByKamarIdKamar(Integer idKamar);

    long countByKategoriIdKategori(Integer idKategori);

    List<FasilitasKamar> findByKodeAsetStartingWith(String prefix);
}
