package com.tinyhydra.botd.sql;

import com.tinyhydra.botd.JSONvalues;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Copyright © 2012 tinyhydra.com
 */
public class SQLInterface {

    public Connection con;

    public SQLInterface(Connection con) {
        this.con = con;
    }

    // Tables
    private final String dbName = new SQLConnector().getDbName();
    private final String shopsTable = dbName + ".shops";
    private final String votesTable = dbName + ".votes";

    // Shops fields
    private final String shopsIdField = "shop_id";
    private final String shopsNameField = "shop_name";
    private final String shopsUrlField = "shop_url";
    private final String shopsVicinityField = "shop_vicinity";
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

    // this is 'get top 10' now. returns top 10 ranking shops for the day in json format
    //TODO: return count for display in the app
    public JSONArray GetBrewOfTheDay() {
        JSONArray brewRankings = new JSONArray();

        PreparedStatement getBrewOfTheDayStmt = null;
        String getBrewOfTheDayQuery = qSELECT
                + shopsTable + "." + shopsIdField + ","
                + shopsTable + "." + shopsNameField + ","
                + shopsTable + "." + shopsUrlField + ","
                + shopsTable + "." + shopsVicinityField + ","
                + shopsTable + "." + shopsReferenceField + ","
                + "COUNT(*) AS cnt"
                + qFROM + votesTable
                + qJOIN + shopsTable
                + qON + votesTable + "." + votesShopVoteField
                + qEQUALS + shopsTable + "." + shopsIdField
                + qWHERE + votesTable + "." + votesDateField + qEQUALS_VALUE

                + qGROUP + qBY + votesTable + "." + votesShopVoteField
                + qORDER + qBY + "cnt"
                + qDESC + qLIMIT + "10";

        ResultSet rs = null;
        try {
            if (con == null || con.isClosed())
                con = new SQLConnector().getConnection();

            getBrewOfTheDayStmt = con.prepareStatement(getBrewOfTheDayQuery);

            getBrewOfTheDayStmt.setLong(1, GetDate());

            rs = getBrewOfTheDayStmt.executeQuery();
            while (rs.next()) {
                try {
                    JSONObject jo = new JSONObject();
                    jo.put(JSONvalues.shopId.toString(), rs.getString(shopsIdField));
                    jo.put(JSONvalues.shopName.toString(), rs.getString(shopsNameField));
                    jo.put(JSONvalues.shopUrl.toString(), rs.getString(shopsUrlField));
                    jo.put(JSONvalues.shopVicinity.toString(), rs.getString(shopsVicinityField));
                    jo.put(JSONvalues.shopRef.toString(), rs.getString(shopsReferenceField));
                    jo.put(JSONvalues.shopVotes.toString(), rs.getString("cnt"));
                    brewRankings.put(jo);
                } catch (JSONException jex) {
                    System.out.println(jex);
                }
                System.out.println(rs.getString(shopsReferenceField));
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

        GetShopDataAndAdd(shop_reference);

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

    private void GetShopDataAndAdd(final String reference) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Properties creds = new Properties();
                    InputStream inputStream = this.getClass().getClassLoader()
                            .getResourceAsStream("conf/credentials.properties");
                    creds.load(inputStream);

                    URL url;
                    HttpURLConnection conn;
                    BufferedReader rd;
                    String line;
                    String result = "";
                    url = new URL("https://maps.googleapis.com/maps/api/place/details/json?reference=" + reference + "&sensor=true&key=" + creds.getProperty("placesapikey"));
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = rd.readLine()) != null) {
                        result += line;
                    }
                    rd.close();

                    JSONObject results = new JSONObject(result);
                    String jstatus = results.getString("status");
                    if (jstatus.equals("OK")) {
                        JSONObject resultObj = results.getJSONObject("result");
                        AddShop(resultObj.getString("id"), resultObj.getString("name"), resultObj.getString("url"), resultObj.getString("vicinity"), reference);
                    }
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // when a vote is cast, an id and ref code are included. If they don't exist in the
    // shops table, add them, otherwise do nothing.
    private boolean AddShop(String id, String name, String url, String vicinity, String reference) {
        boolean addSuccess = false;

        PreparedStatement insertShopStmt = null;

        String insertUserQuery = qINSERT + qINTO
                + shopsTable + " ("
                + shopsIdField + ","
                + shopsNameField + ","
                + shopsUrlField + ","
                + shopsVicinityField + ","
                + shopsReferenceField
                + ") VALUES(?,?,?,?,?)"
                + qON + qDUPLICATE + qKEY + qUPDATE
                + shopsNameField + qEQUALS_VALUE + ","
                + shopsUrlField + qEQUALS_VALUE + ","
                + shopsVicinityField + qEQUALS_VALUE + ","
                + shopsReferenceField + qEQUALS_VALUE;

        try {
            if (con == null || con.isClosed())
                con = new SQLConnector().getConnection();
            insertShopStmt = con.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS);

            insertShopStmt.setString(1, id);
            insertShopStmt.setString(2, name);
            insertShopStmt.setString(3, url);
            insertShopStmt.setString(4, vicinity);
            insertShopStmt.setString(5, reference);
            insertShopStmt.setString(6, name);
            insertShopStmt.setString(7, url);
            insertShopStmt.setString(8, vicinity);
            insertShopStmt.setString(9, reference);

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
        System.out.println("Returning today's date in GMT-8");
        SimpleDateFormat date_format_gmt = new SimpleDateFormat("yyyy-MM-dd");
        date_format_gmt.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        long returnDate = 0;
        try {
            returnDate = date_format_gmt.parse(date_format_gmt.format(Calendar.getInstance().getTime())).getTime();
        } catch (ParseException pex) {
            pex.printStackTrace();
        }
        return returnDate;
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