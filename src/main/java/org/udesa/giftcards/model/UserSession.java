package org.udesa.giftcards.model;

import java.time.LocalDateTime;

public class UserSession {
    public static final String ExpiredSessionException = "La sesión ya expiró";
    String user;
    LocalDateTime stamp;

    public UserSession( String user, Clock clock ) {
        this.user = user;
        this.stamp = clock.now();
    }

    public String userAliveAt( Clock clock ) {
        if (clock.now().isAfter( stamp.plusMinutes( 15 ) )) throw new RuntimeException(ExpiredSessionException);
        return user;
    }
}
