package org.udesa.giftcards.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MerchantServiceTest {
    @Autowired private MerchantService merchantService;

    @Test public void test01CanFindMerchantByName() {
        merchantService.save(new MerchantVault("Starbucks"));
        MerchantVault found = merchantService.findByName("Starbucks");
        assertNotNull(found);
        assertEquals("Starbucks", found.getName());
    }

    @Test public void test02FindByNameFailsIfMerchantDoesNotExist() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            merchantService.findByName("ComercioFantasma");
        });
        assertEquals("Merchant no registrado", ex.getMessage());
    }

    @Test public void test03CannotCreateDuplicateMerchants() {
        merchantService.save(new MerchantVault("Adidas"));
        assertThrows(DataIntegrityViolationException.class, () -> {
            merchantService.save(new MerchantVault("Adidas"));
        });
    }

    @Test public void test04ExistsMethodWorks() {
        merchantService.save(new MerchantVault("Nike"));

        assertTrue(merchantService.exists("Nike"));
        assertFalse(merchantService.exists("Puma"));
    }
}
