package com.github.sdmcraft.meetme;

import com.github.sdmcraft.meetme.event.Event;
import com.github.sdmcraft.meetme.event.EventType;
import org.asteriskjava.live.MeetMeUser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
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
        this.meetMeUser = meetMeUser;
        meetMeUser.addPropertyChangeListener(this);
        alive = true;
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
        notifyObservers(new Event(EventType.USER_LEFT));
        alive = false;
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public String getUserId() {
        return AsteriskUtils.getUserPhoneNumber(meetMeUser) + "@"
                + meetMeUser.getRoom().getRoomNumber();
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

}
