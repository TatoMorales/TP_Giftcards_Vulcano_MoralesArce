package org.udesa.giftcards.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udesa.giftcards.model.GiftCardFacade;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/giftcards") // la ruta para los http requests
public class GiftCardController {

    @Autowired private GiftCardFacade giftCardFacade;
    // NOTA: Si decidiste no usar Facade y llamar directo a los servicios,
    // inyecta aquí UserService y GiftCardService.

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String user, @RequestParam String pass) {
        // Delega la lógica al modelo/facade
        UUID token = giftCardFacade.login(user, pass);

        // Retorna el JSON esperado por el test
        return ResponseEntity.ok(Map.of("token", token.toString()));
    }

    @PostMapping("/{cardId}/redeem")
    public ResponseEntity<String> redeemCard(@RequestHeader("Authorization") String header, @PathVariable String cardId) {
        UUID token = extractToken(header);
        giftCardFacade.redeem(token, cardId);
        return ResponseEntity.ok("Card redeemed");
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<Map<String, Object>> balance(@RequestHeader("Authorization") String header, @PathVariable String cardId) {
        UUID token = extractToken(header);
        int balance = giftCardFacade.balance(token, cardId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @GetMapping("/{cardId}/details")
    public ResponseEntity<Map<String, Object>> details(@RequestHeader("Authorization") String header, @PathVariable String cardId) {
        UUID token = extractToken(header);
        List<String> movements = giftCardFacade.details(token, cardId);
        return ResponseEntity.ok(Map.of("movements", movements));
    }

    @PostMapping("/{cardId}/charge")
    public ResponseEntity<String> charge(
            @RequestParam String merchant,
            @RequestParam int amount,
            @RequestParam String description,
            @PathVariable String cardId) {

        giftCardFacade.charge(merchant, cardId, amount, description);
        return ResponseEntity.ok("Charge applied");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeExceptions(RuntimeException e) {
        // Convierte las RuntimeException en un Status 500 con el mensaje de error.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    // Método auxiliar para limpiar el "Bearer " del token
    private UUID extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("InvalidToken");
        }
        String tokenString = header.substring(7);
        return UUID.fromString(tokenString);
    }
}