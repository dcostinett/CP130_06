package app;

import edu.uw.ext.framework.account.Account;

import java.util.prefs.Preferences;

/**
 * Utility class to initialize the preferences database.
 *
 * @author Russ Moul
 */
public final class InitPrefs {
    /** Minimum length of a legal account name. */
    private static final int MIN_ACCOUNT_LEN = 8;

    /** Minimum initial account balance. */
    private static final int MIN_ACCOUNT_BALANCE = 100000;

    /** The event port. */
    private static final int EVENT_PORT = 5000;

    /** The command part. */
    private static final int COMMAND_PORT = 5500;

    /**
     * Prevent instantiation of this class.
     */
    private InitPrefs () {
    }

    /**
     * Just jams our prefs values into the user prefs database.
     * @param args (not used)
     */
    public static void main(final String[] args) {
        String pad = "    ";
        String minAccountLengthKey = "minAccountLength";
        int minAccountLengthValue = MIN_ACCOUNT_LEN;
        String minAccountBalanceKey = "minAccountBalance";
        int minAccountBalanceValue = MIN_ACCOUNT_BALANCE;

        String eventIpAddressKey = "eventIpAddress";
        String eventIpAddressValue = "228.1.1.1";
        String eventPortKey = "eventPort";
        int eventPortValue = EVENT_PORT;
        String commandIpAddressKey = "commandIpAddress";
        String commandIpAddressValue = "127.0.0.1";
        String commandPortKey = "commandPort";
        int commandPortValue = COMMAND_PORT;

        Preferences prefs = Preferences.userNodeForPackage(Account.class);
        String path = prefs.absolutePath();
        System.out.println("Inserting user preferences:");
        System.out.println(pad + path + "/" + minAccountLengthKey + " = " + minAccountLengthValue);
        prefs.putInt(minAccountLengthKey, minAccountLengthValue);

        System.out.println(pad + path + "/" + minAccountBalanceKey + " = " + minAccountBalanceValue);
        prefs.putInt(minAccountBalanceKey, minAccountBalanceValue);

        prefs = prefs.node("/test");
        path = prefs.absolutePath();

        System.out.println(pad + path + "/" + eventIpAddressKey + " = " + eventIpAddressValue);
        prefs.put(eventIpAddressKey, eventIpAddressValue);

        System.out.println(pad + path + "/" + eventPortKey + " = " + eventPortValue);
        prefs.putInt(eventPortKey, eventPortValue);

        System.out.println(pad + path + "/" + commandIpAddressKey + " = " + commandIpAddressValue);
        prefs.put(commandIpAddressKey, commandIpAddressValue);

        System.out.println(pad + path + "/" + commandPortKey + " = " + commandPortValue);
        prefs.putInt(commandPortKey, commandPortValue);
    }

}
