package com.tinyhydra.botd;

import com.tinyhydra.botd.sql.SQLConnector;
import com.tinyhydra.botd.sql.SQLInterface;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.sql.SQLException;
import java.util.Date;

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
