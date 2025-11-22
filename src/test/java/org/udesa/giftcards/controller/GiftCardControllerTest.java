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
import java.util.List;
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

    @Autowired private UserService userService;
    @Autowired private GiftCardService giftCardService;
    @Autowired private MerchantService merchantService;

    @BeforeEach public void beforeEach() {
        when(clock.now()).then(it -> LocalDateTime.now());
    }

    private UserVault registerUser(String name, String pass) {
        return userService.save(new UserVault(name, pass));
    }

    private GiftCard createCard(String businessId, int initialBalance) {
        return giftCardService.save(new GiftCard(businessId, initialBalance));
    }

    private void registerMerchant(String name) {
        merchantService.save(new MerchantVault(name));
    }

    // refactor para no tener que crear una sesión con tarjeta desde cero
    private String startValidSessionWithACard(String user, String pass, String cardId, int amount) throws Exception {
        registerUser(user, pass);
        createCard(cardId, amount);
        String token = loginAndGetToken(user, pass);
        redeem(token, cardId);
        return token;
    }

    @Test public void test01CanLoginWithValidUser() throws Exception {
        registerUser("ElPapu", "Vulcano");
        assertNotNull(login("ElPapu", "Vulcano").get("token"));
    }

    @Test public void test02CanNotLoginWithInvalidUser() throws Exception {
        loginFailing("UsuarioQueNoExiste", "jijijija");
    }

    @Test public void test03CanRedeemGiftCard() throws Exception {
        String token = startValidSessionWithACard("Nico Lopez", "Niagara", "NicoCard", 1000);
        assertEquals(1000, balance(token, "NicoCard").get("balance"));
    }
    @Test public void test04CanNotRedeemInvalidGiftCard() throws Exception {
        registerUser("Nate", "ButtersClan");
        String token = loginAndGetToken("Nate", "ButtersClan");
        redeemFailing(token, "GiftCardQueNoExiste");
    }

    @Test public void test05UserCanRedeemMultipleGiftCards() throws Exception {
        String token = startValidSessionWithACard("ElLocoOss", "Gadorchix", "CipreCard", 1000);
        createCard("OtraCard", 500);
        redeem(token, "OtraCard");
        assertEquals(1000, balance(token, "CipreCard").get("balance"));
        assertEquals(500, balance(token, "OtraCard").get("balance"));
    }

    @Test public void test06CanNotRedeemAlreadyOwnedCard() throws Exception {
        startValidSessionWithACard("CiroRussi", "Coo-ked", "RussiCard", 1000);
        registerUser("ElLocoOss", "Gadorchix");
        String tokenB = loginAndGetToken("ElLocoOss", "Gadorchix");
        redeemFailing(tokenB, "RussiCard");
    }

    @Test public void test07CanChargeCard() throws Exception {
        String token = startValidSessionWithACard("NicoLopez", "Niagara", "NicoCard", 1000);
        registerMerchant("Betsson");
        charge("Betsson", "NicoCard", 200, "GoldParty");
        assertEquals(800, balance(token, "NicoCard").get("balance"));
    }

    @Test public void test08UnknownMerchantCanNotCharge() throws Exception {
        String token = startValidSessionWithACard("UserA", "PassA", "Card1", 1000);
        chargeFailing("MercadoLibre", "Card1", 100, "Unknown Merchant");
    }

    @Test public void test09CanNotUseInvalidToken() throws Exception {
        String fakeToken = "00000000-0000-0000-0000-000000000000";
        createCard("Card1", 100);
        redeemFailing(fakeToken, "Card1");
    }

    @Test public void test10UserCanNotCheckBalanceOfNotOwnedCard() throws Exception {
        startValidSessionWithACard("CiroRussi", "Coo-ked", "RussiCard", 100);
        registerUser("PapuVulcano", "LaMerchantAPI");
        String tokenB = loginAndGetToken("PapuVulcano", "LaMerchantAPI");
        balanceFailing(tokenB, "RussiCard");
    }

    @Test public void test11CanNotOverchargeCard() throws Exception {
        String token = startValidSessionWithACard("NicoLopez", "Niagara", "NiburCard", 100);
        registerMerchant("Betsson");
        chargeFailing("Betsson", "NiburCard", 101, "Too Expensive");
    }

    @Test public void test12CanListCardDetails() throws Exception {
        String token = startValidSessionWithACard("CiroRussi", "Coo-ked", "RussiCard", 1000);
        registerMerchant("DiDi");
        charge("DiDi", "RussiCard", 200, "Didi a lo del nibur");
        charge("DiDi", "RussiCard", 100, "Didi a lo de loco");
        List<String> movements = (List<String>) details(token, "RussiCard").get("movements");

        assertEquals(2, movements.size());
        assertEquals("Didi a lo del nibur", movements.get(0));
        assertEquals("Didi a lo de loco", movements.get(1));
    }

    @Test public void test13CanNotOperateWithExpiredToken() throws Exception {
        String token = startValidSessionWithACard("ElPapu", "Vulcano", "VulcanCard", 1000);
        // Simulo que pasaron 30 minutos
        when(clock.now()).thenReturn(LocalDateTime.now().plusMinutes(31));
        chargeFailing("Amazon", "VulcanCard", 100, "Late buy"); // Debería de fallar con 500
    }

    @Test public void test14cannotRedeemWithInvalidTokenHeader() throws Exception {
        registerUser("user", "pass");
        createCard("card1", 1000);
        mockMvc.perform(post("/api/giftcards/card1/redeem")
                        .header("Authorization", "Bearer not-a-uuid")) // tiene que fallar por estar mal formateado
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test public void test15cannotRedeemWithMissingHeader() throws Exception {
        registerUser("user", "pass");
        createCard("card1", 1000);
        mockMvc.perform(post("/api/giftcards/card1/redeem"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // métodos para HTTP requests:
    private String loginAndGetToken(String user, String pass) throws Exception {
        return (String) login(user, pass).get("token");
    }

    private Map<String, Object> login(String user, String pass) throws Exception {
        return new ObjectMapper().readValue(
                mockMvc.perform(post("/api/giftcards/login").param("user", user).param("pass", pass))
                        .andDo(print()).andExpect(status().is(200))
                        .andReturn().getResponse().getContentAsString(),
                HashMap.class
        );
    }

    private void loginFailing(String user, String pass) throws Exception {
        performPostFailing("/api/giftcards/login?user=" + user + "&pass=" + pass, null);
    }

    private void redeem(String token, String cardId) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/redeem")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().is(200));
    }

    private void redeemFailing(String token, String cardId) throws Exception {
        performPostFailing("/api/giftcards/" + cardId + "/redeem", token);
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

    private void balanceFailing(String token, String cardId) throws Exception {
        performGetFailing("/api/giftcards/" + cardId + "/balance", token);
    }

    private void charge(String merchant, String cardId, int amount, String desc) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/charge")
                        .param("merchant", merchant)
                        .param("amount", String.valueOf(amount))
                        .param("description", desc))
                .andDo(print())
                .andExpect(status().is(200));
    }

    private void chargeFailing(String merchant, String cardId, int amount, String desc) throws Exception {
        mockMvc.perform(post("/api/giftcards/" + cardId + "/charge")
                        .param("merchant", merchant)
                        .param("amount", String.valueOf(amount))
                        .param("description", desc))
                .andExpect(status().is(500));
    }

    private Map<String, Object> details(String token, String cardId) throws Exception {
        return performGetAndParse("/api/giftcards/" + cardId + "/details", token);
    }

    // refactors de algunos metodos que se repiten
    private void performPostFailing(String url, String token) throws Exception {
        var request = post(url);
        if (token != null) request.header("Authorization", "Bearer " + token);
        mockMvc.perform(request).andExpect(status().is(500));
    }

    private void performGetFailing(String url, String token) throws Exception {
        var request = get(url);
        if (token != null) request.header("Authorization", "Bearer " + token);
        mockMvc.perform(request).andExpect(status().is(500));
    }

    private Map<String, Object> performGetAndParse(String url, String token) throws Exception {
        return new ObjectMapper().readValue(
                mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                        .andDo(print())
                        .andExpect(status().is(200))
                        .andReturn().getResponse().getContentAsString(),
                HashMap.class
        );
    }
}