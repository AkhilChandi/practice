/*
 * GaianMail 3.0 12 Dec, 2013
 * Copyright 2008, by Gaian Solutions. Gaian Consulting Services Inc.400 N.
 * Continental Blvd. Suite 330,El Segundo,CA 90245, USA All rights reserved.
 * This software is the confidential and proprietary information ofGaian
 * Consulting Services Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Gaian.
 */
package com.gaian.interactiveservices.mails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object to get email-login credentials of an user from the
 * database.
 *
 * @author Lovababu Golthi (GSI-155)
 *
 * Implemented On: 12 Dec, 2013
 */
public class EmailCredentialsDAO extends AbstractDataAccessObject {

    private PreparedStatement creditsPstmt;
    public static final String QUERY_SELECT_EMAIL_CREDITS = "SELECT mtu.record_id AS installID, m.mail_id AS loginID, m.mail_pwd AS loginPassword, m.provider_name AS provider, mtu.show_email_notification AS alert "
            + "FROM mail_ids m "
            + "RIGHT JOIN mail_ids_to_user mtu ON mtu.mid=m.record_id "
            + "WHERE mtu.user_id=? AND m.deleted=0 AND mtu.deleted=0";

    public Map<AuthCredentials, UserPreference> getCreditAndPreferences(String STBSerial, int userID) throws SQLException {
        if (!isOpened(creditsPstmt))
            creditsPstmt = getConnection().prepareStatement(QUERY_SELECT_EMAIL_CREDITS);
        creditsPstmt.setInt(1, userID);
        Map<AuthCredentials, UserPreference> credits;
        try (ResultSet rs = creditsPstmt.executeQuery()) {
            credits = new HashMap<>();
            while (rs.next()) {
                credits.put(new AuthCredentials(rs.getString("loginID"), rs.getString("loginPassword")),
                        new UserPreference(rs.getInt("installID"), -1, userID, STBSerial));
            }
        }
        return credits;
    }

    @Override
    public void close() throws SQLException {
        super.close();
        if (creditsPstmt != null)
            creditsPstmt.close();
    }
}
