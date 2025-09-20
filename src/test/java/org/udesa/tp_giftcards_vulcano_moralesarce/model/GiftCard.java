package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
//import lombok.Getter;
public class GiftCard {
    public static final String insufficientBalance = "Amount greater than balance";
    private final String id;
    private float balance;
    private final List<Charge> charges;

    public GiftCard(String id, float balance, List<Charge> charges) {
        this.id = id;
        this.balance = balance;
        this.charges = charges;
    }
    public String getId() {return this.id;}
    public float getBalance(){ return this.balance; }
    public boolean hasNoCharges(){ return this.charges.isEmpty(); }

    public GiftCard charge(String merchantId, float amount, LocalDate date){
        if (amount > balance)
            throw new RuntimeException(insufficientBalance);
        this.balance -= amount;
        this.charges.add(new Charge(merchantId, amount, date));
        return this;
    }
    public List<Charge> getCharges(){ return charges; }
}
