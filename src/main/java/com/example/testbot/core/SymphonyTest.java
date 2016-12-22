package com.example.testbot.core;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.symphonyoss.symphony.clients.model.SymMessage;

import java.util.List;

/**
 * Created by mike.scannell on 11/11/16.
 */
public class SymphonyTest {

    private String imCreated;
    private Integer numberOfMessages;
    private SymMessage newMessage;
    private Boolean totalBool;
    private Boolean imCreateBool;
    private Boolean messageRetrievedBool;
    private Boolean messageCreatedBool;

    private String kmToken;
    private String sessionToken;
    private String podUrl;
    private String agentUrl;

    public Boolean getTotalBool() {
        return totalBool;
    }

    public void setTotalBool(Boolean totalBool) {
        this.totalBool = totalBool;
    }

    public Boolean getImCreateBool() {
        return imCreateBool;
    }

    public void setImCreateBool(Boolean imCreateBool) {
        this.imCreateBool = imCreateBool;
    }

    public Boolean getMessageRetrievedBool() {
        return messageRetrievedBool;
    }

    public void setMessageRetrievedBool(Boolean messageRetrievedBool) {
        this.messageRetrievedBool = messageRetrievedBool;
    }

    public Boolean getMessageCreatedBool() {
        return messageCreatedBool;
    }

    public void setMessageCreatedBool(Boolean messageCreatedBool) {
        this.messageCreatedBool = messageCreatedBool;
    }


    public SymMessage getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(SymMessage newMessage) {
        this.newMessage = newMessage;
    }

    public String getImCreated() {
        return imCreated;
    }

    public void setImCreated(String imCreated) {
        this.imCreated = imCreated;
    }

    public Integer getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(Integer numberOfMessages) {
        this.numberOfMessages = numberOfMessages;
    }

    public String getKmToken() {
        return kmToken;
    }

    public void setKmToken(String kmToken) {
        this.kmToken = kmToken;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getPodUrl() {
        return podUrl;
    }

    public void setPodUrl(String podUrl) {
        this.podUrl = podUrl;
    }

    public String getAgentUrl() {
        return agentUrl;
    }

    public void setAgentUrl(String agentUrl) {
        this.agentUrl = agentUrl;
    }



    public void calculateCheck() {
        if(newMessage != null){
            messageCreatedBool = true;
        } else {
            messageCreatedBool = false;
        }

        if(numberOfMessages > 0){
            messageRetrievedBool = true;
        } else {
            messageRetrievedBool = false;
        }

        if(imCreated != null) {
            imCreateBool = true;
        } else {
            imCreateBool = false;
        }

        if(messageRetrievedBool && messageCreatedBool && imCreateBool){
            totalBool = true;
        } else {
            totalBool = false;
        }



    }

}
