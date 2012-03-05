package com.tinyhydra.botd;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

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
public class ServerResource extends Application {

    // this is a debug method. starts a local server localhost:8182/1
    // add /vote to get at the get/put services in VoteServerResource
    public static void main(String[] args) throws Exception {
        Component c = new Component();
        c.getServers().add(Protocol.HTTP, 8182);

        c.getDefaultHost().attach("/1", new ServerResource());
        c.start();

        // c.stop();
    }

    // basic restlet REST server root binding. Intercepts inbound calls at (serverroot)/vote
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        router.attachDefault(new Directory(getContext(), "war:///"));
        router.attach("/vote", VoteServerResource.class);
        router.attach("/date", DateServerResource.class);

        return router;
    }
}

