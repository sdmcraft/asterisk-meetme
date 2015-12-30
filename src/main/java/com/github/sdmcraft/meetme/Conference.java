package com.github.sdmcraft.meetme;

import com.github.sdmcraft.meetme.event.Event;
import com.github.sdmcraft.meetme.event.EventType;
import org.asteriskjava.live.MeetMeRoom;
import org.asteriskjava.live.MeetMeUser;
import org.asteriskjava.manager.action.OriginateAction;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Conference represents a MeetMe conference on the Asterisk server. This class
 * implements the observable interface. Any class which wants to receive events
 * related to this conference, must add itself as an observer of this class
 * instance.
 */
public class Conference extends Observable {

    /**
     * The meet me room.
     */
    private final MeetMeRoom meetMeRoom;

    /**
     * A map of conference users to their respective meetme userIDs
     */
    private final Map<String, User> conferenceUserMap;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The conference id.
     */
    private final String conferenceNumber;

    /**
     * The recording.
     */
    private boolean recording;

    private List<String> pendingDialOuts = new CopyOnWriteArrayList<String>();

    private final static Logger logger = Logger.getLogger(Conference.class
            .getName());

    // private final String conferenceNumber;

    /**
     * Represents a MeetMe conference.
     *
     * @param conferenceNumber the number to be dialled in order to join the conference
     * @param context          the context for interaction between MeetMe-java and the
     *                         Asterisk server
     */
    private Conference(String conferenceNumber, Context context) {

        logger.info("Creating a new conference " + conferenceNumber);
        meetMeRoom = context.getAsteriskServer()
                .getMeetMeRoom(conferenceNumber);
        this.context = context;
        this.conferenceUserMap = meetMeUsersToConferenceUserMap(meetMeRoom
                .getUsers());
        this.conferenceNumber = conferenceNumber;
    }

    /**
     * Initializes the audio conference. This is necessary in order to receive
     * events for this conference.
     */
    private void init() {
        logger.info("Adding conference " + conferenceNumber + " to context");
        context.getConferences().put(conferenceNumber, this);
    }

    /**
     * Creates a new instance of MeetMe conference.
     *
     * @param conferenceNumber the number to be dialled in order to join the conference
     * @param context          the context for interaction between MeetMe-java and the
     *                         Asterisk server
     * @return single instance of Conference
     */
    public static Conference getInstance(String conferenceNumber,
                                         Context context) {
        Conference conference = new Conference(conferenceNumber, context);
        conference.init();
        return conference;
    }

    /**
     * Meet me users to conference user map.
     *
     * @param meetMeUsers the meet me users
     * @return the map
     */
    private static Map<String, User> meetMeUsersToConferenceUserMap(
            Collection<MeetMeUser> meetMeUsers) {
        Map<String, User> conferenceUserMap = new HashMap<String, User>();
        for (MeetMeUser meetMeUser : meetMeUsers) {
            User conferenceUser = meetMeUserToConferenceUser(meetMeUser);
            conferenceUserMap.put(conferenceUser.getUserId(), conferenceUser);
        }
        return conferenceUserMap;
    }

    /**
     * Meet me user to conference user.
     *
     * @param meetMeUser the meet me user
     * @return the user
     */
    private static User meetMeUserToConferenceUser(MeetMeUser meetMeUser) {
        User conferenceUser = new User(meetMeUser);
        return conferenceUser;
    }

    /**
     * Gets the meet me user.
     *
     * @param userId the user id
     * @return the meet me user
     */
    private MeetMeUser getMeetMeUser(String userId) {
        for (MeetMeUser meetMeUser : meetMeRoom.getUsers()) {
            if (meetMeUser.getChannel().getId().equals(userId))
                return meetMeUser;
        }
        return null;
    }

    /**
     * Gets the conference user.
     *
     * @param phoneNumber the phone number
     * @return the conference user
     */
    private User getConferenceUser(String phoneNumber) {
        for (User conferenceUser : this.conferenceUserMap.values()) {
            if (phoneNumber.equals(conferenceUser.getPhoneNumber()))
                return conferenceUser;
        }
        return null;
    }

    public String requestDialOut(Extension extn) throws Exception {

        logger.info("Requesting dial out for phone: " + extn.getNumber());
        OriginateAction dialoutAction = new OriginateAction();
        dialoutAction.setChannel(extn.getNumber());
        //dialoutAction.setContext(extn.getContext());
        dialoutAction.setCallerId(extn.getCallerId());

		/* TODO Remove these hardcodings */
        //dialoutAction.setPriority(new Integer(1));
        //dialoutAction.setTimeout(new Long(30000));

        //dialoutAction.setExten(meetMeRoom.getRoomNumber());

        dialoutAction.setApplication("MeetMe");
        dialoutAction.setData(meetMeRoom.getRoomNumber() + ",TM");

        context.getConnection().getManagerConnection()
                .sendAction(dialoutAction, new DialoutActionCallback());

        pendingDialOuts.add(extn.getCallerId());

        logger.info("Dial out was requested for " + extn.getNumber());
        return extn.getNumber();
    }

    /**
     * Request end conference.
     *
     * @throws Exception the exception
     */
    public void requestEndConference() throws Exception {
        logger.info("Request received to end conference. Hanging up all users.");

		/*
         * Lock conferenceUserMap to avoid user left events modifying it while
		 * we place hangup requests for all users
		 */
        synchronized (conferenceUserMap) {
            for (String userId : conferenceUserMap.keySet()) {
                conferenceUserMap.get(userId).requestHangUp();
            }

        }
    }

    public void requestMoveConference(User user, String confNumber) {
        if (!context.getConferences().containsKey(confNumber)) {
            Conference.getInstance(confNumber, context);
        }
        user.requestTransfer(confNumber);
    }

    /**
     * Handle add conference user.
     *
     * @param user        the user
     * @throws Exception the exception
     */
    public void handleAddConferenceUser(MeetMeUser user) throws Exception {
        logger.info("Handling user join event");
        User conferenceUser = new User(user);
        conferenceUserMap.put(conferenceUser.getUserId(), conferenceUser);

        setChanged();

        logger.info("Dispatching user-joined");
        notifyObservers(new Event(EventType.USER_JOINED, conferenceUser));

    }

    /**
     * Handle end conference.
     */
    public void handleEndConference() {
        logger.info("Handling conference end event");
        conferenceUserMap.clear();
        setChanged();
        notifyObservers(new Event(EventType.CONFERENCE_ENDED, this));
    }

    /**
     * Destroys this conference instance.
     */
    public void destroy() {
        logger.info("Destroying the conference " + conferenceNumber);
        for (User user : conferenceUserMap.values()) {
            if (user.isAlive())
                user.requestHangUp();
        }
        conferenceUserMap.clear();
        context.getConferences().remove(conferenceNumber);
    }

    /**
     * Gets the users.
     *
     * @return the users
     */
    public Map<String, User> getUsers() {
        return conferenceUserMap;
    }

    /**
     * Checks if is recording.
     *
     * @return true, if is recording
     */
    public boolean isRecording() {
        return recording;
    }

    /**
     * Gets the conference id.
     *
     * @return the conference id
     */
    public String getconferenceNumber() {
        return conferenceNumber;
    }

    @Override
    public String toString() {
        if (meetMeRoom == null)
            return null;
        return meetMeRoom.getRoomNumber();
    }

    /**
     * @return the pendingDialOuts
     */
    public List<String> getPendingDialOuts() {
        return pendingDialOuts;
    }
}
