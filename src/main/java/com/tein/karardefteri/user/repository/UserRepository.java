
package com.tein.karardefteri.user.repository;

import com.tein.karardefteri.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByIdAndActiveTrue(UUID id);

    boolean existsByEmailAndActiveTrue(String email);
}
