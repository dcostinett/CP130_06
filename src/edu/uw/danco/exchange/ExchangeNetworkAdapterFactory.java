package edu.uw.danco.exchange;

import edu.uw.ext.framework.exchange.ExchangeAdapter;
import edu.uw.ext.framework.exchange.NetworkExchangeAdapterFactory;
import edu.uw.ext.framework.exchange.StockExchange;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/21/13
 * Time: 9:53 AM
 *
 * A NetworkExchangeAdapterFactory implementation for creating ExchangeNetworkAdapter instances.
 */
public class ExchangeNetworkAdapterFactory implements NetworkExchangeAdapterFactory {

    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(ExchangeNetworkAdapterFactory.class.getName());


    /**
     * Constructor returns an instance of an ExchangeNetworkAdapter
     * @param exchange - the underlying real exchange
     * @param multicastIP - the multicast ip address used to distribute events
     * @param multicastPort - the port used to distribute events
     * @param commandPort - the listening port to be used to accept command requests
     * @return - a newly instantiated ExchangeNetworkAdapter, or null if instantiation fails
     */
    @Override
    public ExchangeAdapter newAdapter(final StockExchange exchange, final String multicastIP,
                                      final int multicastPort, final int commandPort) {
        ExchangeAdapter networkAdapter = null;
        try {
            networkAdapter = new ExchangeNetworkAdapter(exchange, multicastIP, multicastPort, commandPort);
        } catch (final UnknownHostException e) {
            LOGGER.log(Level.SEVERE, "Unable to resolve address for " + multicastIP, e);
        } catch (final SocketException e) {
            LOGGER.log(Level.SEVERE, String.format("Unable to open socket to %s:%d", multicastIP, multicastPort), e);
        }

        return networkAdapter;
    }
}
