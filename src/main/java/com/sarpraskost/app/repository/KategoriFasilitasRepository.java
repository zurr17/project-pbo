package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.KategoriFasilitas;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KategoriFasilitasRepository extends JpaRepository<KategoriFasilitas, Long> {
    Optional<KategoriFasilitas> findByNamaKategoriIgnoreCase(String namaKategori);
    List<KategoriFasilitas> findAllByOrderByNamaKategoriAsc();
}
