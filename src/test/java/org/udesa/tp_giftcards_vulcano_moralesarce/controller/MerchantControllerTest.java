package org.udesa.tp_giftcards_vulcano_moralesarce.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.udesa.tp_giftcards_vulcano_moralesarce.model.GiftCard;
import org.udesa.tp_giftcards_vulcano_moralesarce.model.GiftCardSystemFacade;
import org.udesa.tp_giftcards_vulcano_moralesarce.model.UserSession;

@SpringBootTest
@AutoConfigureMockMvc
class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private GiftCardSystemFacade giftCardSystemFacade;

    @Test void test01statusEndpointReturnsMessage() throws Exception {
        mockMvc.perform(get("/merchant/status"))
            .andExpect(status().isOk())
            .andExpect(content().string("Service is up and running"));
    }

    @Test void test02paymentEndpointProcessesCharge() throws Exception {
        MerchantController.PaymentRequest request = new MerchantController.PaymentRequest(
            "owner",
            "secret",
            "GC-100",
            100f,
            "tienda",
            null
        );

        mockMvc.perform(post("/merchant/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.transactionId").isNotEmpty())
            .andExpect(jsonPath("$.giftCardId").value("GC-100"))
            .andExpect(jsonPath("$.amount").value(100.0))
            .andExpect(jsonPath("$.remainingBalance").value(4900.0f))
            .andExpect(jsonPath("$.message").value("Cargo realizado para tienda"));
    }
    @Test void test03paymentFailsWithInvalidUser() throws Exception {
        MerchantController.PaymentRequest request = new MerchantController.PaymentRequest(
                "wrongUser",
                "wrongPass",
                "GC-100",
                100f,
                "tienda",
                null
        );

        mockMvc.perform(post("/merchant/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(UserSession.invalidCredentialsErrorDescription));
    }

    @Test void test04paymentFailsWithNonExistingGiftCard() throws Exception {
        MerchantController.PaymentRequest request = new MerchantController.PaymentRequest(
                "owner",
                "secret",
                "GC-999", // no existe
                100f,
                "tienda",
                null
        );

        mockMvc.perform(post("/merchant/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("GiftCard not found"));
    }

    @Test void test05paymentFailsWithInsufficientBalance() throws Exception {
        MerchantController.PaymentRequest request = new MerchantController.PaymentRequest(
                "owner",
                "secret",
                "GC-100",
                999999f, // monto mayor al saldo
                "tienda",
                null
        );

        mockMvc.perform(post("/merchant/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(GiftCard.insufficientBalance));
    }

    @Test void test06paymentFailsWhenRequiredFieldsAreMissing() throws Exception {
        MerchantController.PaymentRequest request = new MerchantController.PaymentRequest(
                null,
                "secret",
                "GC-100",
                100f,
                "tienda",
                null
        );

        mockMvc.perform(post("/merchant/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("username is required"));
    }

}
