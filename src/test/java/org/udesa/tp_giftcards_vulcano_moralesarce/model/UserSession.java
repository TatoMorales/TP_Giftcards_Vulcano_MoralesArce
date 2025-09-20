package org.udesa.tp_giftcards_vulcano_moralesarce.model;

import java.time.LocalDateTime;
import java.util.Map;

public class UserSession {
    private final String username;
    private final LocalDateTime creationTime;
    private final LocalDateTime expirationTime;

    public static final String invalidCredentialsErrorDescription = "Invalid username or password";

    public UserSession(String username, String password, Map<String, String> userDatabase) {
        checkValidUser(username, password, userDatabase);
        this.username = username;
        this.creationTime = LocalDateTime.now();
        this.expirationTime = creationTime.plusMinutes(5);
    }

    private static void checkValidUser(String username, String password, Map<String, String> userDatabase) {
        if (!password.equals(userDatabase.get(username))) {
            throw new RuntimeException(invalidCredentialsErrorDescription);
        }
    }

    public String getUsername() { return username; }
    public LocalDateTime getCreationTime() { return creationTime; }
    public LocalDateTime getExpirationTime() { return expirationTime; }

    public boolean isValidAt(LocalDateTime moment) {
        return !moment.isAfter(expirationTime);
    }
}

