package com.example.testbot.resources;

import com.example.testbot.SymphonyTestConfiguration;
import com.example.testbot.bots.GSuiteBot;
import com.example.testbot.core.SymphonyTest;
import com.example.testbot.utils.GoogleCal;
import com.example.testbot.utils.SymphonyAuth;
import com.google.api.services.translate.Translate;
import com.neovisionaries.i18n.LanguageAlpha3Code;
import io.dropwizard.hibernate.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.events.*;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.services.RoomEventListener;
import org.symphonyoss.symphony.agent.model.Datafeed;
import org.symphonyoss.symphony.clients.model.ApiVersion;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.Stream;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Created by mike.scannell on 11/11/16.
 */

@Path("/symph-test")
public class SymphonyTestResource{

    private Map<Long, String> userProfiles = new HashMap<>();

    private SymphonyTest testResults;
    private SymphonyTestConfiguration config;
    private SymphonyClient symClient;
    private final Logger LOG = LoggerFactory.getLogger(SymphonyTestResource.class);

    public SymphonyTestResource(SymphonyTestConfiguration config) {
        this.config = config;
    }

    @GET
    @Path("/startTradeBot")
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public String initTradeBot() {
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            GSuiteBot tradeBot = GSuiteBot.getInstance(symClient,config);
            return "GSuiteBot initialized";
        } catch (Exception e) {
            //log exception
            return e.toString();
        }
    }

    @GET
    @Path("/testcal")
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public void testCal() {
        try {
            GoogleCal googleCal = new GoogleCal(config);
            googleCal.getCalEvents();

        } catch (Exception e) {
            //log exception
            System.out.print(e.toString());
        }
    }

    @GET
    @Path("/createdatafeed")
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public String createDatafeed() {
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            Datafeed datafeed = symClient.getDataFeedClient().createDatafeed(ApiVersion.V1);
            return datafeed.getId();

        } catch (Exception e) {
            //log exception
            return e.toString();
        }
    }


    @GET
    @Path("/createdatafeedv4")
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public String createDatafeedv4() {
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            Datafeed datafeed = symClient.getDataFeedClient().createDatafeed(ApiVersion.V4);
            return datafeed.getId();

        } catch (Exception e) {
            //log exception
            return e.toString();
        }
    }

    @GET
    @Path("/testsessionauth")
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public String testSessionAuth() {
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            return symClient.getSymAuth().getSessionToken().toString();

        } catch (Exception e) {
            //log exception
            return e.toString();
        }
    }

    @GET
    @Path("/testkeyauth")
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public String testKeyAuth() {
        try {
            SymphonyClient symClient = new SymphonyAuth().init(config);
            return symClient.getSymAuth().getKeyToken().toString();

        } catch (Exception e) {
            //log exception
            return e.toString();
        }
    }


    public void sendTranslation(String message, Long fromUserID) throws Exception {


        try {
            // See comments on
            //   https://developers.google.com/resources/api-libraries/documentation/translate/v2/java/latest/
            // on options to set
            Translate t = new Translate.Builder(
                    com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport()
                    , com.google.api.client.json.gson.GsonFactory.getDefaultInstance(), null)
                    //Need to update this to your App-Name
                    .setApplicationName("Stackoverflow-Example")
                    .build();

            Set<String> langs = new HashSet<>();
            Map<String, String> translations = new HashMap<>();
            for(String userlangs : userProfiles.values()){
                langs.add(userlangs);
            }
            for(String lang : langs) {
                Translate.Translations.List list = t.new Translations().list(
                        Arrays.asList(message),
                        //Target language
                        lang);
                //Set your API-Key from https://console.developers.google.com/
                list.setKey("AIzaSyA5Fzl1SZRoimZxDFD1ClruwwvCGruBJ-o");
                TranslationsListResponse response = list.execute();
                for(TranslationsResource tr : response.getTranslations()) {
                    translations.put(lang, tr.getTranslatedText());
                }
            }


            for(Long userid : userProfiles.keySet()){
                if( !userid.equals(fromUserID) ) {
                    Chat chat = new Chat();
                    chat.setLocalUser(symClient.getLocalUser());
                    Set<SymUser> remoteUsers = new HashSet<>();
                    remoteUsers.add(symClient.getUsersClient().getUserFromId(userid));
                    chat.setRemoteUsers(remoteUsers);
                    //chat.addListener(this);
                    chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));
                    //A message to send when the BOT comes online.
                    SymMessage aMessage = new SymMessage();
                    aMessage.setMessage(translations.get(userProfiles.get(userid)));
                    symClient.getMessageService().sendMessage(chat, aMessage);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @GET
    @Path("/starttranslationbot")
    @UnitOfWork
    @Produces(MediaType.APPLICATION_JSON)
    public String startResearchBot(@QueryParam("datafeedid") String datafeedId) {

        try {
            symClient = new SymphonyAuth().init(config);

            //A message to send when the BOT comes online.
            SymMessage aMessage = new SymMessage();
            aMessage.setMessage("Hello Scannell, I'm ready to start translating....");


            //Creates a Chat session with that will receive the online message.
            Chat chat = new Chat();
            chat.setLocalUser(symClient.getLocalUser());
            Set<SymUser> remoteUsers = new HashSet<>();
            remoteUsers.add(symClient.getUsersClient().getUserFromEmail("manuela.caicedo@symphony.com"));
            chat.setRemoteUsers(remoteUsers);
            //chat.addListener(this);
            chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));

            //Add the chat to the chat service, in case the "master" continues the conversation.
            symClient.getChatService().addChat(chat);


            //Send a message to the master user.
            symClient.getMessageService().sendMessage(chat, aMessage);




        } catch (Exception e) {
            System.out.print(e.toString());
        }

        return "Translation Bot Started";
    }



}
