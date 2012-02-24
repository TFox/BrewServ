package com.mercapps.botd.serv;

import com.mercapps.botd.serv.sql.SQLConnector;
import com.mercapps.botd.serv.sql.SQLInterface;
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

    @Get
    public String retrieve() throws Exception {
        return getSqi().GetBrewOfTheDay().toString();
    }

    @Put
    public int store(String vote) {
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
        return getSqi().SubmitVote(email, shopId, 100, shopRef) ? 0 : 1;
    }
}
