package org.udesa.giftcards.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    Optional<GiftCard> findByCardId(String cardId);
}