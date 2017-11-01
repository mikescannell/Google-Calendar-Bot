package com.example.testbot.utils;



import com.example.testbot.SymphonyTestConfiguration;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.io.File;
import com.google.api.services.calendar.Calendar;


public class GoogleCal {

    private final Logger LOG = LoggerFactory.getLogger(GoogleCal.class);
    /** Application name. */
    private static final String APPLICATION_NAME =
            "Google Calendar API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/calendar-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR);

     private com.google.api.services.calendar.Calendar calendarService;

     private SymphonyTestConfiguration config;


    public GoogleCal(SymphonyTestConfiguration config) {

        GoogleCredential credentials = null;
        try {
            credentials = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(new GsonFactory())
                    .setServiceAccountId("googlebot@gbot-182720.iam.gserviceaccount.com")
                    .setServiceAccountScopes(SCOPES)
                    .setServiceAccountPrivateKeyFromP12File(new File(config.getServiceAccount()))
                    .build();

            calendarService = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory(), credentials).setApplicationName(APPLICATION_NAME).build();

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
//    private Credential authorize() throws IOException {
//        // Load client secrets.
//        File initialFile = new File(this.config.getGoogleAppSecretPath());
//        InputStream in = new FileInputStream(initialFile);
//        GoogleClientSecrets clientSecrets =
//                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow =
//                new GoogleAuthorizationCodeFlow.Builder(
//                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                        .setDataStoreFactory(DATA_STORE_FACTORY)
//                        .setAccessType("offline")
//                        .build();
//        Credential credential = new AuthorizationCodeInstalledApp(
//                flow, new LocalServerReceiver()).authorize("user");
//        System.out.println(
//                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
//        return credential;
//    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
//    public com.google.api.services.calendar.Calendar
//    getCalendarService() throws IOException {
//        Credential credential = authorize();
//        return new com.google.api.services.calendar.Calendar.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, credential)
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }

    public String getCalEvents() throws IOException {


        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = calendarService.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.size() == 0) {
            return "No upcoming events found.";
        } else {
            String upcomingevents;
            upcomingevents = "Calendar Upcoming Events: <br/><ul>";
            int currentItem = 0;
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                java.util.Date date = new java.util.Date(start.getValue());
                String summary = event.getSummary().replace("/","-").replace("&","&amp;").replace("<"," &lt;").replace(">"," &gt;");
                String formattedDate = padZero(date.getMonth())+"-"+padZero(date.getDate())+"-"+(date.getYear()-100)+" @ "+padZero(date.getHours())+":"+padZero(date.getMinutes());
                upcomingevents = upcomingevents + "<li>" + currentItem + ": " + String.format("%s (%s)\n", summary, formattedDate)
                        + "<a href=\""+ event.getHtmlLink().toString() +"\">Link</a></li>";
                currentItem++;
            }
            return upcomingevents + "</ul>";
        }
    }

    public String createCalEvent(Event event) throws IOException {


        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        String calendarId = "primary";
        event = calendarService.events().insert(calendarId, event).execute();
        return String.format("Event created: %s\n", event.getHtmlLink());
    }

    public String removeCalEvent(int eventNum) throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = calendarService.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        if(events == null) {
            return "Check your upcoming events. Looks like there are none.";
        } else {
            Event event = events.getItems().get(eventNum);
            calendarService.events().delete("primary", event.getId()).execute();

            return "Your event has been removed! More YOU time!";
        }
    }

    public String padZero(int number){
        return (Integer.toString(number).length()==1?"0"+Integer.toString(number): Integer.toString(number));
    }

}
