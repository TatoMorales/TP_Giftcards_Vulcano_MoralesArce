package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GiftCardSystemFacadeTests {
    GiftCardSystemFacade systemFacade;

    @BeforeEach
    public void beforeEach() {
        systemFacade = systemFacade();
    }

    @Test
    public void test01CanLoginWithValidUser() {
        assertDoesNotThrow(() -> systemFacade.createSessionFor("Jhon", "Jpass"));
    }

    @Test
    public void test02CannotLoginWithInvalidUser() {
        assertThrows(RuntimeException.class, () -> systemFacade.createSessionFor("George", "badPass"));
    }

    @Test
    public void test03UserCanClaimGiftCardThroughFacade() {
        UserSession session = systemFacade.createSessionFor("Jhon", "Jpass");
        GiftCard giftCard = new GiftCard("card123", 100.0f, new ArrayList<>());
        systemFacade.addGiftCard(giftCard);

        systemFacade.claimGiftCard(session, "card123");

        assertTrue(session.listGiftCards().containsKey("card123"));
    }

    @Test
    public void test04UserCanChargeGiftCardThroughFacade() {
        UserSession session = systemFacade.createSessionFor("Paul", "Ppass");
        GiftCard giftCard = new GiftCard("cardABC", 50.0f, new ArrayList<>());
        systemFacade.addGiftCard(giftCard);

        systemFacade.claimGiftCard(session, "cardABC");
        systemFacade.chargeGiftCard(session, "cardABC", "Nike", 20.0f, LocalDate.now());

        assertEquals(30.0f, session.findGiftCard("cardABC").getBalance());
    }

    @Test
    public void test05FacadeRejectsOperationsWithExpiredSession() {
        UserSession session = systemFacade.createSessionFor("Jhon", "Jpass");
        GiftCard giftCard = new GiftCard("cardX", 40.0f, new ArrayList<>());
        systemFacade.addGiftCard(giftCard);

        // Forzar expiración
        assertFalse(session.isValidAt(session.getCreationTime().plusMinutes(6)));

        assertThrows(RuntimeException.class,
                () -> systemFacade.claimGiftCard(session, "cardX"));
    }

    private static GiftCardSystemFacade systemFacade() {
        return new GiftCardSystemFacade(
                Map.of("Jhon", "Jpass", "Paul", "Ppass"),
                Map.of()
        );
    }
}

