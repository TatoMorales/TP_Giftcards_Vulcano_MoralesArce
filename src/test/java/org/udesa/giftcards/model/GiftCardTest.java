package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GiftCardTest {

    @Test public void test01NewCardHasCorrectBalanceAndId() {
        GiftCard card = newCard();
        assertEquals("GC1", card.getCardId());
        assertEquals(10, card.getBalance());
    }
    @Test public void test02NewCardsStartUnowned() {
        assertFalse( newCard().owned() );
    }

    @Test public void test03NewCardsDontHaveCharges(){
        assertTrue(newCard().getCharges().isEmpty());
    }

    @Test public void test04cannotChargeUnownedCards() {
        GiftCard aCard = newCard();
        assertThrows( RuntimeException.class, () -> aCard.charge( 2, "Un cargo" ) );
        assertEquals( 10, aCard.getBalance() );
        assertTrue( aCard.getCharges().isEmpty() );
    }

    @Test public void test05CanChargeACard() {
        GiftCard aCard = newCard();
        aCard.redeem( "Bob" ).charge( 2, "Un cargo" );
        assertEquals( 8, aCard.getBalance() );
        assertEquals( "Un cargo", aCard.getCharges().getLast() );
    }

    @Test public void test06cannotOverrunACard() {
        GiftCard aCard = newCard();
        assertThrows( RuntimeException.class, () -> aCard.charge( 11, "Un cargo" ) );
        assertEquals( 10, aCard.getBalance() );
    }

    @Test public void test07cannotRedeemAlreadyOwnedCard() {
        GiftCard card = newCard();
        card.redeem("Alice");
        assertThrows(RuntimeException.class, () -> card.redeem("Bob"));
    }

    @Test public void test08ownedCardValidatesOwner() {
        GiftCard card = newCard();
        card.redeem("Alice");
        assertTrue(card.isOwnedBy("Alice"));
        assertFalse(card.isOwnedBy("Bob"));
    }

    @Test public void test09chargesAreAccumulatedInOrder() {
        GiftCard card = newCard();
        card.redeem("Owner");
        card.charge(3, "Compra1");
        card.charge(2, "Compra2");
        assertEquals(2, card.getCharges().size());
        assertEquals("Compra2", card.getCharges().getLast());
    }

    private GiftCard newCard() {
        return new GiftCard( "GC1", 10 );
    }

}
