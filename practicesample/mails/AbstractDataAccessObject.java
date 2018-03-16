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

import com.gaian.interactiveservices.db.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Lovababu Golthi (GSI-155)
 *
 * Implemented On: 12 Dec, 2013
 */
public class AbstractDataAccessObject implements AutoCloseable {

    protected Connection con;

    protected Connection getConnection() throws SQLException {
        if (con == null || con.isClosed())
            con = new DBConnection().getConnectionObj();
        return con;
    }

    protected boolean isOpened(Statement anyStmt) throws SQLException {
        return anyStmt != null && !anyStmt.isClosed();
    }

    @Override
    public void close() throws SQLException {
        if (con != null)
            con.close();
    }
}
