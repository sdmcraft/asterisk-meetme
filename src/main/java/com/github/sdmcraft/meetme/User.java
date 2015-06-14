package com.github.sdmcraft.meetme;

import com.github.sdmcraft.meetme.event.Event;
import com.github.sdmcraft.meetme.event.EventType;
import org.asteriskjava.live.MeetMeUser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc

/**
 * User represents a participant in a MeetMe conference.
 */
public class User extends Observable implements PropertyChangeListener {

    /**
     * The meet me user.
     */
    private MeetMeUser meetMeUser;

    private boolean alive = false;

    /**
     * The Constant logger.
     */
    private final static Logger logger = Logger.getLogger(User.class.getName());

    private final Timer timer;
    private TimerTask dispatchUserLeftTask;

    // public User(AsteriskChannel channel, String userId, String phoneNumber,
    // boolean muted, boolean talking) {
    // this.channel = channel;
    // this.userId = userId;
    // this.phoneNumber = phoneNumber;
    // this.muted = muted;
    // this.talking = talking;
    // }

    /**
     * Instantiates a new user.
     *
     * @param meetMeUser the meet me user
     */
    public User(MeetMeUser meetMeUser) {
        timer = new Timer();
        addOrReplaceMeetMeUser(meetMeUser);
    }

    public void addOrReplaceMeetMeUser(MeetMeUser meetMeUser) {
        boolean transferred = false;
        if(this.meetMeUser != null) {
            this.meetMeUser.removePropertyChangeListener(this);
            transferred = true;
        }
        this.meetMeUser = meetMeUser;
        this.meetMeUser.addPropertyChangeListener(this);
        alive = true;
        if(dispatchUserLeftTask != null) {
            dispatchUserLeftTask.cancel();
            dispatchUserLeftTask = null;
        }
        if(transferred) {
            logger.info("Dispatching meetMeUser-transferred");
            notifyObservers(new Event(EventType.USER_TRANSFERRED));
        }
    }

    public void requestHangUp() {
        if (!alive) {
            System.out.println("Ignoring hangup request, " + getPhoneNumber()
                    + " is already dead!!");

        } else {
            System.out.println("Requesting hangup for " + getPhoneNumber());
            this.meetMeUser.getChannel().hangup();
        }
    }

    public void requestMuteStateChange() {
        if (meetMeUser.isMuted()) {
            System.out.println("Unmuting user " + meetMeUser.getUserNumber());
            meetMeUser.unmute();
        } else {
            System.out.println("Muting user " + meetMeUser.getUserNumber());
            meetMeUser.mute();
        }
    }

    public void requestStartRecording() {
        meetMeUser.getChannel().startMonitoring(
                getUserId() + "_" + System.currentTimeMillis(), "wav", true);
    }

    public void requestStopRecording() {
        meetMeUser.getChannel().stopMonitoring();
    }

    public void requestTransfer(String roomNumber) {
        //TODO Remove hardcoding
        meetMeUser.getChannel().redirect("LocalSets", roomNumber, 1);
    }

    /*
     * (non-Javadoc)
     *
     * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!alive)
            System.out
                    .println("Received event. This looks like a problem!! The user is already dead");
        String propertyName = evt.getPropertyName();
        String propertyValue = evt.getNewValue().toString();
        System.out.println(propertyName + " = " + propertyValue);
        if ("muted".equals(propertyName)) {
            setChanged();
            if ("true".equals(propertyValue))
                notifyObservers(new Event(EventType.MUTE));
            else
                notifyObservers(new Event(EventType.UNMUTE));
        }
        if ("talking".equals(propertyName)) {
            setChanged();
            if ("true".equals(propertyValue))
                notifyObservers(new Event(EventType.TALKING));
            else
                notifyObservers(new Event(EventType.NOT_TALKING));
        } else if ("state".equals(propertyName) && "LEFT".equals(propertyValue)) {
            destroy();
        }
    }

    /**
     * Destroy.
     */
    public void destroy() {
        logger.info("Destroying user " + getUserId());
        meetMeUser.removePropertyChangeListener(this);
        setChanged();
        alive = false;
        if(dispatchUserLeftTask != null) {
            dispatchUserLeftTask.cancel();
        }
        dispatchUserLeftTask = new TimerTask() {
            long scheduled = System.currentTimeMillis();
            @Override
            public void run() {
                logger.info("Running after " + (System.currentTimeMillis() - scheduled)/1000);
                notifyObservers(new Event(EventType.USER_LEFT));
            }
        };
        timer.schedule(dispatchUserLeftTask, 5000);
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return generateUserId(this.meetMeUser);
    }

    public static String generateUserId(MeetMeUser meetMeUser) {
        return generateUserId(meetMeUser, meetMeUser.getRoom().getRoomNumber());
    }

    public static String generateUserId(MeetMeUser meetMeUser, String conferenceId) {
        return AsteriskUtils.getUserPhoneNumber(meetMeUser) + "@"
                + conferenceId;
    }

    /**
     * Checks if is muted.
     *
     * @return true, if is muted
     */
    public boolean isMuted() {
        return meetMeUser.isMuted();
    }

    /**
     * Checks if is talking.
     *
     * @return true, if is talking
     */
    public boolean isTalking() {
        return meetMeUser.isTalking();
    }

    /**
     * Gets the phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
//		return AsteriskUtils.getPhoneNumberFromChannel(meetMeUser.getChannel()
//				.getName());
        return AsteriskUtils.getUserPhoneNumber(meetMeUser);

    }

    /**
     * Gets the user number.
     *
     * @return the user number
     */
    public Integer getUserNumber() {
        return meetMeUser.getUserNumber();
    }

    public boolean isAlive() {
        return alive;
    }

    public String getConferenceId() {
        return this.meetMeUser.getRoom().getRoomNumber();
    }

}
