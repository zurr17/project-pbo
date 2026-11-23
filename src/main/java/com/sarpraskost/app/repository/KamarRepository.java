package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.Kamar;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KamarRepository extends JpaRepository<Kamar, Integer> {
    Optional<Kamar> findByNomorKamarIgnoreCase(String nomorKamar);
    List<Kamar> findAllByOrderByNomorKamarAsc();
    boolean existsByNomorKamarIgnoreCase(String nomorKamar);
}
