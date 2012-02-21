package com.mercapps.botd.serv.sql;

import com.mysql.jdbc.NonRegisteringDriver;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Copyright © 2012 mercapps.com
 */
public class SQLConnector {

    static String dbms = "mysql";

    static String userName;
    static String password;
    private static String dbName;

    static String urlString;

    static String serverName;

    static int portNumber = 3306;

    public static String getDbName() {
        return dbName;
    }

    public static Connection getConnection() throws SQLException {
        SetCreds();
        Connection conn;
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);

        String currentUrlString;

        currentUrlString = "jdbc:" + dbms + "://" + serverName +
                ":" + portNumber + "/";
        NonRegisteringDriver non = new NonRegisteringDriver();
        conn = non.connect(currentUrlString, connectionProps);

        urlString = currentUrlString + dbName;
        conn.setCatalog(dbName);

        System.out.println("Connected to database");
        return conn;
    }

    static void SetCreds() {
        Properties creds = new Properties();

        try {
            creds.load(new FileInputStream("credentials.properties"));

            userName = creds.getProperty("username");
            password = creds.getProperty("password");
            dbName = creds.getProperty("dbname");

            serverName = creds.getProperty("servername");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
