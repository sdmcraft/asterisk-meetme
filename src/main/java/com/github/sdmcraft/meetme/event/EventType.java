package com.github.sdmcraft.meetme.event;

// TODO: Auto-generated Javadoc

/**
 * Denotes the type of the event. The type of the event is governed by the cause
 * of the event.
 */
public enum EventType {

    /**
     * A new user joined.
     */
    USER_JOINED,

    /**
     * The conference ended.
     */
    CONFERENCE_ENDED,

    /**
     * The user was muted.
     */
    MUTE,

    /**
     * The user was unmuted.
     */
    UNMUTE,

    /**
     * The user is talking.
     */
    TALKING,
    /**
     * The user is not talking.
     */
    NOT_TALKING,

    /**
     * The user left.
     */
    USER_LEFT,

    CHANNEL_HUNGUP
}
