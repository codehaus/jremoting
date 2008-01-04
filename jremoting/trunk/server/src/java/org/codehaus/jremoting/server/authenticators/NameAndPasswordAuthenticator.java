package org.codehaus.jremoting.server.authenticators;

import org.codehaus.jremoting.authentication.Authentication;
import org.codehaus.jremoting.authentication.NameAndPasswordAuthentication;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.AuthenticationChallenge;

public class NameAndPasswordAuthenticator implements Authenticator {

    private final String userId;
    private final String password;

    public NameAndPasswordAuthenticator(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public boolean checkAuthority(Authentication auth, String publishedService) {
        if (auth instanceof NameAndPasswordAuthentication) {
            NameAndPasswordAuthentication npAuthentication = (NameAndPasswordAuthentication) auth;
            boolean b = npAuthentication.getPassword().equals(password) && npAuthentication.getName().equals(userId);
            return b;
        }
        return false;
    }

    public AuthenticationChallenge getAuthenticationChallenge() {
        return null;
    }
}
