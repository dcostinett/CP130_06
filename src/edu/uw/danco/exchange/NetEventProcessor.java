package edu.uw.danco.exchange;

import edu.uw.ext.framework.exchange.ExchangeEvent;

import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/27/13
 * Time: 12:37 PM
 */
public class NetEventProcessor implements Callable {
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
                             final int cmdPort) {
        this.eventPort = eventPort;
        this.eventGroup = eventGroup;
        this.cmdIpAddress = cmdIpAddress;
        this.cmdPort = cmdPort;
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
