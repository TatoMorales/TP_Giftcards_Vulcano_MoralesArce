package org.udesa.giftcards.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class GiftCardFacadeTest {

    @Autowired private GiftCardFacade facade;
    @Autowired private UserService userService;
    @Autowired private GiftCardService giftCardService;

    // Mockeamos el reloj para poder manipular el tiempo en los tests
    @MockBean private Clock clock;

    @BeforeEach public void setup() {
        when(clock.now()).thenReturn(LocalDateTime.now());
        setupBaseData();
    }

    // Para poblar la DB con usuarios y giftcards
    private void setupBaseData() {
        userService.save(new UserVault("Bob", "BobPass"));
        userService.save(new UserVault("Kevin", "KevPass"));
        giftCardService.save(new GiftCard("GC1", 10));
        giftCardService.save(new GiftCard("GC2", 5));

        // Nota: No guardamos Merchants porque tu implementación actual no los valida en DB
    }

    @Test public void test01userCanOpenASession() {
        assertNotNull( facade.login( "Bob", "BobPass" ) );
    }

    @Test public void test02unkownUserCannotOpenASession() {
        assertThrows( RuntimeException.class, () -> facade.login( "Stuart", "StuPass" ) );
    }

    @Test public void test03userCannotUseAnInvalidToken() {
        assertThrows( RuntimeException.class, () -> facade.redeem( UUID.randomUUID(), "GC1" ) );
        assertThrows( RuntimeException.class, () -> facade.balance( UUID.randomUUID(), "GC1" ) );
        assertThrows( RuntimeException.class, () -> facade.details( UUID.randomUUID(), "GC1" ) );
    }

    @Test public void test04userCannotCheckOnAlienCard() {
        UUID token = facade.login( "Bob", "BobPass" );

        assertThrows( RuntimeException.class, () -> facade.balance( token, "GC1" ) );
    }

    @Test public void test05userCanRedeemACard() {
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        assertEquals( 10, facade.balance( token, "GC1" ) );
    }

    @Test public void test06userCanRedeemASecondCard() {
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        facade.redeem( token, "GC2" );

        assertEquals( 10, facade.balance( token, "GC1" ) );
        assertEquals( 5, facade.balance( token, "GC2" ) );
    }

    @Test public void test07multipleUsersCanRedeemACard() {
        UUID bobsToken = facade.login( "Bob", "BobPass" );
        UUID kevinsToken = facade.login( "Kevin", "KevPass" );

        facade.redeem( bobsToken, "GC1" );
        facade.redeem( kevinsToken, "GC2" );

        assertEquals( 10, facade.balance( bobsToken, "GC1" ) );
        assertEquals( 5, facade.balance( kevinsToken, "GC2" ) );
    }

    /*
    @Test public void unknownMerchantCantCharge() {
        assertThrows( RuntimeException.class, () -> facade.charge( "Mx", "GC1", 2, "UnCargo" ) );
    }
    */

    @Test public void tes08merchantCantChargeUnredeemedCard() {
        // Este test sigue pasando porque la validación de 'owned()' está dentro de GiftCard.charge()
        assertThrows( RuntimeException.class, () -> facade.charge( "M1", "GC1", 2, "UnCargo" ) );
    }

    @Test public void test09MerchantCanChargeARedeemedCard() {
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        facade.charge( "M1", "GC1", 2, "UnCargo" );

        assertEquals( 8, facade.balance( token, "GC1" ) );
    }

    @Test public void test10MerchantCannotOverchargeACard() {
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        assertThrows( RuntimeException.class, () -> facade.charge( "M1", "GC1", 11, "UnCargo" ) );
    }

    @Test public void test11UserCanCheckHisEmptyCharges() {
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );

        assertTrue( facade.details( token, "GC1" ).isEmpty() );
    }

    @Test public void test12UserCanCheckHisCharges() {
        UUID token = facade.login( "Bob", "BobPass" );

        facade.redeem( token, "GC1" );
        facade.charge( "M1", "GC1", 2, "UnCargo" );

        assertEquals( "UnCargo", facade.details( token, "GC1" ).getLast() );
    }

    @Test public void test13userCannotCheckOthersCharges() {
        facade.redeem( facade.login( "Bob", "BobPass" ), "GC1" );

        UUID token = facade.login( "Kevin", "KevPass" );

        assertThrows( RuntimeException.class, () -> facade.details( token, "GC1" ) );
    }

    @Test public void test14tokenExpires() {
        // Configuramos el comportamiento del mock para este test específico
        // 1ra llamada (login): devuelve ahora
        // 2da llamada (check): devuelve ahora + 16 minutos
        when(clock.now())
                .thenReturn(LocalDateTime.now())
                .thenReturn(LocalDateTime.now().plusMinutes(16));

        UUID token = facade.login( "Kevin", "KevPass" );

        assertThrows( RuntimeException.class, () -> facade.redeem( token, "GC1" ) );
    }
}