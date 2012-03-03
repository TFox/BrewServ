package com.tinyhydra.botd;

import com.tinyhydra.botd.sql.SQLConnector;
import com.tinyhydra.botd.sql.SQLInterface;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import java.sql.SQLException;

public class VoteServerResource extends ServerResource {

    SQLInterface sqi = null;

    SQLInterface getSqi() {
        if (sqi == null)
            try {
                sqi = new SQLInterface(new SQLConnector().getConnection());
            } catch (SQLException sex) {
                System.out.println(sex);
            }
        return sqi;
    }

    // returns top 5 ranking coffee shops
    @Get
    public String retrieve() throws Exception {
        String returnString = getSqi().GetBrewOfTheDay().toString();
        sqi.con.close();
        return returnString;
    }

    // accepts votes, returns 0 for successful entries
    // or 1 for duplicate/errored entries.
    //TODO: implement a new error code for duplicate entries and handle it appropriately
    @Put
    public int store(String vote) throws SQLException {
        String email = "";
        String shopId = "";
        String shopRef = "";
        try {
            JSONObject jo = new JSONObject(vote);
            email = jo.getString(JSONvalues.email.toString());
            shopId = jo.getString(JSONvalues.shopId.toString());
            shopRef = jo.getString(JSONvalues.shopRef.toString());
        } catch (JSONException jex) {
            System.out.println(jex);
        }
        int success = getSqi().SubmitVote(email, shopId, 100, shopRef) ? 0 : 1;
        sqi.con.close();
        return success;
    }
}
