package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.Pengguna;
import com.sarpraskost.app.entity.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenggunaRepository extends JpaRepository<Pengguna, Integer> {

    @EntityGraph(attributePaths = "kamar")
    Optional<Pengguna> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "kamar")
    Optional<Pengguna> findByNoHpIgnoreCase(String noHp);

    boolean existsByNoHpIgnoreCase(String noHp);

    @EntityGraph(attributePaths = "kamar")
    Optional<Pengguna> findByIdPengguna(Integer idPengguna);

    @EntityGraph(attributePaths = "kamar")
    Optional<Pengguna> findFirstByKamarNomorKamarIgnoreCaseAndRole(String nomorKamar, Role role);

    @EntityGraph(attributePaths = "kamar")
    List<Pengguna> findAllByOrderByIdPenggunaAsc();

    @EntityGraph(attributePaths = "kamar")
    List<Pengguna> findByRoleOrderByNamaLengkapAsc(Role role);

    Optional<Pengguna> findFirstByRoleOrderByIdPenggunaAsc(Role role);

    long countByRole(Role role);

    long countByKamarIdKamar(Integer idKamar);
}
