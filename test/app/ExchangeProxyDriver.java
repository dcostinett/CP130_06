package app;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.account.AccountManagerFactory;
import edu.uw.ext.framework.broker.Broker;
import edu.uw.ext.framework.broker.BrokerFactory;
import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;
import edu.uw.ext.framework.exchange.NetworkExchangeProxyFactory;
import edu.uw.ext.framework.exchange.StockExchange;
import edu.uw.ext.framework.exchange.StockQuote;
import edu.uw.ext.framework.order.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.prefs.Preferences;


/**
 * Test driver for the NetworkExchangeProxy implementation.
 *
 * @author Russ Moul
 */
public final class ExchangeProxyDriver {
    /** This class' logger. */
    private static final Logger LOG =
                         Logger.getLogger(ExchangeProxyDriver.class.getName());

    /** Brokerage name. */
    private static final String BROKERAGE_NAME = "RTrade";

    /** Fred test account name. */
    private static final String ACCOUNT_NAME = "fflinstone";

    /** Good Fred test password. */
    private static final String ACCOUNT_PASSWORD = "password1";

    /** One thousand dollars in cents. */
    private static final int TEN_THOUSAND_DOLLARS_IN_CENTS = 1000000;

    /** Price offset */
    private static final int PRICE_OFFSET = 5;

    /** 10 shares */
    private static final int SHARES_10 = 10;

    /** 30 shares */
    private static final int SHARES_30 = 30;

    /** 250 shares */
    private static final int SHARES_250 = 250;

    /** 400 shares */
    private static final int SHARES_400 = 400;

    /** Symbol for BA (Boeing) */
    private static final String SYMBOL_BA = "BA";

    /** Symbol for F (Ford) */
    private static final String SYMBOL_F = "F";

    /**
     * Private constructor to prevent instantiation.
     */
    private ExchangeProxyDriver() {
        super();
    }

    /**
     * Tests the exchange proxy.
     *
     * @param args (not used)
     *
     * @throws Exception if any exceptions are raised
     */
    public static void main(final String[] args)
        throws Exception {
        Preferences prefs;
        String eventIp;
        int eventPort;
        String cmdIp;
        int cmdPort;

        DaoFactory daoFact;
        AccountDao dao;

        Broker broker;
        Order[] orders;
        StockExchange exchange;
        AccountManager mAcctMngr;
        BrokerFactory mBrokerFactory;

        // get the preferences
        prefs = Preferences.userNodeForPackage(ExchangeProxyDriver.class);
        eventIp = prefs.get(NetExchangeTestConstants.EVENT_IP_PREF,
                NetExchangeTestConstants.EVENT_IP_DEFAULT);
        eventPort = prefs.getInt(NetExchangeTestConstants.EVENT_PORT_PREF,
                NetExchangeTestConstants.EVENT_PORT_DEFAULT);
        cmdIp = prefs.get(NetExchangeTestConstants.COMMAND_IP_PREF,
                NetExchangeTestConstants.COMMAND_IP_DEFAULT);
        cmdPort = prefs.getInt(NetExchangeTestConstants.COMMAND_PORT_PREF,
                NetExchangeTestConstants.COMMAND_PORT_DEFAULT);
        LOG.info("Preferences loaded.");

        // initialize the factories
        BeanFactory beanfactory = new FileSystemXmlApplicationContext("context.xml");
        Object obj = beanfactory.getBean("AccountManagerFactory", AccountManagerFactory.class);

        AccountManagerFactory mAccountManagerFactory;
        mAccountManagerFactory = (AccountManagerFactory) obj;

        obj = beanfactory.getBean("BrokerFactory", BrokerFactory.class);
        mBrokerFactory = (BrokerFactory) obj;

        obj = beanfactory.getBean("NetworkExchangeProxyFactory", NetworkExchangeProxyFactory.class);
        LOG.info("Factories initialized.");

        NetworkExchangeProxyFactory proxyFact;
        proxyFact = (NetworkExchangeProxyFactory) obj;

        exchange = proxyFact.newProxy(eventIp, eventPort, cmdIp, cmdPort);
        LOG.info("Connected to exchange proxy.");

        // create the account manager, dao, and broker
        daoFact = (DaoFactory) beanfactory.getBean("DaoFactory", DaoFactory.class);
        dao = daoFact.getAccountDao();
        dao.reset();
        LOG.info("DAO initialized.");

        mAcctMngr = mAccountManagerFactory.newAccountManager(dao);
        LOG.info("Account manager initialized.");

        broker = mBrokerFactory.newBroker(BROKERAGE_NAME, mAcctMngr, exchange);
        LOG.info("Broker initialized.");

        broker.createAccount(ACCOUNT_NAME, ACCOUNT_PASSWORD,
                             TEN_THOUSAND_DOLLARS_IN_CENTS);

        // test getting a quote
        StockQuote quote = broker.requestQuote(SYMBOL_BA);
        LOG.info("Obtained quote for " + SYMBOL_BA + ".");

        broker.placeOrder(new MarketBuyOrder(ACCOUNT_NAME,
                                             SHARES_250, SYMBOL_BA));
        broker.placeOrder(new MarketSellOrder(ACCOUNT_NAME,
                                              SHARES_400, SYMBOL_F));
        LOG.info("Placed market buy and sell orders.");

        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("#");
        System.out.println("#");
        System.out.println("#");
        System.out.println("# Open the exchange... "
                         + "queued market orders should execute, then...");

        System.out.println("# Press [Enter] to add stop orders and continue");
        System.out.println("#");
        System.out.println("#");
        System.out.println("#");
        in.readLine();

        int price;

        // place orders for Boeing
        quote = broker.requestQuote(SYMBOL_BA);
        price = quote.getPrice();
        broker.placeOrder(new StopBuyOrder(ACCOUNT_NAME, SHARES_10, SYMBOL_BA,
                price - PRICE_OFFSET));
        broker.placeOrder(new StopSellOrder(ACCOUNT_NAME, SHARES_30, SYMBOL_BA,
                price + PRICE_OFFSET));
        LOG.info("Placed stop buy and sell orders for "
                         + SYMBOL_BA + ".");

        // place orders for Ford
        quote = broker.requestQuote("F");
        price = quote.getPrice();
        broker.placeOrder(new StopBuyOrder(ACCOUNT_NAME, SHARES_10, SYMBOL_F,
                price - PRICE_OFFSET));
        broker.placeOrder(new StopSellOrder(ACCOUNT_NAME, SHARES_30, SYMBOL_F,
                price + PRICE_OFFSET));
        LOG.info("Placed stop buy and sell orders for "
                         + SYMBOL_F + ".");

        System.out.println("#");
        System.out.println("#");
        System.out.println("#");
        System.out.println(
            "# Watch the exchange to verify the orders execute then,");
        System.out.println("# Press [Enter] to exit");
        System.out.println("#");
        System.out.println("#");
        System.out.println("#");
        in.readLine();
        System.exit(0);
    }
}

