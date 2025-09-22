package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiftCardSystemFacade {
    private final Map<String, String> validUsers;
    private final Map<String, GiftCard> validGiftCards;
    private final Map<String, UserSession> userSessions;
    private final List<String> merchants;

    public static final String invalidSessionError = "Session expired or invalid";
    public static final String invalidGiftCardError = "GiftCard not found";
    public static final String alreadyRedeemedGiftCardError = "GiftCard already redeemed";
    public static final String unknownMerchantError = "Merchant not found";

    public GiftCardSystemFacade(Map<String, String> validUsers, Map<String, GiftCard> validGiftCards, List<String> merchants) {
        this.validUsers = validUsers;
        this.validGiftCards = new HashMap<>(validGiftCards);
        this.userSessions = new HashMap<String, UserSession>();
        this.merchants = merchants;
    }

    public UserSession login(String username, String password) {
        if (!userSessions.containsKey(username)) userSessions.put(username, createSessionFor(username, password));
        else userSessions.get(username).resetTime();
        return userSessions.get(username);
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
        if (giftCard.isRedeemed()) throw new RuntimeException(alreadyRedeemedGiftCardError);
        session.claimGiftCard(giftCard);
    }

    public void chargeGiftCard(UserSession session, String giftCardId, String merchantId, float amount, LocalDate date) {
        checkValidMerchant(merchantId);
        checkValidSession(session);
        GiftCard giftCard = session.findGiftCard(giftCardId);
        giftCard.charge(merchantId, amount, date);
    }

    private void checkValidMerchant(String merchantId) {
        if (!merchants.contains(merchantId)) throw new RuntimeException(unknownMerchantError);
    }

    private boolean checkValidSession(UserSession session) {
        if (!session.isValidAt(java.time.LocalDateTime.now())) {
            throw new RuntimeException(invalidSessionError);
        }
        else return true;
    }
    public Map<String, UserSession> getExistingSessions(){
        return userSessions;
    }
}
