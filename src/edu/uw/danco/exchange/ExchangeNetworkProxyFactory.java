package edu.uw.danco.exchange;

import edu.uw.ext.framework.exchange.ExchangeAdapter;
import edu.uw.ext.framework.exchange.ExchangeEvent;
import edu.uw.ext.framework.exchange.NetworkExchangeProxyFactory;
import edu.uw.ext.framework.exchange.StockExchange;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/21/13
 * Time: 9:53 AM
 *
 * A factory interface for creating ExchangeNetworkProxy instances.
 */
public class ExchangeNetworkProxyFactory implements NetworkExchangeProxyFactory {

    /**
     * Instantiates a network enabled ExchangeNetworkProxy
     * @param multicastIP - the multicast ip address used to distribute events
     * @param multicastPort - the port used to distribute events
     * @param commandIP - the exchange host
     * @param commandPort - the listening port to be used to accept command requests
     * @return - a newly instantiated ExchangeProxy
     */
    @Override
    public StockExchange newProxy(final String multicastIP, final int multicastPort,
                                  final String commandIP, final int commandPort) {
        ExchangeNetworkProxy networkProxy =
                new ExchangeNetworkProxy(multicastIP, multicastPort, commandIP, commandPort);

        return networkProxy;
    }
}
