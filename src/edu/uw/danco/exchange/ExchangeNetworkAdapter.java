package edu.uw.danco.exchange;

import edu.uw.ext.framework.exchange.ExchangeAdapter;
import edu.uw.ext.framework.exchange.ExchangeEvent;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.MarketBuyOrder;
import edu.uw.ext.framework.order.MarketSellOrder;
import edu.uw.ext.framework.order.Order;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/21/13
 * Time: 9:52 AM
 *
 * Provides a network interface to an exchange.
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

    /** The multicast socket  */
    private MulticastSocket multiSock = null;

    /** The multicast group */
    private InetAddress group = null;

    /** Listens for commands to send to the exchange */
    private CommandListener listener;

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

        try {
            group = InetAddress.getByName(multicastIp);
            multiSock = new MulticastSocket();
            multiSock.joinGroup(group);

            listener = new CommandListener(exchange, commandPort);
            listener.start();
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
                listener.close();
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
        final String eventStr = NetEventProcessor.GetEventString(event);
        byte[] bytes = eventStr.getBytes();
        packet.setData(bytes);
        packet.setLength(bytes.length);
        try {
            multiSock.send(packet);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Unable to multicast %s event", event.getEventType()), e);
        }
    }


    /**
     *
     */
    private static class CommandListener extends Thread {

        /** The logger */
        private static final Logger logger = Logger.getLogger(CommandListener.class.getName());

        /** The Socket used to listen for commands */
        private ServerSocket serverSocket;

        /** Reference to the real exchange */
        private final StockExchange exchange;

        /** The port to listen to commands on */
        private final int commandPort;

        /** The socket encapsulating a client connection. */
        private Socket client;

        private CommandListener(StockExchange exchange, int commandPort) {
            this.exchange = exchange;
            this.commandPort = commandPort;

            try {
                serverSocket = new ServerSocket(commandPort);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Unable to open command port.", e);
            }
        }

        @Override
        public void run() {
            while (!serverSocket.isClosed()) {
                logger.info("Listening for commands");
                try {
                    client = serverSocket.accept();
                    final CommandHandler handler = new CommandHandler(exchange, client);
                    final Thread handlerThread = new Thread(handler);
                    handlerThread.start();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Exception listening on server with port = " + commandPort, e);
                }
            }
        }


        /**
         * Call to close the server socket and stop listening for commands
         */
        public void close() {
            try {
                client.close();
                serverSocket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Unable to close server socket", e);
            } finally {
                client = null;
                serverSocket = null;
            }
        }
    }


    /**
     *
     */
    private static class CommandHandler implements Runnable {
        /** The logger */
        private static final Logger logger = Logger.getLogger(CommandHandler.class.getName());

        /** The real exchange */
        private final StockExchange exchange;

        /** The socket encapsulating a client connection. */
        private Socket socket;

        private CommandHandler(final StockExchange exchange, final Socket socket) {
            this.exchange = exchange;
            this.socket = socket;
        }

        @Override
        public void run() {
            while (!socket.isClosed()) {
                try {
                    final InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                    final BufferedReader reader = new BufferedReader(isr);
                    final String command = reader.readLine();
                    if (command == null) {
                        continue;
                    }
                    final Scanner scanner =
                            new Scanner(command).useDelimiter(ProtocolConstants.ELEMENT_DELIMITER.toString());
                    final String name = scanner.next();
                    final ProtocolConstants cmdName = ProtocolConstants.valueOf(name);
                    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    switch (cmdName) {
                        case GET_QUOTE_CMD:
                            scanner.skip(ProtocolConstants.ELEMENT_DELIMITER.toString());
                            final String ticker = scanner.next();
                            final StockQuote quote = exchange.getQuote(ticker);
                            writer.write(quote.getPrice() + "\n");
                            writer.flush();
                            break;

                        case GET_TICKERS_CMD:
                            String[] tickers = exchange.getTickers();
                            for (final String symbol : tickers) {
                                writer.write(symbol);
                                writer.write(ProtocolConstants.ELEMENT_DELIMITER.toString());
                            }
                            writer.flush();
                            break;

                        case GET_STATE_CMD:
                            if (exchange.isOpen()) {
                                writer.write(ProtocolConstants.OPEN_STATE.toString());
                            } else {
                                writer.write(ProtocolConstants.CLOSED_STATE.toString());
                            }
                            writer.flush();
                            break;

                        case EXECUTE_TRADE_CMD:
                            /*
                             * Request:
                             * [EXECUTE_TRADE_CMD][ELEMENT_DELIMITER]
                             * [BUY_ORDER]|[SELL_ORDER][ELEMENT_DELIMITER]
                             * account_id[ELEMENT_DELIMITER]
                             * symbol[ELEMENT_DELIMITER]
                             * shares
                             * Response: execution_price
                             */
                            scanner.skip(ProtocolConstants.ELEMENT_DELIMITER.toString());
                            final Order order;
                            final String orderType = scanner.next();
                            final String accountId = scanner.next();
                            final String symbol = scanner.next();
                            final int numberOfShares = scanner.nextInt();
                            if (orderType.equals(ProtocolConstants.BUY_ORDER)) {
                                order = new MarketBuyOrder(accountId, numberOfShares, symbol);
                            } else {
                                order = new MarketSellOrder(accountId, numberOfShares, symbol);
                            }
                            final int executionPrice = exchange.executeTrade(order);
                            writer.write(executionPrice);
                            writer.flush();
                            break;

                        default:
                            logger.log(Level.WARNING, "Unable to determine command for: " + cmdName);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Exception reading from input stream", e);
                }
            }
        }
    }
}
