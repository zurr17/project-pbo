package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.Kamar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KamarRepository extends JpaRepository<Kamar, Integer> {
    Optional<Kamar> findByNomorKamarIgnoreCase(String nomorKamar);
    Optional<Kamar> findByGedungIgnoreCaseAndNomorKamarIgnoreCase(String gedung, String nomorKamar);
    Optional<Kamar> findFirstByNomorKamarIgnoreCaseOrderByGedungAscIdKamarAsc(String nomorKamar);
    List<Kamar> findAllByOrderByNomorKamarAsc();
    List<Kamar> findAllByOrderByGedungAscLantaiAscNomorKamarAsc();
    boolean existsByNomorKamarIgnoreCase(String nomorKamar);
    boolean existsByGedungIgnoreCaseAndNomorKamarIgnoreCase(String gedung, String nomorKamar);
}
