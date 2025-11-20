package org.udesa.giftcards.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserVault, Long> {
    Optional<UserVault> findByName(String name);
}