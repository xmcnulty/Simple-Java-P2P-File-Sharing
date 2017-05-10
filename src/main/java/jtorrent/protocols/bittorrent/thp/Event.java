package jtorrent.protocols.bittorrent.thp;

/**
 * Events that are possible in an announce message.
 *
 * @author Xavier McNulty
 * Created by Xavier on 4/27/17.
 */
public enum Event {
    STARTED,
    STOPPED,
    COMPLETED,
    NONE;

    /**
     * Returns an event enumeration from a string.
     * @param s String representation of announce state. Case insensitive.
     * @return Event.
     */
    public static Event fromString(String s) {
        if (s.equalsIgnoreCase(STARTED.toString()))
            return STARTED;
        else if (s.equalsIgnoreCase(STOPPED.toString()))
            return STOPPED;
        else if (s.equalsIgnoreCase(COMPLETED.toString()))
            return COMPLETED;
        else
            return NONE;
    }
}
