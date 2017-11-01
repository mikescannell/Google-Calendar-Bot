package com.example.testbot.resources;

import com.example.testbot.SymphonyTestConfiguration;
import com.example.testbot.bots.GSuiteBot;
import com.example.testbot.utils.SymphonyAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;

import javax.ws.rs.Path;

@Path("/tradeBot")
public class GSuiteBotResource {

    private SymphonyTestConfiguration config;
    private SymphonyClient symClient;
    private final Logger LOG = LoggerFactory.getLogger(GSuiteBotResource.class);
    private GSuiteBot gSuiteBot;

    public GSuiteBotResource(SymphonyTestConfiguration config) {
        this.config = config;
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            gSuiteBot = GSuiteBot.getInstance(symClient,config);

        } catch (Exception e) {
            LOG.error("error", e);
        }
    }



}
