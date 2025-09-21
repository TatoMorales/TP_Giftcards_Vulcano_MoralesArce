package org.udesa.tp_giftcards_vulcano_moralesarce.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.udesa.tp_giftcards_vulcano_moralesarce.model.GiftCard;
import org.udesa.tp_giftcards_vulcano_moralesarce.model.GiftCardSystemFacade;

@Configuration
public class MerchantConfiguration {

    @Bean
    public GiftCardSystemFacade giftCardSystemFacade() {
        Map<String, String> users = new HashMap<>();
        users.put("owner", "secret");
        users.put("alice", "wonderland");

        Map<String, GiftCard> giftCards = new HashMap<>();
        giftCards.put("GC-100", new GiftCard("GC-100", 5000f, new ArrayList<>()));
        giftCards.put("GC-200", new GiftCard("GC-200", 2000f, new ArrayList<>()));

        // Preload sample card state so consumers can observe historic charges.
        GiftCard preloaded = new GiftCard("GC-300", 3000f, new ArrayList<>());
        preloaded.charge("initial-setup", 200f, LocalDate.now().minusDays(3));
        giftCards.put(preloaded.getId(), preloaded);

        return new GiftCardSystemFacade(users, giftCards);
    }
}
