package edu.uw.danco.exchange;

import edu.uw.ext.framework.exchange.ExchangeEvent;
import edu.uw.ext.framework.exchange.ExchangeListener;
import edu.uw.ext.framework.exchange.StockExchange;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.EventListener;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/27/13
 * Time: 12:37 PM
 */
public class NetEventProcessor implements Callable, Runnable {
    /** The logger */
    private static final Logger logger = Logger.getLogger(NetEventProcessor.class.getName());

    /** The event queue */
    private final BlockingQueue<ExchangeOperation> commandQueue = new ArrayBlockingQueue<ExchangeOperation>(10, true);

    /** The multicast port to listen on */
    private int eventPort;

    /** The InetAddress of the multicast group */
    private InetAddress eventGroup;

    /** The command port for sending exchange commands */
    private final String cmdIpAddress;

    /** The command port for sending exchange commands */
    private final int cmdPort;

    /** The multicast socket for events */
    private MulticastSocket eventMultiSock;

    /** The socket with which to talk to the server */
    private Socket server;

    /** The event listeners to notify of multicast events */
    private final EventListenerList listeners;


    /**
     * Constructor
     * @param eventPort - the multicast event port
     * @param eventGroup - the multicast group
     * @param eventMultiSock - the multicastsocket for events
     * @param cmdPort - the port for exchange commands
     */
    public NetEventProcessor(final int eventPort,
                             final InetAddress eventGroup,
                             final MulticastSocket eventMultiSock,
                             final String cmdIpAddress,
                             final int cmdPort,
                             final EventListenerList listeners) {
        this.eventPort = eventPort;
        this.eventGroup = eventGroup;
        this.cmdIpAddress = cmdIpAddress;
        this.cmdPort = cmdPort;
        this.listeners = listeners;
        try {
            this.eventMultiSock = eventMultiSock;
            eventMultiSock.joinGroup(eventGroup);

            server = new Socket(cmdIpAddress, cmdPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Method to return the event string for a particular event
     * @param event - the exchange event
     * @return - string representation of the event
     */
    public static String GetEventString(final ExchangeEvent event) {
        StringBuilder sb = new StringBuilder();

        if (event.getEventType().equals(ExchangeEvent.EventType.CLOSED)) {
            sb.append(ProtocolConstants.CLOSED_EVENT);
        } else if (event.getEventType().equals(ExchangeEvent.EventType.OPENED)) {
            sb.append(ProtocolConstants.OPEN_EVENT);
        } else if (event.getEventType().equals(ExchangeEvent.EventType.PRICE_CHANGED)) {
            sb.append(ProtocolConstants.PRICE_CHANGE_EVENT);
            sb.append(ProtocolConstants.ELEMENT_DELIMITER);
            sb.append(event.getTicker());
            sb.append(ProtocolConstants.ELEMENT_DELIMITER);
            sb.append(event.getPrice());
        }

        return sb.toString();
    }


    /**
     * Build string representation for the get quote command
     * @param ticker - the ticker symbol for which to get the quote
     * @return - string representation of the price
     */
    public static String GetQuoteFor(final String ticker) {
        StringBuilder sb = new StringBuilder();

        sb.append(ProtocolConstants.GET_QUOTE_CMD);
        sb.append(ProtocolConstants.ELEMENT_DELIMITER);
        sb.append(ticker);

        return sb.toString();
    }


    /**
     * Enqueue the operation for network processing
     * @param operation - the oepration to send to the exchange
     */
    public void enqueue(final ExchangeOperation operation) {
        commandQueue.add(operation);
    }


    @Override
    public void run() {
        // while (market.isOpen()) // how to determine this from here? If I pass in the proxy to the constructor, I run
        // into a problem, I think, where the proxy hasn't finished construction so I get a hang...
         while (true) {
            try {
                if (!eventMultiSock.isClosed()) {
                    final byte[] receiveBuffer = new byte[128];
                    final DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    eventMultiSock.receive(receivePacket);
                    final String eventStr = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    final Scanner scanner =
                            new Scanner(eventStr).useDelimiter(ProtocolConstants.ELEMENT_DELIMITER.toString());

                    final String eventTypeStr = scanner.next();
                    final ExchangeEvent.EventType eventType =
                            eventTypeStr.equals(ProtocolConstants.PRICE_CHANGE_EVENT.toString()) ?
                            ExchangeEvent.EventType.PRICE_CHANGED :
                                    eventTypeStr.equals(ProtocolConstants.CLOSED_EVENT.toString()) ?
                                            ExchangeEvent.EventType.CLOSED :
                                            ExchangeEvent.EventType.OPENED;

                    for (final ExchangeListener listener : listeners.getListeners(ExchangeListener.class)) {
                        switch (eventType) {
                            case PRICE_CHANGED:
                                final ExchangeEvent priceChangedEvent =
                                        ExchangeEvent.newPriceChangedEvent(this, scanner.next(), scanner.nextInt());
                                listener.priceChanged(priceChangedEvent);
                                break;

                            case CLOSED:
                                final ExchangeEvent closedEvent = ExchangeEvent.newClosedEvent(this);
                                listener.exchangeClosed(closedEvent);
                                break;

                            case OPENED:
                                final ExchangeEvent openedEvent = ExchangeEvent.newOpenedEvent(this);
                                listener.exchangeOpened(openedEvent);
                                break;

                            default:
                                logger.log(Level.WARNING, "Unable to determine event type from: " + eventTypeStr);
                        }
                    }
                } else {
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Wait was interrupted.", e);
                    }
                }
            } catch (final IOException e) {
                logger.log(Level.SEVERE, "Exception reading from multisock", e);
            }
        }
    }

    @Override
    public Object call() throws Exception {
        ExchangeOperation operation = null;
        try {
            operation = commandQueue.take();

            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
            writer.write(operation.getCommand() + "\n");
            writer.flush();

            final InputStreamReader isr = new InputStreamReader(server.getInputStream());
            final BufferedReader reader = new BufferedReader(isr);
            final String result = reader.readLine();

            operation.setResult(result);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted waiting for event", e);
        }

        return operation;
    }
}
