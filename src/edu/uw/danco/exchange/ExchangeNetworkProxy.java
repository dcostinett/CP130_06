package edu.uw.danco.exchange;

import edu.uw.danco.exchange.operations.ExecuteTrade;
import edu.uw.danco.exchange.operations.GetQuote;
import edu.uw.danco.exchange.operations.GetState;
import edu.uw.danco.exchange.operations.GetTickers;
import edu.uw.ext.framework.exchange.ExchangeListener;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.Order;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/21/13
 * Time: 9:53 AM
 *
 * The exchange network proxy provides programmatic interface to the exchange network adapter. The exchange network
 * proxy implementation must implement the StockExchange interface. The operations of the StockExchange interface,
 * except the listener registration operations, will be implemented to make requests of the ExchangeNetworkAdapter
 * using the text based custom protocol.
 *
 * The proxy will receive multicast messages representing exchange events. These event messages will be transformed
 * into the appropriate event object and then propagated to registered listeners.
 */
public class ExchangeNetworkProxy implements StockExchange {

    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(ExchangeNetworkAdapter.class.getName());

    /** The event listener list for the exchange */
    private EventListenerList listenerList = new EventListenerList();

    /** The multicast eventGroup */
    private InetAddress eventGroup;

    /** The event multicast socket */
    private MulticastSocket eventMultiSock = null;

    /** The event processor */
    private NetEventProcessor commandProcessor;

    /**
     *
     * @param eventIpAddress - the multicast IP address to connect to
     * @param eventPort - the multicastport to connect to
     * @param cmdIpAddress - the address the exchange accepts requests on
     * @param cmdPort - the port the exchange accepts requests on
     */
    public ExchangeNetworkProxy(final String eventIpAddress,
                                final int eventPort,
                                final String cmdIpAddress,
                                final int cmdPort) {
        try {
            eventGroup = InetAddress.getByName(eventIpAddress);
            eventMultiSock = new MulticastSocket(eventPort);

            commandProcessor = new NetEventProcessor(eventPort, eventGroup, eventMultiSock, cmdIpAddress, cmdPort);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to connect to command socket.", e);
        } finally {
            if (eventMultiSock != null) {
                eventMultiSock.close();
            }
        }
    }

    /**
     * Client command processing entails encoding the command as a string and sending it to the server, reading the
     * response, converting the response string to the appropriate type and finally returning it.
     *
     * The state of the exchange
     * @return - true if the exchange is open, otherwise false
     */
    @Override
    public boolean isOpen() {
        // sends the GET_STATE_CMD command, parses response
        boolean isOpen = false;
        commandProcessor.enqueue(new GetState());
        try {
            ExchangeOperation operation = (ExchangeOperation) commandProcessor.call();
            final Scanner scanner =
                    new Scanner(operation.getResult()).useDelimiter(ProtocolConstants.ELEMENT_DELIMITER.toString());
            isOpen = scanner.next().equals(ProtocolConstants.OPEN_STATE.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception trying to call the event processor", e);
        }
        return true;
    }


    /**
     * Client command processing entails encoding the command as a string and sending it to the server, reading the
     * response, converting the response string to the appropriate type and finally returning it.
     *
     * Gets the ticker symbols for all of the stocks traded on the exchange
     * @return - the stock ticker symbols
     */
    @Override
    public String[] getTickers() {
        // send the GET_TICKERS_CMD command
        final ArrayList<String> tickers = new ArrayList<String>();
        commandProcessor.enqueue(new GetTickers());
        try {
            ExchangeOperation operation = (ExchangeOperation) commandProcessor.call();
            final Scanner scanner =
                    new Scanner(operation.getResult()).useDelimiter(ProtocolConstants.ELEMENT_DELIMITER.toString());
            while (scanner.hasNext()) {
                tickers.add(scanner.next());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception trying to call the event processor", e);
        }
        return tickers.toArray(new String[0]);
    }


    /**
     * Gets a stock's current price
     * @param ticker - the ticker symbol for the stock
     * @return - the quote, or null if the quote is unavailable
     */
    @Override
    public StockQuote getQuote(final String ticker) {
        // send the GET_QUOTE_CMD command
        StockQuote quote = null;

//        byte[] buf = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(buf, buf.length,
//                                                          cmdAddress, cmdPort);
//        byte[] bytes = NetEventProcessor.GetQuoteFor(ticker).getBytes();
//        packet.setData(bytes);
//        packet.setLength(bytes.length);
//        try {
//            cmdSock.getOutputStream().write(bytes);
//
//            byte[] receiveBuffer = new byte[128];
//            cmdSock.getInputStream().read(receiveBuffer);
//
//            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//            Scanner scanner = new Scanner(new String(receivePacket.getData(), 0, receivePacket.getLength()));
//            int price = scanner.nextInt();
//            quote = new StockQuote(ticker, price);
//        } catch (IOException e) {
//            LOGGER.log(Level.SEVERE, "Unable to send getQuote to exchangeServerIp", e);
//        }
        commandProcessor.enqueue(new GetQuote(ticker));
        try {
            ExchangeOperation operation = (ExchangeOperation) commandProcessor.call();
            quote = new StockQuote(ticker, Integer.valueOf(operation.getResult()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception trying to call the event processor", e);
        }

        return quote;
    }


    /**
     * The client registers the Broker as an ExchangeListener with the ExchangeNetworkProxy (the Exchange). The client
     * then receives the multicast messages, converts the message to the appropriate event and notifies the listeners.
     *
     * Adds a market listener. Delegates to the NetEventProcessor
     * @param l - the listener to add
     */
    @Override
    public void addExchangeListener(final ExchangeListener l) {
        listenerList.add(ExchangeListener.class, l);
    }


    /**
     * Removes a market exchange listener. Delegates to the NetEventProcessor.
     * @param l - the listener to remove
     */
    @Override
    public void removeExchangeListener(final ExchangeListener l) {
        listenerList.remove(ExchangeListener.class, l);
    }


    /**
     * Creates a command to execute a trade and sends it to the exchange.
     * @param order - the order to execute
     * @return - the price the order was executed at
     */
    @Override
    public int executeTrade(final Order order) {
        //sends the EXECUTE_TRADE command
        int executionPrice = 0;
        commandProcessor.enqueue(new ExecuteTrade(order));
        try {
            ExchangeOperation operation = (ExchangeOperation) commandProcessor.call();
            executionPrice = Integer.valueOf(operation.getResult());
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Exception executing order id: " + order.getOrderId(), e);
        }

        return executionPrice;
    }
}
