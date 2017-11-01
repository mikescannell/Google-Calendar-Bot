package com.example.testbot.bots;

import com.example.testbot.SymphonyTestConfiguration;
import com.example.testbot.utils.GoogleCal;
import com.example.testbot.utils.GoogleDrive;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.events.*;
import org.symphonyoss.client.exceptions.*;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.*;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.pod.model.Stream;

import java.util.*;
import java.util.Date;


public class GSuiteBot implements ChatListener, ChatServiceListener{

    private static GSuiteBot instance;
    private final Logger logger = LoggerFactory.getLogger(GSuiteBot.class);
    private SymphonyClient symClient;
    //private RoomService roomService;
    private Event event;
    private SymphonyTestConfiguration config;
    private HashMap<String,Boolean> streamIdMap;

    protected GSuiteBot(SymphonyClient symClient, SymphonyTestConfiguration config) {
        this.symClient=symClient;
        this.config = config;
        init();


    }

    public static GSuiteBot getInstance(SymphonyClient symClient, SymphonyTestConfiguration config){
        if(instance==null){
            instance = new GSuiteBot(symClient, config);


        }
        return instance;
    }

    private void init() {

        logger.info("Connections example starting...");
//        try {
            //Will notify the bot of new Chat conversations.
            symClient.getChatService().addListener(this);
            this.streamIdMap = new HashMap<>();
    }


    //Chat sessions callback method.
    @Override
    public void onChatMessage(SymMessage symMessage) {
        if (symMessage == null)
            return;

        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                symMessage.getTimestamp(),
                symMessage.getFromUserId(),
                symMessage.getMessage(),
                symMessage.getMessageType());
        SymMessage message2 = new SymMessage();


       newMessage(symMessage);
    }

    @Override
    public void onNewChat(Chat chat) {

        chat.addListener(this);

        logger.debug("New chat session detected on stream {} with {}", chat.getStream().getStreamId(), chat.getRemoteUsers());
    }

    @Override
    public void onRemovedChat(Chat chat) {

    }

    public void newMessage(SymMessage symMessage){
        GoogleCal googleCal = new GoogleCal(config);
        //GoogleDrive googleDrive = new GoogleDrive(config);
        if (symMessage == null)
            return;

        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                symMessage.getTimestamp(),
                symMessage.getFromUserId(),
                symMessage.getMessage(),
                symMessage.getMessageType());
        SymMessage message2 = new SymMessage();
        if((!streamIdMap.containsKey(symMessage.getStreamId())) || !streamIdMap.get(symMessage.getStreamId()).booleanValue()) {

            if (symMessage.getMessage().contains("#upcomingevents")) {
                try {
                    String upcomingEvents = googleCal.getCalEvents();
                    message2.setMessage("<messageML>" + upcomingEvents + "</messageML>");
                } catch (Exception e) {
                    //log exception
                    System.out.print(e.toString());
                }

            } else if (symMessage.getMessage().contains("#createevent")) {
                try {
                    streamIdMap.put(symMessage.getStreamId(),true);

                    event = new Event();
                    message2.setMessage("<messageML>What is the title of your event?</messageML>");


                } catch (Exception e) {
                    //log exception
                    System.out.print(e.toString());
                }
            } else if (symMessage.getMessage().contains("#removeevent")) {
                try {

                    String message = symMessage.getMessageText();
                    String cleaned = message.replace("&nbsp;"," ");
        // Split path into segments
                    String segments[] = cleaned.split(" ");

                    String info  = googleCal.removeCalEvent(Integer.parseInt(segments[1]));
                    message2.setMessage("<messageML>"+info+"</messageML>");


                } catch (Exception e) {
                    //log exception
                    System.out.print(e.toString());
                }
            }
//            else if (symMessage.getMessage().contains("#checkdrive")) {
//                try {
//                    //String driveContents = googleDrive.getFiles();
//                    message2.setMessage("<messageML>" + "no content now" + "</messageML>");
//                } catch (Exception e) {
//                    //log exception
//                    System.out.print(e.toString());
//                }
//            }
            else {
                message2.setMessage("<messageML>I am here to help. Try one of the following commands: <ul><li><hash tag=\"upcomingevents\"/></li><li><hash tag=\"createevent\"/></li><li><hash tag=\"removeevent\"/></li></ul></messageML>");
            }
        } else {
            if(symMessage.getMessageText().toLowerCase().contains("cancel" )) {
                streamIdMap.put(symMessage.getStreamId(),false);
                event = new Event();
                message2.setMessage("<messageML>Not a Problem. Keep your day free!</messageML>");
            } else {
                if (event.getSummary() == null) {
                    event.setSummary(symMessage.getMessageText().replace("&nbsp;"," "));
                    message2.setMessage("<messageML>Does this event have a location?</messageML>");
                } else if (event.getLocation() == null) {
                    event.setLocation(symMessage.getMessageText().replace("&nbsp;"," "));
                    message2.setMessage("<messageML>Does this event have a Description?</messageML>");
                } else if (event.getDescription() == null) {
                    event.setDescription(symMessage.getMessageText().replace("&nbsp;"," "));
                    message2.setMessage("<messageML>When does your event start?</messageML>");
                } else if (event.getStart() == null) {
                    EventDateTime eventDateTime = new EventDateTime();
                    Parser parser = new Parser();
                    Date dateFromMessage = new Date();
                    List<DateGroup> groups = parser.parse(symMessage.getMessageText().replace("&nbsp;"," "));
                    for (DateGroup group : groups) {
                        List<Date> dates = group.getDates();
                        dateFromMessage = dates.get(0);
                    }


                    DateTime dateTime = new DateTime(dateFromMessage);
                    eventDateTime.setDateTime(DateTime.parseRfc3339(dateTime.toString()));
                    event.setStart(eventDateTime);
                    message2.setMessage("<messageML>What time will your event end?</messageML>");
                } else if (event.getEnd() == null) {
                    EventDateTime eventDateTime = new EventDateTime();
                    Parser parser = new Parser();
                    Date dateFromMessage = new Date();
                    List<DateGroup> groups = parser.parse(symMessage.getMessageText().replace("&nbsp;"," "));
                    for (DateGroup group : groups) {
                        List<Date> dates = group.getDates();
                        dateFromMessage = dates.get(0);
                    }


                    DateTime dateTime = new DateTime(dateFromMessage);
                    eventDateTime.setDateTime(DateTime.parseRfc3339(dateTime.toString()));
                    event.setEnd(eventDateTime);
                    try {
                        googleCal.createCalEvent(event);
                    } catch (Exception e) {
                        //log exception
                        System.out.print(e.toString());
                    }
                    streamIdMap.put(symMessage.getStreamId(),false);
                    message2.setMessage("<messageML>Meeting Setup: Please check your cal</messageML>");
                }
            }

        }


        Stream stream = new Stream();
        stream.setId(symMessage.getStreamId());

        try {
            symClient.getMessagesClient().sendMessage(stream, message2);
        } catch (MessagesException e) {
            logger.error("Failed to send message", e);
        }
    }
}