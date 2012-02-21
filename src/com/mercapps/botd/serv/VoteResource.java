package com.mercapps.botd.serv;

import org.restlet.resource.Get;
import org.restlet.resource.Put;

public interface VoteResource {

    @Get
    public String retrieve() throws Exception;

    @Put
    public int store(String string);

}
