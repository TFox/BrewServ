package com.tinyhydra.botd;

import com.tinyhydra.botd.sql.SQLConnector;
import com.tinyhydra.botd.sql.SQLInterface;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.sql.SQLException;
import java.util.Date;

public class DateServerResource extends ServerResource {

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

    // maintenance url, checks the date
    @Get
    public String retrieve() throws Exception {
        String date = Long.toString(getSqi().GetDate());
        date += " -- ";
        date += new Date().toString();
        return date;
    }
}
