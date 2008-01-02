package org.codehaus.jremoting.server.authenticators;

import org.codehaus.jremoting.authentications.Authentication;
import org.codehaus.jremoting.authentications.NamePasswordAuthentication;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.AuthenticationChallenge;

public class SinglePasswordAuthenticator implements Authenticator {

    private final String userId;
    private final String password;

    public SinglePasswordAuthenticator(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public boolean checkAuthority(Authentication auth, String publishedService) {
        if (auth instanceof NamePasswordAuthentication) {
            NamePasswordAuthentication npAuthentication = (NamePasswordAuthentication) auth;
            return npAuthentication.getPassword().equals(password) && npAuthentication.getUserID().equals(userId);
        }
        return false;
    }

    public AuthenticationChallenge getAuthenticationChallenge() {
        return null;
    }
}
