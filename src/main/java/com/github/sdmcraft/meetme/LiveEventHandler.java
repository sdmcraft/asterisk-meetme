package com.github.sdmcraft.meetme;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskQueue;
import org.asteriskjava.live.AsteriskQueueEntry;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.AsteriskServerListener;
import org.asteriskjava.live.ChannelState;
import org.asteriskjava.live.MeetMeRoom;
import org.asteriskjava.live.MeetMeUser;
import org.asteriskjava.live.internal.AsteriskAgentImpl;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.MeetMeEndEvent;
import org.asteriskjava.manager.event.NewCallerIdEvent;
import org.asteriskjava.manager.event.NewExtenEvent;
import org.asteriskjava.manager.event.NewStateEvent;

// TODO: Auto-generated Javadoc
/**
 * The Class LiveEventHandler.
 */
public class LiveEventHandler implements AsteriskServerListener,
		ManagerEventListener, PropertyChangeListener {

	/** The Constant logger. */
	private final static Logger logger = Logger
			.getLogger(LiveEventHandler.class.getName());

	/** The context. */
	private final Context context;

	/**
	 * Instantiates a new live event handler.
	 * 
	 * @param context
	 *            the context
	 */
	public LiveEventHandler(Context context) {
		this.context = context;
	}

	/**
	 * Inits the.
	 */
	public void init() {
		AsteriskServer asteriskServer = context.getAsteriskServer();
		asteriskServer.addAsteriskServerListener(this);
		context.getConnection().addManagerEventListener(this);

		for (AsteriskChannel asteriskChannel : asteriskServer.getChannels()) {
			logger.info(asteriskChannel.toString());
			asteriskChannel.addPropertyChangeListener(this);
		}

		for (AsteriskQueue asteriskQueue : asteriskServer.getQueues()) {
			logger.info(asteriskQueue.toString());
			for (AsteriskQueueEntry asteriskQueueEntry : asteriskQueue
					.getEntries()) {
				asteriskQueueEntry.getChannel().addPropertyChangeListener(this);
			}
		}

		for (MeetMeRoom meetMeRoom : asteriskServer.getMeetMeRooms()) {
			logger.info(meetMeRoom.toString());
			for (MeetMeUser user : meetMeRoom.getUsers()) {
				user.addPropertyChangeListener(this);
			}
		}

		logger.info("LiveEventHandler registered to receive events..");
	}

	/**
	 * Destroy.
	 */
	public void destroy() {
		AsteriskServer asteriskServer = context.getAsteriskServer();
		asteriskServer.removeAsteriskServerListener(this);
		context.getConnection().removeManagerEventListener(this);

		for (AsteriskChannel asteriskChannel : asteriskServer.getChannels()) {
			logger.info(asteriskChannel.toString());
			asteriskChannel.removePropertyChangeListener(this);
		}

		for (AsteriskQueue asteriskQueue : asteriskServer.getQueues()) {
			logger.info(asteriskQueue.toString());
			for (AsteriskQueueEntry asteriskQueueEntry : asteriskQueue
					.getEntries()) {
				asteriskQueueEntry.getChannel().removePropertyChangeListener(
						this);
			}
		}

		for (MeetMeRoom meetMeRoom : asteriskServer.getMeetMeRooms()) {
			logger.info(meetMeRoom.toString());
			for (MeetMeUser user : meetMeRoom.getUsers()) {
				user.removePropertyChangeListener(this);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.asteriskjava.live.AsteriskServerListener#onNewAgent(org.asteriskjava
	 * .live.internal.AsteriskAgentImpl)
	 */
	@Override
	public void onNewAgent(AsteriskAgentImpl agent) {
		logger.info(agent.toString());
		agent.addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.asteriskjava.live.AsteriskServerListener#onNewAsteriskChannel(org
	 * .asteriskjava.live.AsteriskChannel)
	 */
	@Override
	public void onNewAsteriskChannel(AsteriskChannel channel) {
		logger.info("A new channel joined:" + channel.toString());
		channel.addPropertyChangeListener(this);
		context.getChannels().put(channel.getId(), channel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.asteriskjava.live.AsteriskServerListener#onNewMeetMeUser(org.asteriskjava
	 * .live.MeetMeUser)
	 */
	@Override
	public void onNewMeetMeUser(MeetMeUser user) {
		logger.info("A new user joined " + user.toString());
		try {
			// String phoneNumber = AsteriskUtils.getUserPhoneNumber(user);
			// logger.info("Phone Number:" + phoneNumber);
			// Map<String, String> dialOutLock = context.getDialOutLocks().get(
			// phoneNumber);
			// if (dialOutLock != null) {
			// synchronized (dialOutLock) {
			// dialOutLock.put("user-id", user.getChannel().getId());
			// dialOutLock.notifyAll();
			// }
			// /* Sleep for sometime to let notified thread proceed */
			// try {
			// Thread.sleep(500);
			// } catch (InterruptedException ex) {
			// logger.log(Level.WARNING, ex.getMessage(), ex);
			// }
			// }

			Conference conference = context.getConferences().get(
					user.getRoom().getRoomNumber());
			conference.handleAddConferenceUser(user);
		} catch (Exception ex) {
			logger.log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.asteriskjava.live.AsteriskServerListener#onNewQueueEntry(org.asteriskjava
	 * .live.AsteriskQueueEntry)
	 */
	@Override
	public void onNewQueueEntry(AsteriskQueueEntry queueEntry) {
		logger.info(queueEntry.toString());
		queueEntry.addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
		logger.info(propertyChangeEvent.toString());
		logger.info(propertyChangeEvent.getPropertyName());
		if(propertyChangeEvent.getPropertyName().equals("state") && propertyChangeEvent.getOldValue().toString().equals("RINGING") &&
				propertyChangeEvent.getNewValue().toString().equals("UP"))
		{
			AsteriskChannel source = (AsteriskChannel)propertyChangeEvent.getSource();	
			source.getCallerId();
		}
		/*
INFO: java.beans.PropertyChangeEvent[propertyName=state; oldValue=RINGING; newValue=UP; propagationId=null; source=AsteriskChannel[id='1406094200.6',name='SIP/callcentric-00000002',callerId='"SIP/callcentric/011919971647800"',state='UP',account='null',dateOfCreation=Wed Jul 23 11:13:29 IST 2014,dialedChannel=null,dialingChannel=null,linkedChannel=null]]
INFO: java.beans.PropertyChangeEvent[propertyName=state; oldValue=RINGING; newValue=UP; propagationId=null; source=AsteriskChannel[id='1406092463.0',name='SIP/1001-00000000',callerId='"1001" <1001>',state='UP',account='null',dateOfCreation=Wed Jul 23 10:44:30 IST 2014,dialedChannel=null,dialingChannel=null,linkedChannel=null]]
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.asteriskjava.manager.ManagerEventListener#onManagerEvent(org.asteriskjava
	 * .manager.event.ManagerEvent)
	 */
	@Override
	public void onManagerEvent(ManagerEvent managerEvent) {
		logger.info("Received manager event: " + managerEvent);
		if (managerEvent instanceof MeetMeEndEvent) {
			MeetMeEndEvent endEvent = (MeetMeEndEvent) managerEvent;
			Conference conference = context.getConferences().get(
					endEvent.getMeetMe());
			if (conference != null)
				conference.handleEndConference();
		} else if (managerEvent instanceof HangupEvent) {
			HangupEvent hangupEvent = (HangupEvent) managerEvent;
			context.handleChannelHangup(hangupEvent.getUniqueId());
		} else if (managerEvent instanceof NewStateEvent) {
			NewStateEvent stateEvent = (NewStateEvent)managerEvent;
			if(ChannelState.UP.equals(stateEvent.getChannelState()))
			{
				for(Conference conference : context.getConferences().values())
				{
					for(String pendingDialout : conference.getPendingDialOuts())
					{
						if(stateEvent.getCallerIdNum().equals(pendingDialout))
						{
							//conference.handleAddConferenceUser(user)
						}
					}
				}
			}
			int a = 1;
		}
	}

}
