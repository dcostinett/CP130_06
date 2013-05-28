package app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Receives the multicast datagrams and prints them as UTF-8 Strings.
 */
public class EventMonitor {
    /** This class' logger. */
    private static final Logger LOG =
                         Logger.getLogger(ExchangeProxyDriver.class.getName());

    /**
     * The program.
     *
     * @param args (not used)
     * @throws java.io.IOException if any are raised
     */
    public static void main(String[] args) {
        // get the preferences
        Preferences prefs;
        prefs = Preferences.userNodeForPackage(ExchangeProxyDriver.class);
        String eventIp = prefs.get(NetExchangeTestConstants.EVENT_IP_PREF,
                NetExchangeTestConstants.EVENT_IP_DEFAULT);
        int eventPort = prefs.getInt(NetExchangeTestConstants.EVENT_PORT_PREF,
                NetExchangeTestConstants.EVENT_PORT_DEFAULT);
        LOG.info("Preferences loaded: ." + eventIp + ":" + eventPort
                
        );

        MulticastSocket eventSocket = null;
        try {
        InetAddress eventGroup = InetAddress.getByName(eventIp);
        eventSocket = new MulticastSocket(eventPort);
        eventSocket.joinGroup(eventGroup);

        byte[] buf = new byte[512];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (true) {
            eventSocket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength(), "UTF8");
            LOG.info(msg);
        }
        } catch (IOException ex) {
            if (eventSocket != null) {
                eventSocket.close();
            }
        }
    }
}
