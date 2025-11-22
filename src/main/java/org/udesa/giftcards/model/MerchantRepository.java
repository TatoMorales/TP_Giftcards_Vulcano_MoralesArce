package org.udesa.giftcards.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<MerchantVault, Long> {
    Optional<MerchantVault> findByName(String name);
}
