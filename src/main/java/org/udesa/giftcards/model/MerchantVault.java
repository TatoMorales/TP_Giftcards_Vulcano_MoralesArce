package org.udesa.giftcards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
public class MerchantVault extends ModelEntity {

    @Column(unique = true)
    private String name;

    public MerchantVault() {}

    public MerchantVault(String name) {
        this.name = name;
    }
}
