package org.udesa.tp_giftcards_vulcano_moralesarce.model;
import org.apache.catalina.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.udesa.tp_giftcards_vulcano_moralesarce.model.GiftCardSystemFacade.*;

public class GiftCardSystemFacadeTests {
    GiftCardSystemFacade systemFacade;

    private static String validUsername1 = "ElPapuVulcano";
    private static String validPassword1 = "LaPapuPassword";
    private static String validUsername2 = "LaCabraNitales";
    private static String validPassword2 = "BroIsCooked";
    private static List<String> validMerchants =  List.of("Nike", "Kentucky Fried Chicken");

    @BeforeEach
    public void beforeEach() {
        systemFacade = systemFacade();
    }

    @Test public void test01CanLoginWithValidUser() {
        loginAsValidUser1();
        assertTrue(systemFacade.getExistingSessions().containsKey(validUsername1));
    }

    @Test public void test02CannotLoginWithInvalidUser() {
        assertThrowsLike(() -> systemFacade.login("Emilio", "Oca"), UserSession.invalidCredentialsErrorDescription);
    }

    @Test public void test03UserCanClaimGiftCardThroughFacade() {
        UserSession session = loginAsValidUser1();
        addGiftCard("card123", 100.0f);
        claimGiftCard(session, "card123");
        assertTrue(session.listGiftCards().containsKey("card123"));
    }

    @Test public void test04UserCanNotClaimUnexistingGiftCardThroughFacade() {
        UserSession session = loginAsValidUser1();
        assertThrowsLike(() -> systemFacade.claimGiftCard(session, "card123"), invalidGiftCardError);  //Es mejor hacer estos imports de variables estáticas?
    }

    @Test public void test05UserCanNotClaimRedeemedGiftCardThroughFacade() {
        addGiftCard("card123", 100.0f);
        claimGiftCard(loginAsValidUser1(), "card123");
        UserSession session = loginAsValidUser2();
        assertThrowsLike(() -> systemFacade.claimGiftCard(session, "card123"), alreadyRedeemedGiftCardError);
    }
    @Test public void test06UserCanChargeGiftCardThroughFacade() {
        UserSession session = loginAsValidUser1();
        addGiftCard("cardABC", 50.0f);
        claimGiftCard(session, "cardABC");
        systemFacade.chargeGiftCard(session, "cardABC", "Nike", 20.0f, LocalDate.now());
        assertEquals(30.0f, session.findGiftCard("cardABC").getBalance());
    }

    @Test public void test07FacadeRejectsOperationsWithExpiredSession() {
        UserSession session = loginAsValidUser1();
        addGiftCard("cardX", 40.0f);
        assertFalse(session.isValidAt(session.getCreationTime().plusMinutes(6)));
        assertThrowsLike(() -> systemFacade.claimGiftCard(session, "cardX"), invalidSessionError);
    }

    @Test public void test08FacadeCanRestartSession() {
        UserSession session = loginAsValidUser1();
        assertFalse(session.isValidAt(session.getCreationTime().plusMinutes(6)));
        assertTrue(loginAsValidUser1().isValidAt(LocalDateTime.now()));
    }

    @Test public void test09UnknownMerchantCanNotChargeGiftCardThroughFacade() {
        UserSession session = loginAsValidUser1();
        addGiftCard("cardX", 40.0f);
        assertThrowsLike(() -> systemFacade.chargeGiftCard(session, "cardX", "RandomMerchantId", 25f, LocalDate.now()), unknownMerchantError);
    }

    private UserSession loginAsValidUser1() {
        return systemFacade.login(validUsername1, validPassword1);
    }

    private UserSession loginAsValidUser2() {
        return systemFacade.login(validUsername2, validPassword2);
    }

    private void addGiftCard(String giftCardId, float balance) {
        systemFacade.addGiftCard(new GiftCard(giftCardId, balance));
    }

    private void claimGiftCard(UserSession session, String giftCardId) {
        systemFacade.claimGiftCard(session, giftCardId);
    }

    private static GiftCardSystemFacade systemFacade() {
        return new GiftCardSystemFacade(
                Map.of(validUsername1, validPassword1, validUsername2, validPassword2),
                Map.of(), validMerchants
        );
    }
    private static void assertThrowsLike(Executable executable, String message) {
        assertEquals(message, assertThrows(Exception.class, executable).getMessage());
    }
}
