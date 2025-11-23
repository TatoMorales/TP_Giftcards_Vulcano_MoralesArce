package org.udesa.giftcards.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class GiftCardServiceTest {

    @Autowired private GiftCardService giftCardService;

    @Test public void test01CanFindByBusinessId() {
        giftCardService.save(new GiftCard("GC-67", 500));

        GiftCard found = giftCardService.findByCardId("GC-67");

        assertNotNull(found);
        assertEquals(500, found.getBalance());
    }

    @Test public void test02FindByCardIdFailsIfCardDoesNotExist() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            giftCardService.findByCardId("NonExistentCard");
        });
        assertEquals("InvalidCard", ex.getMessage());
    }

    @Test public void test03ChargesArePersistedCorrectly() {
        GiftCard card = new GiftCard("ChargeTest", 1000);
        card.redeem("User");
        card.charge(100, "FirstCharge");
        giftCardService.save(card);

        GiftCard retrieved = giftCardService.findByCardId("ChargeTest");
        assertEquals(900, retrieved.getBalance());
        assertEquals(1, retrieved.getCharges().size());
        assertEquals("FirstCharge", retrieved.getCharges().getFirst());
    }
}
