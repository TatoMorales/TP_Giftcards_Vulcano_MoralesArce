package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class GiftCardTest {

    @Test public void aSimpleCard() {
        assertEquals( 10, newCard().getBalance() );
    }

    @Test public void aSimpleIsNotOwnedCard() {
        assertFalse( newCard().owned() );
    }

    @Test public void cannotChargeUnownedCards() {
        GiftCard aCard = newCard();
        assertThrows( RuntimeException.class, () -> aCard.charge( 2, "Un cargo" ) );
        assertEquals( 10, aCard.getBalance() );
        assertTrue( aCard.getCharges().isEmpty() );
    }

    @Test public void chargeACard() {
        GiftCard aCard = newCard();
        aCard.redeem( "Bob" );
        aCard.charge( 2, "Un cargo" );
        assertEquals( 8, aCard.getBalance() );
        assertEquals( "Un cargo", aCard.getCharges().getLast() );
    }

    @Test public void cannotOverrunACard() {
        GiftCard aCard = newCard();
        assertThrows( RuntimeException.class, () -> aCard.charge( 11, "Un cargo" ) );
        assertEquals( 10, aCard.getBalance() );
    }

    @Test public void cannotRedeemAlreadyOwnedCard() {
        GiftCard card = newCard();
        card.redeem("Alice");
        assertThrows(RuntimeException.class, () -> card.redeem("Bob"));
    }

    @Test public void ownedCardValidatesOwner() {
        GiftCard card = newCard();
        card.redeem("Alice");
        assertTrue(card.isOwnedBy("Alice"));
        assertFalse(card.isOwnedBy("Bob"));
    }

    @Test public void chargesAreAccumulatedInOrder() {
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
