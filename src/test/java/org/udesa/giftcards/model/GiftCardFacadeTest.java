package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class GiftCardFacadeTest {

    @Autowired private GiftCardFacade facade;
    @Autowired private UserService userService;
    @Autowired private GiftCardService giftCardService;
    @MockBean private Clock clock;

    @BeforeEach
    public void setUp() {
        when(clock.now()).thenReturn(LocalDateTime.now());
        clearSessions();
    }

    @Test public void userCanOpenSession() {
        registerUser("Bob", "BobPass");
        assertNotNull(facade.login("Bob", "BobPass"));
    }

    @Test public void unknownUserCannotOpenSession() {
        assertThrows(RuntimeException.class, () -> facade.login("Stuart", "StuPass"));
    }

    @Test public void invalidTokenIsRejected() {
        registerUser("Bob", "BobPass");
        createCard("GC1", 10);
        assertThrows(RuntimeException.class, () -> facade.redeem(UUID.randomUUID(), "GC1"));
        assertThrows(RuntimeException.class, () -> facade.balance(UUID.randomUUID(), "GC1"));
        assertThrows(RuntimeException.class, () -> facade.details(UUID.randomUUID(), "GC1"));
    }

    @Test public void userCanRedeemAndSeeBalance() {
        registerUser("Bob", "BobPass");
        createCard("GC1", 10);

        UUID token = facade.login("Bob", "BobPass");
        facade.redeem(token, "GC1");

        assertEquals(10, facade.balance(token, "GC1"));
    }

    @Test public void userCannotRedeemAlreadyOwnedCard() {
        registerUser("UserA", "PassA");
        registerUser("UserB", "PassB");
        createCard("GC1", 10);

        UUID tokenA = facade.login("UserA", "PassA");
        facade.redeem(tokenA, "GC1");

        UUID tokenB = facade.login("UserB", "PassB");
        assertThrows(RuntimeException.class, () -> facade.redeem(tokenB, "GC1"));
    }

    @Test public void multipleUsersCanRedeemDifferentCards() {
        registerUser("Bob", "BobPass");
        registerUser("Kevin", "KevPass");
        createCard("GC1", 10);
        createCard("GC2", 5);

        UUID bobsToken = facade.login("Bob", "BobPass");
        UUID kevinsToken = facade.login("Kevin", "KevPass");

        facade.redeem(bobsToken, "GC1");
        facade.redeem(kevinsToken, "GC2");

        assertEquals(10, facade.balance(bobsToken, "GC1"));
        assertEquals(5, facade.balance(kevinsToken, "GC2"));
    }

    @Test public void chargeUpdatesBalance() {
        registerUser("Bob", "Pass");
        createCard("GC1", 10);
        UUID token = facade.login("Bob", "Pass");
        facade.redeem(token, "GC1");

        facade.charge("AnyMerchant", "GC1", 2, "Cargo");

        assertEquals(8, facade.balance(token, "GC1"));
    }

    @Test public void chargeFailsIfCardNotRedeemed() {
        createCard("GC1", 10);
        assertThrows(RuntimeException.class, () -> facade.charge("AnyMerchant", "GC1", 2, "Cargo"));
    }

    @Test public void chargeFailsIfBalanceInsufficient() {
        registerUser("Bob", "Pass");
        createCard("GC1", 10);
        UUID token = facade.login("Bob", "Pass");
        facade.redeem(token, "GC1");

        assertThrows(RuntimeException.class, () -> facade.charge("AnyMerchant", "GC1", 11, "Cargo"));
    }

    @Test public void userCanCheckCharges() {
        registerUser("Bob", "Pass");
        createCard("GC1", 10);
        UUID token = facade.login("Bob", "Pass");
        facade.redeem(token, "GC1");
        facade.charge("AnyMerchant", "GC1", 2, "CargoX");

        assertEquals("CargoX", facade.details(token, "GC1").getLast());
    }

    @Test public void tokenExpiresAfterFifteenMinutes() {
        LocalDateTime now = LocalDateTime.now();
        when(clock.now()).thenReturn(now, now.plusMinutes(16));
        registerUser("Bob", "Pass");
        createCard("GC1", 10);

        UUID token = facade.login("Bob", "Pass");

        assertThrows(RuntimeException.class, () -> facade.redeem(token, "GC1"));
    }

    private void registerUser(String name, String pass) {
        userService.save(new UserVault(name, pass));
    }

    private void createCard(String cardId, int initialBalance) {
        giftCardService.save(new GiftCard(cardId, initialBalance));
    }

    @SuppressWarnings("unchecked")
    private void clearSessions() {
        Map<UUID, UserSession> sessions = (Map<UUID, UserSession>) ReflectionTestUtils.getField(facade, "sessions");
        if (sessions != null) {
            sessions.clear();
        }
    }
}
