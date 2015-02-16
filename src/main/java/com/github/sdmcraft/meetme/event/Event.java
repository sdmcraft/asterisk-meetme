package com.github.sdmcraft.meetme.event;

// TODO: Auto-generated Javadoc
/**
 * Represents a MeetMe event. MeetMe events are dipatched as a result of a
 * change in the conferencing state for e.g. on somebody joining/leaving a
 * conference. @see com.github.sdmcraft.meetme.event
 */
public class Event {

	/** The type. */
	private final EventType type;

	/** The data. */
	private final Object data;

	/**
	 * Instantiates a new event.
	 * 
	 * @param type
	 *            the type
	 */
	public Event(EventType type) {
		this.type = type;
		data = null;
	}

	/**
	 * Instantiates a new event.
	 * 
	 * @param type
	 *            the type
	 * @param data
	 *            the data
	 */
	public Event(EventType type, Object data) {
		this.type = type;
		this.data = data;
	}

	/**
	 * Gets the event type.The type of the event tells the cause of this event.
	 * 
	 * @return the event type indicating the event cause
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * Gets the data associated with this event. This method is only relevant
	 * for USER_JOINED event wherein it gets the user who just joined.
	 * 
	 * @return the data associated with this event
	 */
	public Object getData() {
		return data;
	}

}
