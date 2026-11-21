package com.sarpraskost.app.repository;

import com.sarpraskost.app.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findFirstByNomorKamarIgnoreCase(String nomorKamar);
}
