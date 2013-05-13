package edu.uw.danco.broker;

import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.order.Order;
import org.springframework.jmx.export.UnableToRegisterMBeanException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/28/13
 * Time: 4:31 PM
 *
 * OrderProcessor implementation that executes orders through the broker.
 */
public class StockTraderOrderProcessor implements OrderProcessor {

    /** The logger */
    private static Logger LOGGER = Logger.getLogger(StockTraderOrderProcessor.class.getName());

    /** The AccountManager used by this processor */
    private AccountManager accountManager;

    /** The exchange used for the execution of orders */
    private StockExchange exchange;


    /**
     * Constructor
     * @param accountManager  - the account manager to be used to update account balances.
     * @param exchange - the exchange to be used for the execution of orders
     */
    public StockTraderOrderProcessor(final AccountManager accountManager, final StockExchange exchange) {
        this.accountManager = accountManager;
        this.exchange = exchange;
    }


    /**
     * Executes for order using the exchange
     * @param order - the order to process
     */
    @Override
    public void process(final Order order) {
        if (exchange.isOpen()) {
            try {
                accountManager.getAccount(order.getAccountId())
                        .reflectOrder(order, exchange.getQuote(order.getStockTicker()).getPrice());
                exchange.executeTrade(order);
            } catch (AccountException e) {
                LOGGER.log(Level.SEVERE, "Unable to get account for " + order.getAccountId(), e);
            }
        }
    }
}
