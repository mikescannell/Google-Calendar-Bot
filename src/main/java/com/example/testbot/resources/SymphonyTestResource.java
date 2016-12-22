package com.example.testbot.resources;

import com.example.testbot.SymphonyTestConfiguration;
import com.example.testbot.core.SymphonyTest;
import com.example.testbot.utils.SymphonyAuth;
import com.example.testbot.views.SymphonyTestView;
import io.dropwizard.hibernate.UnitOfWork;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.symphony.agent.model.Datafeed;
import org.symphonyoss.symphony.agent.model.V2BaseMessage;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.Stream;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by mike.scannell on 11/11/16.
 */

@Path("/symph-test")
public class SymphonyTestResource {

    private SymphonyTest testResults;
    private SymphonyTestConfiguration config;

    public SymphonyTestResource(SymphonyTestConfiguration config) {
        this.config = config;
    }


    @GET
    @Path("/complete")
    @Produces(MediaType.APPLICATION_JSON)
    public SymphonyTest completeTest() {
        testResults = new SymphonyTest();
        Chat chat = createChat();
        if ( chat == null ) {
            testResults.setImCreated(null);
            testResults.setNewMessage(null);
            testResults.setNumberOfMessages(0);
        } else {
            testResults.setImCreated(chat.getStreamId());
            SymMessage sentMessage = sendMessage(chat);
            testResults.setNewMessage(sentMessage);
            List<SymMessage> messages = readMessages(chat);
            testResults.setNumberOfMessages(messages.size());
        }
        testResults.calculateCheck();
        return testResults;
    }

    @GET
    @Path("/view")
    @UnitOfWork
    @Produces(MediaType.TEXT_HTML)
    public SymphonyTestView getSymphonyTestViewMustache() {
        return new SymphonyTestView(SymphonyTestView.Template.MUSTACHE, completeTest());
    }

    private  List<SymMessage>  getMessages(String streamid) {
        List<SymMessage> listMessages = new ArrayList<>();
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            Stream stream = new Stream();
            stream.setId(streamid);
            Date date = addDays(new Date(), -20);
            listMessages = symClient.getMessagesClient().getMessagesFromStream(stream, date.getTime(), 0, 1000);
            return listMessages;

        } catch (Exception e) {
            //log exception
            return listMessages;
        }

    }

    private Chat createChat() {
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            setClientInfo(symClient);
            Chat chat = new Chat();
            chat.setLocalUser(symClient.getLocalUser());
            Set<SymUser> remoteUsers = new HashSet<>();
            remoteUsers.add(symClient.getUsersClient().getUserFromEmail("mike.scannell@symphony.com"));
            remoteUsers.add(symClient.getUsersClient().getUserFromEmail("bot.user1@example.com"));
            chat.setRemoteUsers(remoteUsers);
            symClient.getChatService().addChat(chat);
            return chat;

        } catch (Exception e) {
            //log exception
            return null;
        }
    }

    private SymMessage sendMessage(Chat chat) {
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            SymMessage message = new SymMessage();
            message.setFormat(SymMessage.Format.MESSAGEML);
            message.setMessage("<messageML>This a <hash tag=\"Test\"/> coming from the test bot</messageML>");
            message.setSymUser(symClient.getLocalUser());
            SymMessage sentMessage = symClient.getMessagesClient().sendMessage(chat.getStream(), message);
            return sentMessage;

        } catch (Exception e) {
            //log exception
            return null;
        }
    }

    private List<SymMessage> readMessages(Chat chat) {
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            Date date = addDays(new Date(), -20);
            List<SymMessage> listMessages = symClient.getMessagesClient().getMessagesFromStream(chat.getStream(), date.getTime(), 0, 1000);
            return listMessages;

        } catch (Exception e) {
            //log exception
            return null;
        }
    }

    private void setClientInfo(SymphonyClient symClient){
        testResults.setSessionToken(symClient.getSymAuth().getSessionToken().getToken());
        testResults.setKmToken(symClient.getSymAuth().getKeyToken().getToken());
        testResults.setPodUrl(symClient.getServiceUrl().toString());
        testResults.setAgentUrl(symClient.getAgentUrl().toString());
    }


    private Date addDays(Date d, int days)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, days);
        d.setTime(c.getTime().getTime());
        return d;
    }

    @GET
    @Path("/datafeed")
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public List<V2BaseMessage> testDataFeed() {
        List<V2BaseMessage> messages = new ArrayList<>();
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            Datafeed datafeed = new Datafeed();
            datafeed.setId("4d526824-64de-40b6-937f-717395c2ecc8");
            messages = symClient.getDataFeedClient().getMessagesFromDatafeed(datafeed);
            return messages;

        } catch (Exception e) {
            //log exception
            return messages;
        }
    }

}
