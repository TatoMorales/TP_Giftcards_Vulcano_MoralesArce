package org.udesa.tp_giftcards_vulcano_moralesarce.controller;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.udesa.tp_giftcards_vulcano_moralesarce.model.GiftCard;
import org.udesa.tp_giftcards_vulcano_moralesarce.model.GiftCardSystemFacade;
import org.udesa.tp_giftcards_vulcano_moralesarce.model.UserSession;

@RestController
@RequestMapping("/merchant")

public class MerchantController {

    private final GiftCardSystemFacade giftCardSystemFacade;

    public MerchantController(GiftCardSystemFacade giftCardSystemFacade) {
        this.giftCardSystemFacade = giftCardSystemFacade;
    }

    @GetMapping(value = "/status", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Service is up and running");
    }

    @PostMapping(value = "/payment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> payment(@RequestBody PaymentRequest request) {
        try {
            validate(request);

            UserSession session = giftCardSystemFacade.createSessionFor(request.username(), request.password());
            giftCardSystemFacade.claimGiftCard(session, request.giftCardId());

            LocalDate chargeDate = request.chargeDate() != null ? request.chargeDate() : LocalDate.now();
            giftCardSystemFacade.chargeGiftCard(
                session,
                request.giftCardId(),
                request.merchant(),
                request.amount(),
                chargeDate
            );

            GiftCard chargedCard = session.findGiftCard(request.giftCardId());
            PaymentResponse response = new PaymentResponse(
                UUID.randomUUID().toString(),
                request.giftCardId(),
                request.amount(),
                chargedCard.getBalance(),
                "Cargo realizado para %s".formatted(request.merchant())
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException error) {
            return ResponseEntity.status(resolveStatus(error))
                .body(Map.of("error", resolveMessage(error)));
        }
    }

    private static void validate(PaymentRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        if (request.giftCardId() == null || request.giftCardId().isBlank()) {
            throw new IllegalArgumentException("giftCardId is required");
        }
        if (request.merchant() == null || request.merchant().isBlank()) {
            throw new IllegalArgumentException("merchant is required");
        }
        if (request.amount() == null || request.amount() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }

    private static HttpStatus resolveStatus(RuntimeException error) {
        String message = resolveMessage(error);
        if (UserSession.invalidCredentialsErrorDescription.equals(message)
            || GiftCardSystemFacade.invalidSessionError.equals(message)) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (GiftCardSystemFacade.invalidGiftCardError.equals(message)) {
            return HttpStatus.NOT_FOUND;
        }
        if (GiftCard.insufficientBalance.equals(message) || message.endsWith("required") || message.contains("greater than zero")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static String resolveMessage(RuntimeException error) {
        if (error.getMessage() == null || error.getMessage().isBlank()) {
            return "Unexpected error";
        }
        return error.getMessage();
    }

    public record PaymentRequest(
        String username,
        String password,
        String giftCardId,
        Float amount,
        String merchant,
        LocalDate chargeDate
    ) {}

    public record PaymentResponse(
        String transactionId,
        String giftCardId,
        float amount,
        float remainingBalance,
        String message
    ) {}
}
