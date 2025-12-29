package com.mikael.paymybuddy.Repository;

import com.mikael.paymybuddy.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndActiveTrue(Long id);

    Optional<User> findByIdAndActiveFalse(Long id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
