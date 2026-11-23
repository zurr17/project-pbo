package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.Notifikasi;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotifikasiRepository extends JpaRepository<Notifikasi, Integer> {

    @EntityGraph(attributePaths = {"laporan", "pengguna"})
    List<Notifikasi> findByPenggunaIdPenggunaOrderByWaktuKirimDescIdNotifikasiDesc(Integer idPengguna);

    @Query("SELECT n FROM Notifikasi n JOIN FETCH n.laporan JOIN FETCH n.pengguna WHERE n.idNotifikasi = :id")
    Optional<Notifikasi> findDetailById(@Param("id") Integer id);

    @Query("SELECT COUNT(n) FROM Notifikasi n WHERE n.pengguna.idPengguna = :idPengguna AND n.isRead = false")
    long countUnreadByPenggunaId(@Param("idPengguna") Integer idPengguna);

    void deleteByLaporanIdLaporan(Integer idLaporan);

    void deleteByPenggunaIdPengguna(Integer idPengguna);
}
