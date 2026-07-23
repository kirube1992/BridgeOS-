package com.bridgeos.backend.repository;

import com.bridgeos.backend.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

}
