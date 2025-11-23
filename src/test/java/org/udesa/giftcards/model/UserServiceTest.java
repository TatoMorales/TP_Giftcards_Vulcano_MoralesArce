package org.udesa.giftcards.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired private UserService userService;

    @Test public void test01CanFindUserByName() {
        userService.save(new UserVault("NombreUnico", "Pass"));

        UserVault found = userService.findByName("NombreUnico");
        assertNotNull(found);
        assertEquals("Pass", found.getPassword());
    }

    @Test public void test02FindByNameFailsIfUserDoesNotExist() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.findByName("UsuarioInexistente");
        });
        assertEquals("El usuario no existe", ex.getMessage());
    }

    @Test public void test03CannotCreateDuplicateUsers() {
        userService.save(new UserVault("Duplicado", "Pass1"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.save(new UserVault("Duplicado", "Pass2"));
        });
    }
}