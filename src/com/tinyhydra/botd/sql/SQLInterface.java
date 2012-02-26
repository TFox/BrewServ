package com.tinyhydra.botd.sql;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copyright © 2012 tinyhydra.com
 */
public class SQLInterface {

    private Connection con;

    public SQLInterface(Connection con) {
        this.con = con;
    }

    // Tables
    private final String dbName = new SQLConnector().getDbName();
    private final String shopsTable = dbName + ".shops";
    private final String votesTable = dbName + ".votes";

    // Shops fields
    private final String shopsIdField = "shop_id";
    private final String shopsReferenceField = "shop_reference";

    // Votes fields
    private final String votesUsernameField = "votes_username";
    private final String votesDateField = "votes_date";
    private final String votesShopVoteField = "votes_shop_vote";
    private final String votesPointsField = "votes_vote_points";

    // QUERY STRINGS
    private final String qSELECT = " SELECT ";
    private final String qINSERT = " INSERT ";
    private final String qUPDATE = " UPDATE ";
    private final String qALL = " * ";
    private final String qDISTINCT = " DISTINCT ";
    private final String qDUPLICATE = " DUPLICATE ";
    private final String qKEY = " KEY ";
    private final String qINTO = " INTO ";
    private final String qFROM = " FROM ";
    private final String qJOIN = " JOIN ";
    private final String qGROUP = " GROUP ";
    private final String qORDER = " ORDER ";
    private final String qBY = " BY ";
    private final String qDESC = " DESC ";
    private final String qLIMIT = " LIMIT ";
    private final String qSET = " SET ";
    private final String qON = " ON ";
    private final String qWHERE = " WHERE ";
    private final String qAND = " AND ";
    private final String qEQUALS = " = ";
    private final String qEQUALS_VALUE = " = ?";

    // QUERY TEMPLATE

    private Object QueryMethodTemplate() {
        String table = "";
        String field = "";
        String value = "";

        Object returnObject = null;

        PreparedStatement getObjectStmt = null;
        String getObjectQuery = qSELECT + qALL + qFROM + table + qWHERE + field + qEQUALS_VALUE;

        ResultSet rs = null;
        try {
            if (con == null || con.isClosed())
                con = new SQLConnector().getConnection();

            getObjectStmt = con.prepareStatement(getObjectQuery);
            getObjectStmt.setString(1, value);

            rs = getObjectStmt.executeQuery();
            while (rs.next()) {
                returnObject = new Object();
            }

        } catch (SQLException sqe) {
            System.out.println(sqe);
        } finally {
            closeStatement(getObjectStmt);
        }
        if (rs == null)
            return null;

        return returnObject;
    }

    // this is 'get top 5' now. returns top 5 ranking shops for the day in json format
    //TODO: return count for display in the app
    public JSONObject GetBrewOfTheDay() {

        JSONObject brewRankings = new JSONObject();

        PreparedStatement getBrewOfTheDayStmt = null;
        String getBrewOfTheDayQuery = qSELECT
                + shopsTable + "." + shopsReferenceField + ","
                + "COUNT(*) AS cnt"
                + qFROM + votesTable
                + qJOIN + shopsTable
                + qON + votesTable + "." + votesShopVoteField
                + qEQUALS + shopsTable + "." + shopsIdField
                + qWHERE + votesTable + "." + votesDateField + qEQUALS_VALUE

                + qGROUP + qBY + votesTable + "." + votesShopVoteField
                + qORDER + qBY + "cnt"
                + qDESC + qLIMIT + "5";

        ResultSet rs = null;
        try {
            if (con == null || con.isClosed())
                con = new SQLConnector().getConnection();

            getBrewOfTheDayStmt = con.prepareStatement(getBrewOfTheDayQuery);

            getBrewOfTheDayStmt.setLong(1, GetDate());

            rs = getBrewOfTheDayStmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                try {
                    brewRankings.put("" + rank, rs.getString(shopsReferenceField));
                } catch (JSONException jex) {
                    System.out.println(jex);
                }
                System.out.println(rs.getString(shopsReferenceField));
                rank++;
            }

        } catch (SQLException sqe) {
            System.out.println(sqe);
        } finally {
            closeStatement(getBrewOfTheDayStmt);
        }
        if (rs == null)
            return null;

        return brewRankings;
    }

    // accepts a user's vote and stores it in the DB. Checks for an existing vote and returns '1' if this is the case
    //TODO: allow user to update their vote, and return some other int code for that.
    public boolean SubmitVote(String username, String shop_id, int points, String shop_reference) {
        boolean addSuccess = false;

        boolean addShopSuccess = AddShop(shop_id, shop_reference);
        if (!addShopSuccess)
            return false;

        PreparedStatement insertVoteStmt = null;

        String insertVoteQuery = qINSERT + qINTO
                + votesTable + " ("
                + votesUsernameField + ","
                + votesDateField + ","
                + votesShopVoteField + ","
                + votesPointsField
                + ") VALUES(?,?,?,?)";
        //+ qON + qDUPLICATE + qKEY + qUPDATE + votesShopVoteField + qEQUALS_VALUE;

        try {
            if (con == null || con.isClosed())
                con = new SQLConnector().getConnection();
            insertVoteStmt = con.prepareStatement(insertVoteQuery);

            insertVoteStmt.setString(1, username);
            insertVoteStmt.setLong(2, GetDate());
            insertVoteStmt.setString(3, shop_id);
            insertVoteStmt.setInt(4, points);

            insertVoteStmt.execute();

            addSuccess = true;

        } catch (SQLException sqe) {
            addSuccess = false;
            System.out.println(sqe);
        } finally {
            closeStatement(insertVoteStmt);
        }

        return addSuccess;
    }

    // when a vote is cast, an id and ref code are included. If they don't exist in the
    // shops table, add them, otherwise do nothing.
    private boolean AddShop(String id, String reference) {
        boolean addSuccess = false;

        PreparedStatement insertShopStmt = null;

        String insertUserQuery = qINSERT + qINTO
                + shopsTable + " ("
                + shopsIdField + ","
                + shopsReferenceField
                + ") VALUES(?,?)"
                + qON + qDUPLICATE + qKEY + qUPDATE + shopsReferenceField + qEQUALS + shopsReferenceField;

        try {
            if (con == null || con.isClosed())
                con = new SQLConnector().getConnection();
            insertShopStmt = con.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS);

            insertShopStmt.setString(1, id);
            insertShopStmt.setString(2, reference);

            insertShopStmt.execute();

            addSuccess = true;

        } catch (SQLException sqe) {
            addSuccess = false;
            System.out.println(sqe);
        } finally {
            closeStatement(insertShopStmt);
        }

        return addSuccess;
    }

    // returns today's date with no time data. we'll use this to count votes for the day
    public long GetDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    private void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();

            } catch (SQLException sqe) {
                System.out.println("Unable to close connection: " + sqe);
            }
        }
    }

}