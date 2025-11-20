package org.udesa.giftcards.model; // o .service

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GiftCardService extends ModelService<GiftCard, GiftCardRepository> {

    @Override
    protected void updateData(GiftCard existingObject, GiftCard updatedObject) {
        // Define qué se puede actualizar administrativamente, si aplica.
        // Por ahora, quizás solo el balance si fuera una corrección manual.
        existingObject.setBalance(updatedObject.getBalance());
    }

    @Transactional(readOnly = true)
    public GiftCard findByCardId(String cardId) {
        return repository.findByCardId(cardId)
                .orElseThrow(() -> new RuntimeException("InvalidCard"));
    }
}