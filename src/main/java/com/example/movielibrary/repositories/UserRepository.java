package com.example.movielibrary.repositories;

import com.example.movielibrary.models.user.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ApplicationUser, Integer> {

    Optional<ApplicationUser> findByUsername(String username);

    boolean existsByUsername(String username);
}