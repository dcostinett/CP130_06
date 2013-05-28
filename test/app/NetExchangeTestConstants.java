package app;


/**
 * Defines constants shared by the network exchange and proxy test drivers.
 *
 * @author Russ Moul
 */
public final class NetExchangeTestConstants {
    /** Event IP preference name. */
    public static final String EVENT_IP_PREF = "eventIpAddress";

    /** Event IP default. */
    public static final String EVENT_IP_DEFAULT = "224.0.0.1";

    /** Event port preference name. */
    public static final String EVENT_PORT_PREF = "eventPort";

    /** Event port default. */
    public static final int EVENT_PORT_DEFAULT = 5000;

    /** Command IP preference name. */
    public static final String COMMAND_IP_PREF = "commandIpAddress";

    /** Command IP default. */
    public static final String COMMAND_IP_DEFAULT = "127.0.0.1";

    /** Command port preference name. */
    public static final String COMMAND_PORT_PREF = "commandPort";

    /** Command port default. */
    public static final int COMMAND_PORT_DEFAULT = 5001;

    /**
     * Private constructor prevents instantiation.
     */
    private NetExchangeTestConstants() {
        super();
    }
}

