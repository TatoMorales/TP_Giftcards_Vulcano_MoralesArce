package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import java.time.LocalDate;

public class Charge {
    private final String merchant;
    private final float amount;
    private final LocalDate date;

    public Charge(String merchantId, float amount, LocalDate date) {
        this.merchant = merchantId;
        this.amount = amount;
        this.date = date;
    }

    public String getMerchant() { return merchant; }
    public float getAmount() {  return amount; }
    public LocalDate getDate() { return date; }
}
