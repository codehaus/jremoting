package org.codehaus.jremoting.server.authenticators;

import org.codehaus.jremoting.Authentication;
import org.codehaus.jremoting.NamePasswordAuthentication;
import org.codehaus.jremoting.server.Authenticator;

public class SinglePasswordAuthenticator implements Authenticator {

    private String password;

    public SinglePasswordAuthenticator(String password) {
        this.password = password;
    }

    public boolean checkAuthority(Authentication auth, String publishedService) {
        if (auth instanceof NamePasswordAuthentication) {
            NamePasswordAuthentication npAuthentication = (NamePasswordAuthentication) auth;
            return npAuthentication.getPassword().equals(password);
        }
        return false;
    }

    public String getTextToSign() {
        return null;
    }
}
