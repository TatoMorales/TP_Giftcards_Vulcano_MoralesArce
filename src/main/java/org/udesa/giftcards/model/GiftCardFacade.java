package org.udesa.giftcards.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class GiftCardFacade {
    @Autowired private UserService userService;
    @Autowired private GiftCardService giftCardService;
    @Autowired private MerchantService merchantService;
    @Autowired private Clock clock;

    // Estoy manteniendo las sesiones en memoria - PREGUNTAR A EMILIO
    private Map<UUID, UserSession> sessions = new HashMap<>();

    @Transactional(readOnly = true)
    public UUID login(String userKey, String pass) {
        UserVault user = userService.findByName(userKey);
        // validar la password
        if (!user.getPassword().equals(pass)) {
            throw new RuntimeException("InvalidUser");
        }
        // Creo una session
        UUID token = UUID.randomUUID();
        sessions.put(token, new UserSession(userKey, clock));
        return token;
    }

    public void redeem(UUID token, String cardId) {
        String username = findUser(token);
        giftCardService.findByCardId(cardId).redeem(username);
        // Por ser @Transactional, el cambio se guarda solo al salir del metodo
    }

    @Transactional(readOnly = true)
    public int balance(UUID token, String cardId) {
        GiftCard card = giftCardService.findByCardId(cardId);
        validateOwnership(token, card);
        return card.getBalance();
    }

    public void charge(String merchant, String cardId, int amount, String description) {
        if (!merchantService.exists(merchant)) {throw new RuntimeException("UnknownMerchant");}
        merchantService.findByName(merchant);
        giftCardService.findByCardId(cardId).charge(amount, description);
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
