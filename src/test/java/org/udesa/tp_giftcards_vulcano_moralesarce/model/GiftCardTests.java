package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class GiftCardTests {
    private static final String defautId = "x12rlls928";
    private float defaultInitialBalance = 6.5f;

    public GiftCard defaultGiftCard(){
        return new GiftCard(defautId, defaultInitialBalance);
    }

    @Test public void test01GiftCardStartsWithNoCharges(){
        assertTrue(defaultGiftCard().hasNoCharges());
    }
    @Test public void test02GiftCardStartsWithInitialBalance(){
        assertEquals(defaultGiftCard().getBalance(), defaultInitialBalance);
    }
    @Test public void test03GiftCardBalanceDecreasesWhenChargeIsMade(){
        assertEquals(defaultInitialBalance - 2.0f,
                     defaultGiftCard().charge("Nike", 2.0f, LocalDate.now()).getBalance());
    }

    @Test public void test04GiftCardCannotBeOvercharged(){
        assertThrows(RuntimeException.class,
                     ()-> defaultGiftCard().charge("Nike", 10.0f, LocalDate.now()));
    }

    @Test public void test05GiftCardStoresChargesWithMerchantAndDate(){
        LocalDate todayDate = LocalDate.now();
        Charge aCharge = defaultGiftCard().charge("Nike", 2.0f, LocalDate.now()).getCharges().getFirst();
        assertEquals("Nike",  aCharge.getMerchant());
        assertEquals(2.0f, aCharge.getAmount());
        assertEquals(todayDate, aCharge.getDate());
    }

}
