package com.tinyhydra.botd;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

/**
 * Copyright © 2012 tinyhydra.com
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

