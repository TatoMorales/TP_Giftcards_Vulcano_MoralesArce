package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GiftCard {
    public static final String insufficientBalance = "Amount greater than balance";
    private final String id;
    private float balance;
    private List<Charge> charges;
    private boolean redeemed;

    public GiftCard(String id, float balance) {
        this.id = id;
        this.balance = balance;
        this.charges = new ArrayList<>();
    }

    public String getId() {return this.id;}
    public float getBalance(){ return this.balance; }
    public boolean hasNoCharges(){ return this.charges.isEmpty(); }
    public boolean isRedeemed(){ return this.redeemed; }

    public GiftCard charge(String merchantId, float amount, LocalDate date){
        if (amount > balance)
            throw new RuntimeException(insufficientBalance);
        this.balance -= amount;
        this.charges.add(new Charge(merchantId, amount, date));
        return this;
    }
    public List<Charge> getCharges(){ return charges; }
}
