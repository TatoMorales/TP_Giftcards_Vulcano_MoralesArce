package org.udesa.giftcards.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ModelServiceTest {
    @Autowired private UserService modelService;

    @Test public void test01CanSaveAndRetrieveById() {
        UserVault savedUser = modelService.save(new UserVault("TestUser", "Pass"));
        assertNotNull(savedUser.getId());

        UserVault retrieved = modelService.getById(savedUser.getId());
        assertEquals("TestUser", retrieved.getName());
    }

    @Test public void test02GetByIdFailsForUnknownId() {
        assertThrows(RuntimeException.class, () -> {
            modelService.getById(99999L);
        });
    }

    @Test public void test03CanUpdateObject() {
        UserVault user = modelService.save(new UserVault("Original", "Pass"));

        UserVault changes = new UserVault("Updated", "NewPass");
        UserVault updated = modelService.update(user.getId(), changes);

        assertEquals("Updated", updated.getName());
        assertEquals("NewPass", updated.getPassword());
    }

    @Test public void test04CanDeleteObject() {
        UserVault user = modelService.save(new UserVault("ToDelete", "Pass"));
        Long id = user.getId();
        modelService.delete(id);

        assertThrows(RuntimeException.class, () -> {
            modelService.getById(id);
        });
    }

    @Test public void test05CanListAll() {
        modelService.save(new UserVault("U1", "P1"));
        modelService.save(new UserVault("U2", "P2"));
        List<UserVault> all = modelService.findAll();
        assertTrue(all.size() >= 2);
    }
}