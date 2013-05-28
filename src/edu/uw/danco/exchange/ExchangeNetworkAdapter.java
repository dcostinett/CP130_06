package edu.uw.danco.exchange;

import edu.uw.ext.framework.exchange.ExchangeAdapter;
import edu.uw.ext.framework.exchange.ExchangeEvent;
import edu.uw.ext.framework.exchange.StockExchange;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/21/13
 * Time: 9:52 AM
 *
 * The exchange network adapter provides all of the functionality of the exchange through a network connection.
 * Provides a network interface to an exchange.
 *
 * The implementation will need to implement the listener interfaces of ExchangeAdapter and register these listeners
 * with the "real" exchange. The listeners need to be implemented such that the events are converted to text messages
 * and then multicast to brokers. The adapter must also provide a text based custom protocol using TCP sockets to
 * access the isOpen, getQuote, getTickers and executeTrade operations of the exchange interface.
 */
public class ExchangeNetworkAdapter implements ExchangeAdapter {

    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(ExchangeNetworkAdapter.class.getName());

    /** The exchange used to service the network requests */
    private final StockExchange exchange;

    /** the ip address used to propagate price changes */
    private final String multicastIp;

    /** the ip port used to propagate price changes */
    private final int multicastPort;

    /** the port for listening for commands */
    private final int commandPort;

    /** The multicast socket  */
    private MulticastSocket multiSock = null;

    /** The multicast group */
    private InetAddress group = null;

    /** The Socket used to listen for commands */
    ServerSocket serverSocket;

    /**
     * Server event processing consists of the ExchangeNetworkAdapter registering as an ExchangeListener
     *
     * @param exchange - the exchange used to service the network requests
     * @param multicastIp - the ipaddress used to propagate price changes
     * @param multicastPort - the ip port used to propagate price changes
     * @param commandPort - the port for listening for commands
     * @throws UnknownHostException
     * @throws SocketException
     */
    public ExchangeNetworkAdapter(final StockExchange exchange,
                                  final String multicastIp,
                                  final int multicastPort,
                                  final int commandPort)
            throws UnknownHostException, SocketException {
        this.exchange = exchange;
        this.multicastIp = multicastIp;
        this.multicastPort = multicastPort;
        this.commandPort = commandPort;

        try {
            group = InetAddress.getByName(multicastIp);
            multiSock = new MulticastSocket();
            multiSock.joinGroup(group);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to open socket", e);
        }

        this.exchange.addExchangeListener(this);
    }


    /**
     * Close the adapter
     */
    @Override
    public void close() {
        if (multiSock != null) {
            try {
                multiSock.leaveGroup(InetAddress.getByName(multicastIp));
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Exception trying to leave multicast group", e);
            } finally {
                multiSock.close();
            }
        }
    }

    /**
     * the listener methods encode the events as text and multicast them.
     *
     * The exchange has opened and prices are adjusting - add listener to receive price change events from the exchange
     * and multicast them to brokers.
     * @param event - the event
     */
    @Override
    public void exchangeOpened(final ExchangeEvent event) {
        exchange.addExchangeListener(this);
        multicastEvent(event);
    }


    /**
     * the listener methods encode the events as text and multicast them.
     *
     * The exchange has closed - notify clients and remove price change listener.
     * @param event - the event
     */
    @Override
    public void exchangeClosed(final ExchangeEvent event) {
        multicastEvent(event);
        exchange.removeExchangeListener(this);
    }


    /**
     * the listener methods encode the events as text and multicast them.
     *
     * Process price changed events.
     * @param event - the event
     */
    @Override
    public void priceChanged(final ExchangeEvent event) {
        multicastEvent(event);
    }


    private void multicastEvent(final ExchangeEvent event) {
        byte[] buf = new byte[1024];
        final DatagramPacket packet = new DatagramPacket(buf, buf.length,
                                                                group, multicastPort);
        final String openEvent = NetEventProcessor.GetEventString(event);
        byte[] bytes = openEvent.getBytes();
        packet.setData(bytes);
        packet.setLength(bytes.length);
        try {
            multiSock.send(packet);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Unable to multicast %s event", event.getEventType()), e);
        }
    }
}
