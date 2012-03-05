package com.tinyhydra.botd.sql;

import com.mysql.jdbc.NonRegisteringDriver;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Brew of the day
 * Copyright (C) 2012  tinyhydra.com
 * *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class SQLConnector {

    private static String userName;
    private static String password;
    private static String dbName;

    private static String serverName;

    public String getDbName() {
        return dbName;
    }

    public Connection getConnection() throws SQLException {
        String dbms = "mysql";
        int portNumber = 3306;
        String currentUrlString;

        SetCreds();
        Connection conn;
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);

        currentUrlString = "jdbc:" + dbms + "://" + serverName +
                ":" + portNumber + "/";
        NonRegisteringDriver non = new NonRegisteringDriver();
        conn = non.connect(currentUrlString, connectionProps);

        conn.setCatalog(dbName);

        System.out.println("Connected to database");
        return conn;
    }

    private void SetCreds() {
        Properties creds = new Properties();

        try {

            InputStream inputStream = this.getClass().getClassLoader()
                    .getResourceAsStream("conf/credentials.properties");
            creds.load(inputStream);

            userName = creds.getProperty("username");
            password = creds.getProperty("password");
            dbName = creds.getProperty("dbname");

            serverName = creds.getProperty("servername");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
