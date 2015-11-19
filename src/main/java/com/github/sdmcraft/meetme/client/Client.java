package com.github.sdmcraft.meetme.client;

import com.github.sdmcraft.meetme.Conference;
import com.github.sdmcraft.meetme.Context;
import com.github.sdmcraft.meetme.Extension;
import com.github.sdmcraft.meetme.User;
import com.github.sdmcraft.meetme.event.Event;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.TimeoutException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


// TODO: Auto-generated Javadoc

/**
 * The Class Client.
 */
public class Client implements Observer {


    /*
     * (non-Javadoc)
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable dispatcher, Object eventObject) {
        Event event = (Event) eventObject;

        switch (event.getType()) {
            case USER_JOINED:

                User user = (User) event.getData();
                user.addObserver(this);
                System.out.println(user.getUserId() +
                        " joined the audio conference  " +
                        user.getConferenceId());

                break;

            case CONFERENCE_ENDED:
                System.out.println("The audio conference ended");

                break;

            case MUTE:
                user = (User) dispatcher;
                System.out.println(user.getPhoneNumber() + " was muted");

                break;

            case UNMUTE:
                user = (User) dispatcher;
                System.out.println(user.getPhoneNumber() + " was unmuted");

                break;

            case TALKING:
                user = (User) dispatcher;
                System.out.println(user.getPhoneNumber() + " talking");

                break;

            case NOT_TALKING:
                user = (User) dispatcher;
                System.out.println(user.getPhoneNumber() + " not talking");

                break;

            case USER_LEFT:
                user = (User) dispatcher;
                System.out.println(user.getPhoneNumber() +
                        " left the audio conference " + user.getConferenceId());

                break;
        }
    }

    /**
     * Demo.
     *
     * @param ip               the ip
     * @param admin            the admin
     * @param pwd              the pwd
     * @param conferenceNumber the conference number
     * @throws IllegalStateException         the illegal state exception
     * @throws IOException                   Signals that an I/O exception has occurred.
     * @throws AuthenticationFailedException the authentication failed exception
     * @throws TimeoutException              the timeout exception
     * @throws InterruptedException          the interrupted exception
     */
    public void demo(String ip, String admin, String pwd,
                     String conferenceNumber, Extension[] extensions, String extensionUrl)
            throws Exception {
        Context context = Context.getInstance(ip, admin, pwd, extensionUrl);
        Conference conference = Conference.getInstance(conferenceNumber, context);
        conference.addObserver(this);
        Thread.sleep(2 * 1000);

        for (Extension extn : extensions) {
            System.out.println("User Number:" +
                    conference.requestDialOut(extn) + " dialled out");
            //Wait for dial-out to be answered
            Thread.sleep(30000);
        }

        if (conference.getUsers().containsKey(extensions[0].getNumber())) {

            //Mute an extension for 10 seconds
            conference.getUsers().get(extensions[0].getNumber()).requestMuteStateChange();
            Thread.sleep(10000);

            //Unmute the muted extension
            conference.getUsers().get(extensions[0].getNumber()).requestMuteStateChange();

            //Hangup the user after 10 seconds
            Thread.sleep(10000);
            conference.getUsers().get("SIP/6000").requestHangUp();
        }

        //End the conference after 10 seconds. This should hangup all users
        Thread.sleep(10000);
        conference.requestEndConference();

        //Destroy the conference after 10 seconds
        Thread.sleep(10000);
        conference.destroy();

        //Destroy the context after 10 seconds
        Thread.sleep(10000);
        context.destroy();
    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IllegalStateException         the illegal state exception
     * @throws IOException                   Signals that an I/O exception has occurred.
     * @throws AuthenticationFailedException the authentication failed exception
     * @throws TimeoutException              the timeout exception
     * @throws InterruptedException          the interrupted exception
     */
    public static void main(String[] args) throws Exception {
//        new Client().demo("192.168.1.103", "admin", "amp111", "600",
//            new Extension[] {
//                new Extension("LocalSets", "SIP/callcentric/011919971647800",
//                    "SIP/callcentric/011919971647800")
//            }, null);

        new Client().demo("192.168.1.103", "admin", "amp111", "600",
                new Extension[]{new Extension("LocalSets", "SIP/101", "SIP/101")},
                null);

        //		new Client().demo("192.168.1.104", "admin", "amp111", "600", 
        //				new Extension[] { new Extension("from-internal", "SIP/callcentric/011919971647800", "SIP/callcentric/011919971647800") },
        //				"http://10.40.63.202:8080/AsteriskExtension/service");

        //		new Client().demo("10.40.63.202", "admin", "amp111", "6300",
        //				new Extension[] { new Extension("from-internal", "SIP/1001", "SIP/1001") },
        //				"http://10.40.63.202:8080/AsteriskExtension/service");
    }
}
