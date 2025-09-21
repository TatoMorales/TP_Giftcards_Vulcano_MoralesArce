package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserSessionTests {

    private UserSession createValidUserSession(){
        return new UserSession("Papu", "papuPass", Map.of("Papu", "papuPass"));
    }
    private GiftCard defaultGiftCard(){
        return new GiftCard("dfsofnd", 50.0f, new ArrayList<Charge>());
    }
    @Test public void test01CanCreateSessionWithValidCredentials(){
        assertDoesNotThrow(()->createValidUserSession());
    }

    @Test public void test02CanNotCreateSessionWithInvalidCredentials(){
        assertThrows(RuntimeException.class, ()->new UserSession("Sarf", "fingit", Map.of("Papu", "papuPass")));
    }

    @Test public void test03TokenExpiresFiveMinutesAfterCreation(){
        UserSession userSession = createValidUserSession();
        assertFalse(userSession.isValidAt(userSession.getCreationTime().plusMinutes(6)));
    }

    @Test public void test04TokenIsValidBeforeExpiration() {
        UserSession userSession = createValidUserSession();
        assertTrue(userSession.isValidAt(userSession.getCreationTime().plusMinutes(3)));
    }

    @Test public void test05UserCanClaimGiftCard(){
        GiftCard giftCard = defaultGiftCard();
        assertTrue(createValidUserSession().claimGiftCard(giftCard).listGiftCards().containsKey(giftCard.getId()));
    }

    @Test public void test06UserCanFindGiftCardById(){
        GiftCard giftCard = defaultGiftCard();
        assertEquals(giftCard, createValidUserSession().claimGiftCard(giftCard).findGiftCard(giftCard.getId()));
    }

    @Test public void test07UserCanNotFindUnclaimedGiftCardById(){
        GiftCard giftCard = defaultGiftCard();
        assertThrows(RuntimeException.class, ()->createValidUserSession().findGiftCard(giftCard.getId()));
    }
}