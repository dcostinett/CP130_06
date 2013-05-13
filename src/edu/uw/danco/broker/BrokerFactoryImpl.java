package edu.uw.danco.broker;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerFactory;
import edu.uw.ext.framework.exchange.StockExchange;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/28/13
 * Time: 2:14 PM
 */
public class BrokerFactoryImpl implements BrokerFactory {

    @Override
    public Broker newBroker(String name, AccountManager acctMngr, StockExchange exch) {
        BrokerImpl broker = new BrokerImpl(name, acctMngr, exch);
        return broker;
    }
}
