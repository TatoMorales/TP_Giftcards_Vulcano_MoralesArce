package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class GiftCardSystemFacade {
    private final Map<String, String> validUsers;
    private final Map<String, GiftCard> validGiftCards;

    public static final String invalidSessionError = "Session expired or invalid";
    public static final String invalidGiftCardError = "GiftCard not found";

    public GiftCardSystemFacade(Map<String, String> validUsers, Map<String, GiftCard> validGiftCards) {
        this.validUsers = validUsers;
        this.validGiftCards = new HashMap<>(validGiftCards);
    }

    public UserSession createSessionFor(String user, String password) {
        return new UserSession(user, password, validUsers);
    }

    public void addGiftCard(GiftCard giftCard) {
        validGiftCards.put(giftCard.getId(), giftCard);
    }

    public void claimGiftCard(UserSession session, String giftCardId) {
        checkValidSession(session);
        GiftCard giftCard = validGiftCards.get(giftCardId);
        if (giftCard == null) throw new RuntimeException(invalidGiftCardError);
        session.claimGiftCard(giftCard);
    }

    public void chargeGiftCard(UserSession session, String giftCardId, String merchant, float amount, LocalDate date) {
        checkValidSession(session);
        GiftCard giftCard = session.findGiftCard(giftCardId);
        giftCard.charge(merchant, amount, date);
    }

    private void checkValidSession(UserSession session) {
        if (!session.isValidAt(java.time.LocalDateTime.now())) {
            throw new RuntimeException(invalidSessionError);
        }
    }
}
