package edu.uw.danco;

import static app.ExchangeFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import app.ExchangeFactory;

import edu.uw.ext.exchange.TestExchange;
import edu.uw.ext.framework.order.MarketBuyOrder;
import edu.uw.ext.framework.order.MarketSellOrder;
import edu.uw.ext.framework.order.Order;
import edu.uw.ext.framework.order.StopBuyOrder;
import edu.uw.ext.framework.order.StopSellOrder;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerException;
import edu.uw.ext.framework.broker.BrokerFactory;
import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.account.AccountManagerFactory;


/**
 * Tests for a Broker implementation.
 *
 * @author Russ Moul
 */
public final class BrokerTest {
    /** Two seconds in milliseconds */
    private static final int PROCESSING_DELAY = 500;

    /** Brokerage name. */
    private static final String BROKERAGE_NAME = "RTrade";

    /** Offset from the initial price to place orders at. */
    private static final int PRICE_DELTA = 5;

    /** Below initial price for BA (Boeing) */
    private static final int BELOW_INITIAL_PRICE_BA = INITIAL_PRICE_BA - PRICE_DELTA;

    /** Above initial price for BA (Boeing) */
    private static final int ABOVE_INITIAL_PRICE_BA = INITIAL_PRICE_BA + PRICE_DELTA;

    /** Above initial price for F (Ford) */
    private static final int ABOVE_INITIAL_PRICE_F = INITIAL_PRICE_F + PRICE_DELTA;

    /** Initial price for PG (Procter & Gamble) */
    private static final int INITIAL_PRICE_PG = 7279;

    /** Initial price for T (AT&T)*/
    private static final int INITIAL_PRICE_T = 1930;

    /** A small price adjustment*/
    private static final int SMALL_PRICE_OFFSET = 10;

    /** A large price adjustment*/
    private static final int LARGE_PRICE_OFFSET = 500;

    /** 10 shares */
    private static final int SHARES_10 = 10;

    /** 30 shares */
    private static final int SHARES_30 = 30;

    /** 100 shares */
    private static final int SHARES_100 = 100;

    /** 250 shares */
    private static final int SHARES_250 = 250;

    /** 400 shares */
    private static final int SHARES_400 = 400;

    /** Test account's name */
    private static final String ACCT_NAME = "fflintstone";

    /** Test account's password */
    private static final String PASSWORD = "password1";

    /** Test account's initial balance */
    private static final int INIT_BALANCE = 1000000;

    /** The market order count */
    private static final int MARKET_ORDER_COUNT = 3;

    /** The balance after the market orders have executed */
    private static final int AFTER_MARKET_ORDERS_BALANCE = 2150000;

    /** The balance after execution of stop buy order */
    private static final int AFTER_STOP_BUY_ORDERS_BALANCE = 2169750;

    /** The balance after execution of stop buy order */
    private static final int AFTER_STOP_SELL_ORDERS_BALANCE = 2239850;

    /** The orders to be placed */
    private Order[] expectedOrderSequence;

    /** The exchange used in the tests */
    private TestExchange exchange;

    /** The account manager used in the tests */
    private AccountManager accountManager;

    /** The account manager used in the tests */
    private BrokerFactory brokerFactory;

    /**
     * Initialize testing environment.
     *
     * @throws Exception if any exceptions are raised
     */
    @Before
    public void setUp() throws Exception {
        exchange = ExchangeFactory.newTestStockExchange();
        BeanFactory beanfactory = new FileSystemXmlApplicationContext("context.xml");
        AccountManagerFactory accountManagerFactory = beanfactory.getBean("AccountManagerFactory", AccountManagerFactory.class);

        brokerFactory = beanfactory.getBean("BrokerFactory", BrokerFactory.class);

        // create the account manager, dao, and broker
        DaoFactory daoFact;
        daoFact = beanfactory.getBean("DaoFactory", DaoFactory.class);

        AccountDao dao = daoFact.getAccountDao();
        dao.reset();
        accountManager = accountManagerFactory.newAccountManager(dao);

        Order[] tmp = {
                              new MarketSellOrder(ACCT_NAME, SHARES_400, SYMBOL_F),
                              new MarketBuyOrder(ACCT_NAME, SHARES_250, SYMBOL_BA),
                              new MarketSellOrder(ACCT_NAME, SHARES_100, SYMBOL_BA),
                              new StopSellOrder(ACCT_NAME, SHARES_30, SYMBOL_BA,
                                                       BELOW_INITIAL_PRICE_BA),
                              new StopBuyOrder(ACCT_NAME, SHARES_10, SYMBOL_F,
                                                      ABOVE_INITIAL_PRICE_F),
                              new StopBuyOrder(ACCT_NAME, SHARES_10, SYMBOL_BA,
                                                      ABOVE_INITIAL_PRICE_BA),

                              // These should never execute
                              new StopBuyOrder(ACCT_NAME, SHARES_10, SYMBOL_BA,
                                                      ABOVE_INITIAL_PRICE_BA + SMALL_PRICE_OFFSET),
                              new StopSellOrder(ACCT_NAME, SHARES_30, SYMBOL_BA,
                                                       BELOW_INITIAL_PRICE_BA - SMALL_PRICE_OFFSET),
                              new StopBuyOrder(ACCT_NAME, SHARES_30, SYMBOL_BA,
                                                      ABOVE_INITIAL_PRICE_BA + SMALL_PRICE_OFFSET),
                              new StopSellOrder(ACCT_NAME, SHARES_10, SYMBOL_BA,
                                                       BELOW_INITIAL_PRICE_BA - SMALL_PRICE_OFFSET),
                              new StopBuyOrder(ACCT_NAME, SHARES_10, SYMBOL_BA,
                                                      ABOVE_INITIAL_PRICE_BA + LARGE_PRICE_OFFSET),
                              new StopSellOrder(ACCT_NAME, SHARES_30, SYMBOL_BA,
                                                       BELOW_INITIAL_PRICE_BA - LARGE_PRICE_OFFSET),
        };
        expectedOrderSequence = tmp;
    }

    /**
     * Utility method to place some orders. The orders are intentionally placed
     * in an order inconsistant with their expected execution order.
     *
     * @param broker the boker to add orders with
     *
     * @throws Exception if any exceptions are raised
     */
    private void addOrders(final Broker broker) throws Exception {
        Logger logger = Logger.getLogger("");
        logger.info("Adding orders");
        broker.placeOrder((MarketSellOrder) expectedOrderSequence[2]);
        broker.placeOrder((StopBuyOrder) expectedOrderSequence[4]);
        broker.placeOrder((StopBuyOrder) expectedOrderSequence[5]);
        broker.placeOrder((MarketSellOrder) expectedOrderSequence[0]);
        broker.placeOrder((StopSellOrder) expectedOrderSequence[3]);

        broker.placeOrder((MarketBuyOrder) expectedOrderSequence[1]);
        broker.placeOrder((StopBuyOrder) expectedOrderSequence[6]);
        broker.placeOrder((StopSellOrder) expectedOrderSequence[7]);
        broker.placeOrder((StopBuyOrder) expectedOrderSequence[8]);
        broker.placeOrder((StopSellOrder) expectedOrderSequence[9]);
        broker.placeOrder((StopBuyOrder) expectedOrderSequence[10]);
        broker.placeOrder((StopSellOrder) expectedOrderSequence[11]);

        logger.info("Done adding orders");
    }

    /**
     * Utility method to compare the order processing history against the
     * planned order.
     *
     * @param planned the planned order execution list
     * @param actual the actual (history) order execution list
     * @param startPos the position in the lists to begin comparing from
     *
     * @return true if the expected and actual order histories are consistent
     */
    private boolean checkHistory(final Order[] planned, final Order[] actual,
                                 final int startPos) {
        for (int i = startPos; i < actual.length; i++) {
            if (planned[i].getOrderId() != actual[i].getOrderId()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Utility method to wait for a specified delay.
     *
     * @param duration length of the delay in milliseconds
     */
    private void delay(final int duration) {
        synchronized (this) {
            try {
                this.wait(duration);
            } catch (InterruptedException iex) {
                Logger logger = Logger.getLogger("");
                logger.info("Wait interrupted.");
            }
        }
    }

    /**
     * Tests the requestQuote method.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testRequestQuote() throws Exception {
        Broker broker = brokerFactory.newBroker(BROKERAGE_NAME, accountManager,
                                                       exchange);

        StockQuote quote = broker.requestQuote("BA");
        assertEquals("BA", quote.getTicker());
        assertEquals(INITIAL_PRICE_BA, quote.getPrice());
    }

    /**
     * Tests the deleteAccount method.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testDeleteAccount() throws Exception {
        Broker broker = brokerFactory.newBroker(BROKERAGE_NAME, accountManager,
                                                       exchange);

        Account acct1 = broker.createAccount(ACCT_NAME, PASSWORD, INIT_BALANCE);
        Account acct2 = broker.getAccount(ACCT_NAME, PASSWORD);
        assertEquals(acct1.getBalance(), acct2.getBalance());
        broker.deleteAccount(ACCT_NAME);

        try {
            broker.getAccount(ACCT_NAME, PASSWORD);
            fail("Retrieve of deleted account should not succeed.");
        } catch (BrokerException ex) {
            Logger logger = Logger.getLogger("");
            logger.info("testDeleteAccount threw exception when getting "
                                + "deleted account.");
        }
    }

    /**
     * Tests the getAccount method.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testGetAccount() throws Exception {
        Broker broker = brokerFactory.newBroker(BROKERAGE_NAME, accountManager,
                                                       exchange);

        Account acct1 = broker.createAccount(ACCT_NAME, PASSWORD, INIT_BALANCE);
        Account acct2 = broker.getAccount(ACCT_NAME, PASSWORD);
        assertEquals(acct1.getBalance(), acct2.getBalance());

        try {
            broker.getAccount(ACCT_NAME, "badpassword");
            fail("Retrieve of account with bad password should not succeed.");
        } catch (BrokerException ex) {
            Logger logger = Logger.getLogger("");
            logger.info(
                               "testGetAccount threw exception in response to bad password.");
        }
    }

    /**
     * Tests the broker when the exchange is closed when the broker initializes.
     *
     * @throws Exception if any exceptions are raised,
     */
    @Test
    public void testBrokerExchangeClosed() throws Exception {
        Broker broker = brokerFactory.newBroker(BROKERAGE_NAME, accountManager,
                                                       exchange);
        Account acct = broker.createAccount(ACCT_NAME, PASSWORD, INIT_BALANCE);

        addOrders(broker);

        // Wait for queued market orders to process
        delay(PROCESSING_DELAY);

        //check history should be empty
        Order[] history;
        history = exchange.getExecutionHistory();

        if (history.length != 0) {
            fail("The exchange is closed. "
                         + "It should not be sent orders for execution.");
        }

        // open the exchange
        exchange.open();
        delay(PROCESSING_DELAY);

        //check history should match expected results
        history = exchange.getExecutionHistory();

        if (history.length != MARKET_ORDER_COUNT) {
            Logger logger = Logger.getLogger(BrokerTest.class.getName());
            logger.log(Level.SEVERE, String.format("Expected %d, actual %d", MARKET_ORDER_COUNT, history.length));
            fail("Market orders should be processed once the market opens.");
        }

        if (!checkHistory(expectedOrderSequence, history, 0)) {
            fail("Processing of all market orders should be prioritized by "
                         + "quantity.");
        }

        acct = broker.getAccount(ACCT_NAME, PASSWORD);

        if (acct.getBalance() != AFTER_MARKET_ORDERS_BALANCE) {
            fail("Once the order is executed by the exchange, "
                         + "the account balance should be adjusted."
                         + " Balance is " + acct.getBalance() + " should be "
                         + AFTER_MARKET_ORDERS_BALANCE + ".");
        }

        baseTest(broker);
    }

    /**
     * Tests the broker when the exchange is open when the broker initializes.
     *
     * @throws Exception if any exceptions are raised,
     */
    @Test
    public void testBrokerExchangeOpen() throws Exception {
        exchange.open();

        Broker broker = brokerFactory.newBroker(BROKERAGE_NAME, accountManager,
                                                       exchange);
        Account acct = broker.createAccount(ACCT_NAME, PASSWORD, INIT_BALANCE);

        addOrders(broker);

        // Wait for queued market orders to process
        delay(PROCESSING_DELAY);

        //check history should match expected results
        Order[] history = exchange.getExecutionHistory();

        if (history.length != MARKET_ORDER_COUNT) {
            fail("Market orders should be processed, the market is open.");
        }

        // can't verify order of first three
        // depends on threading, may be executed in the order placed
        acct = broker.getAccount(ACCT_NAME, PASSWORD);

        if (acct.getBalance() != AFTER_MARKET_ORDERS_BALANCE) {
            fail("Once the order is executed by the Exchange, "
                         + "the account balance should be adjusted."
                         + " Balance is " + acct.getBalance() + " should be "
                         + AFTER_MARKET_ORDERS_BALANCE + ".");
        }

        baseTest(broker);
    }

    /**
     * The base test for the broker, submits orders and verifies their
     * execution.
     *
     * @param broker the broker to conduct the test with
     *
     * @throws Exception if any exceptions are raised,
     */
    private void baseTest(final Broker broker) throws Exception {
        // adjust stock price
        exchange.adjustPrice(SYMBOL_BA, BELOW_INITIAL_PRICE_BA);
        // Wait for orders to process
        delay(PROCESSING_DELAY);

        // verify execution of StopBuyOrder
        Order[] history;
        history = exchange.getExecutionHistory();

        int expectedLength = MARKET_ORDER_COUNT + 1;

        if (history.length != expectedLength) {
            fail("StopSellOrders should be processed when the price "
                         + "drops below or meets the target price.");
        }

        if (!checkHistory(expectedOrderSequence, history,
                                 MARKET_ORDER_COUNT)) {
            fail("Orders should react only to their stocks price adjustment.");
        }

        Account acct = broker.getAccount(ACCT_NAME, PASSWORD);

        if (acct.getBalance() != AFTER_STOP_SELL_ORDERS_BALANCE) {
            fail("Once the order is executed by the exchange, "
                         + "the account balance should be adjusted."
                         + " Balance is " + acct.getBalance() + " should be "
                         + AFTER_STOP_SELL_ORDERS_BALANCE + ".");
        }

        // adjust stock price
        exchange.adjustPrice("F", ABOVE_INITIAL_PRICE_F);
        // Wait for orders to process
        delay(PROCESSING_DELAY);

        // verify execution of StopBuyOrder
        history = exchange.getExecutionHistory();

        expectedLength++;

        if (history.length != expectedLength) {
            fail("StopSellOrders should be processed when the price "
                         + "drops below or meets the target price.");
        }

        if (!checkHistory(expectedOrderSequence, history,
                                 MARKET_ORDER_COUNT)) {
            fail("Orders should react only to their stocks price adjustment.");
        }

        // adjust stock price
        exchange.adjustPrice(SYMBOL_BA, ABOVE_INITIAL_PRICE_BA);
        // Wait for orders to process
        delay(PROCESSING_DELAY);

        // verify execution of StopSellOrder
        history = exchange.getExecutionHistory();

        expectedLength++;

        if ((history.length != expectedLength)
                    || !checkHistory(expectedOrderSequence, history,
                                            MARKET_ORDER_COUNT)) {
            fail("StopBuyOrders should be processed when the price "
                         + "meets or exceeds the target price.");
        }

        acct = broker.getAccount(ACCT_NAME, PASSWORD);

        if (acct.getBalance() != AFTER_STOP_BUY_ORDERS_BALANCE) {
            fail("Once the order is executed by the exchange, "
                         + "the account balance should be adjusted."
                         + " Balance is " + acct.getBalance() + " should be "
                         + AFTER_STOP_BUY_ORDERS_BALANCE + ".");
        }

        // close the exchange
        exchange.close();
        broker.placeOrder(new MarketBuyOrder(ACCT_NAME, SHARES_250, SYMBOL_BA));
        // Wait for orders to process
        delay(PROCESSING_DELAY);

        // verify the broker is respecting the closure of the exchange
        history = exchange.getExecutionHistory();

        if (history.length != expectedLength) {
            fail("Once the exchange is closed. "
                         + "It should not longer be sent orders for execution.");
        }
    }

    /**
     * Tests the close method.  Just verify no exception is thrown.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testClose() throws Exception {
        Broker broker = brokerFactory.newBroker(BROKERAGE_NAME, accountManager,
                                                       exchange);
        broker.close();
    }
}

