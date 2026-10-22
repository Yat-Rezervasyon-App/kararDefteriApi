package com.tein.karardefteri.user.repository;

import com.tein.karardefteri.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
