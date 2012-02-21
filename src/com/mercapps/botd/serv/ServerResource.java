package com.mercapps.botd.serv;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

/**
 * Copyright © 2012 mercapps.com
 */
public class ServerResource extends Application {

    public static void main(String[] args) throws Exception {
        Component c = new Component();
        c.getServers().add(Protocol.HTTP, 8182);

        c.getDefaultHost().attach("/1", new ServerResource());
        c.start();

        // c.stop();
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        router.attachDefault(new Directory(getContext(), "war:///"));
        router.attach("/vote", VoteServerResource.class);

        return router;
    }
}
