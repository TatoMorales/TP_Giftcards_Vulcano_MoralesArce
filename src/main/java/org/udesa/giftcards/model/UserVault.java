package org.udesa.giftcards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
public class UserVault extends ModelEntity {
    @Column(unique = true) private String name;
    @Column private String password;

    public UserVault() {}

    public UserVault(String aName, String pass) {
        name = aName;
        password = pass;
    }
}
