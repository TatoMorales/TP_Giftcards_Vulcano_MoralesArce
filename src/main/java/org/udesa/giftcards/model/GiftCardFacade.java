package org.udesa.giftcards.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service // <--- Importante para que el Controller lo encuentre
@Transactional
public class GiftCardFacade {

    @Autowired private UserService userService;
    @Autowired private GiftCardService giftCardService;
    @Autowired private Clock clock;

    // Mantenemos sesiones en memoria (esto es aceptable para este TP usualmente)
    // o podrías crear una entidad Session y un SessionRepository si te piden persistencia total.
    private Map<UUID, UserSession> sessions = new HashMap<>();

    @Transactional(readOnly = true)
    public UUID login(String userKey, String pass) {
        // 1. Usamos UserService para buscar en DB
        UserVault user = userService.findByName(userKey);

        // 2. Validamos password
        if (!user.getPassword().equals(pass)) {
            throw new RuntimeException("InvalidUser");
        }

        // 3. Creamos sesión
        UUID token = UUID.randomUUID();
        sessions.put(token, new UserSession(userKey, clock));
        return token;
    }

    public void redeem(UUID token, String cardId) {
        String username = findUser(token);
        // Buscamos la tarjeta en DB usando el servicio
        GiftCard card = giftCardService.findByCardId(cardId);

        // Operamos sobre el dominio
        card.redeem(username);
        // Al ser @Transactional, el cambio se guarda solo al salir del método
    }

    @Transactional(readOnly = true)
    public int balance(UUID token, String cardId) {
        GiftCard card = giftCardService.findByCardId(cardId);
        validateOwnership(token, card);
        return card.getBalance();
    }

    public void charge(String merchant, String cardId, int amount, String description) {
        // Aquí podrías validar el Merchant si tuvieras un MerchantService
        GiftCard card = giftCardService.findByCardId(cardId);
        card.charge(amount, description);
    }

    @Transactional(readOnly = true)
    public List<String> details(UUID token, String cardId) {
        GiftCard card = giftCardService.findByCardId(cardId);
        validateOwnership(token, card);
        return card.getCharges();
    }

    private void validateOwnership(UUID token, GiftCard card) {
        if (!card.isOwnedBy(findUser(token))) {
            throw new RuntimeException("InvalidToken");
        }
    }

    private String findUser(UUID token) {
        UserSession session = sessions.get(token);
        if (session == null) throw new RuntimeException("InvalidToken");
        return session.userAliveAt(clock);
    }
}
