package com.example.taykotoproject.repository;

import com.example.taykotoproject.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Users findByUsername(String name);

    Boolean existsByUsername(String username);

    Optional<Users> findByEmail(String email);
}
