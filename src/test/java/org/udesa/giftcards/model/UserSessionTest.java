package org.udesa.giftcards.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserSessionTest {

    private Clock clock;
    private LocalDateTime startTime;

    @BeforeEach
    public void setup() {
        clock = mock(Clock.class);
        startTime = LocalDateTime.now();
    }

    @Test public void test01SessionIsAliveImmediatelyAfterCreation() {
        when(clock.now()).thenReturn(startTime, startTime);
        UserSession session = new UserSession("Pepe", clock);
        assertEquals("Pepe", session.userAliveAt(clock));
    }

    @Test public void test02SessionIsAliveBeforeExpirationLimit() {
        when(clock.now()).thenReturn(startTime, startTime.plusMinutes(14).plusSeconds(59));
        UserSession session = new UserSession("Pepe", clock);
        assertEquals("Pepe", session.userAliveAt(clock));
    }

    @Test public void test03SessionIsAliveExactlyAtExpirationLimit() {
        when(clock.now()).thenReturn(startTime, startTime.plusMinutes(15));
        UserSession session = new UserSession("Pepe", clock);
        assertEquals("Pepe", session.userAliveAt(clock));
    }

    @Test public void test04SessionExpiredByOneSecond() {
        when(clock.now()).thenReturn(startTime, startTime.plusMinutes(15).plusSeconds(1));
        UserSession session = new UserSession("Pepe", clock);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            session.userAliveAt(clock);
        });
        assertEquals(UserSession.ExpiredSessionException, exception.getMessage());
    }

    @Test public void test05SessionExpiredAfterLongTime() {
        when(clock.now()).thenReturn(startTime, startTime.plusHours(1));
        UserSession session = new UserSession("Pepe", clock);
        assertThrows(RuntimeException.class, () -> session.userAliveAt(clock));
    }

    @Test public void test06SessionRespectsDifferentUsers() {
        when(clock.now()).thenReturn(startTime);
        UserSession sessionA = new UserSession("Alice", clock);
        UserSession sessionB = new UserSession("Bob", clock);
        assertEquals("Alice", sessionA.userAliveAt(clock));
        assertEquals("Bob", sessionB.userAliveAt(clock));
    }
}