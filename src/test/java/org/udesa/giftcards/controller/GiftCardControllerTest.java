package org.udesa.giftcards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.udesa.giftcards.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GiftCardControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private Clock clock;

    // Inyecto los servicios reales para configurar el estado de la DB
    @Autowired private UserService userService;
    @Autowired private GiftCardService giftCardService;

    @BeforeEach public void beforeEach() {
        when(clock.now()).then(it -> LocalDateTime.now());
        // Opcional: limpiar DB si no usas @Transactional en el test o H2 en memoria
    }

    // para crear usuarios usando UserVault
    private UserVault registerUser(String name, String pass) {
        return userService.save(new UserVault(name, pass));
    }

    // para crear GiftCards
    private GiftCard createCard(String businessId, int initialBalance) {
        return giftCardService.save(new GiftCard(businessId, initialBalance));
    }

    @Test public void test01CanLoginWithValidUser() throws Exception {
        registerUser("aUser", "aPass");
        Map<String, Object> response = login("aUser", "aPass");
        assertNotNull(response.get("token"));
    }

    @Test public void test02CanNotLoginWithInvalidUser() throws Exception {
        // No registramos el usuario
        loginFailing("InvalidUser", "aPass");
    }

    @Test public void test03CanRedeemGiftCard() throws Exception {
        registerUser("aUser", "aPass");
        createCard("card1", 1000);
        String token = loginAndGetToken("aUser", "aPass");
        redeem(token, "card1");
        Map<String, Object> balanceMap = balance(token, "card1");
        assertEquals(1000, balanceMap.get("balance"));
    }

    @Test public void test04CanNotRedeemAlreadyOwnedCard() throws Exception {
        registerUser("UserA", "PassA");
        registerUser("UserB", "PassB");
        createCard("card1", 1000);

        String tokenA = loginAndGetToken("UserA", "PassA");
        String tokenB = loginAndGetToken("UserB", "PassB");

        redeem(tokenA, "card1");

        // UserB intenta redimir la misma tarjeta
        redeemFailing(tokenB, "card1");
    }

    @Test public void test05CanChargeCard() throws Exception {
        registerUser("aUser", "aPass");
        createCard("card1", 1000);
        // Asumimos que los merchants están pre-cargados o validados de otra forma,
        // o agregamos un MerchantService si fuera necesario.

        String token = loginAndGetToken("aUser", "aPass");
        redeem(token, "card1");

        charge("AnyMerchant", "card1", 200, "Books"); // Si validas merchant, crea uno antes

        Map<String, Object> response = balance(token, "card1");
        assertEquals(800, response.get("balance"));
    }

    // --- HELPERS (Métodos privados para HTTP requests, igual que antes) ---

    private String loginAndGetToken(String user, String pass) throws Exception {
        return (String) login(user, pass).get("token");
    }

    private Map<String, Object> login(String user, String pass) throws Exception {
        return new ObjectMapper().readValue(
                mockMvc.perform(post("/api/giftcards/login")
                                .param("user", user)
                                .param("pass", pass))
                        .andDo(print())
                        .andExpect(status().is(200))
                        .andReturn().getResponse().getContentAsString(),
                HashMap.class
        );
    }

    private void loginFailing(String user, String pass) throws Exception {
        mockMvc.perform(post("/api/giftcards/login")
                        .param("user", user)
                        .param("pass", pass))
                .andDo(print())
                .andExpect(status().is(500));
    }

    private void redeem(String token, String cardId) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/redeem")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(200));
    }

    private void redeemFailing(String token, String cardId) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/redeem")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(500));
    }

    private Map<String, Object> balance(String token, String cardId) throws Exception {
        return new ObjectMapper().readValue(
                mockMvc.perform(get("/api/giftcards/" + cardId + "/balance")
                                .header("Authorization", "Bearer " + token))
                        .andDo(print())
                        .andExpect(status().is(200))
                        .andReturn().getResponse().getContentAsString(),
                HashMap.class
        );
    }

    private void charge(String merchant, String cardId, int amount, String desc) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/charge")
                        .param("merchant", merchant)
                        .param("amount", String.valueOf(amount))
                        .param("description", desc))
                .andDo(print())
                .andExpect(status().is(200));
    }
}