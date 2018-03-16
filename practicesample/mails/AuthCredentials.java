/*
 * GaianMail 3.0 11 Dec, 2013
 * Copyright 2008, by Gaian Solutions. Gaian Consulting Services Inc.400 N.
 * Continental Blvd. Suite 330,El Segundo,CA 90245, USA All rights reserved.
 * This software is the confidential and proprietary information ofGaian
 * Consulting Services Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Gaian.
 */
package com.gaian.interactiveservices.mails;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Lovababu Golthi (GSI-155)
 *
 * Implemented On: 11 Dec, 2013
 */
public class AuthCredentials implements Serializable {

    private String username;
    private String password;
    private boolean loginFailed;

    public AuthCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean isLoginFailed() {
        return loginFailed;
    }

    public void setLoginFailed(boolean loginFailed) {
        this.loginFailed = loginFailed;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "AuthCredentials{" + "username=" + username + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.username);
        hash = 97 * hash + Objects.hashCode(this.password);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AuthCredentials other = (AuthCredentials) obj;
        if (!Objects.equals(this.username, other.username))
            return false;
        if (!Objects.equals(this.password, other.password))
            return false;
        return true;
    }
}
