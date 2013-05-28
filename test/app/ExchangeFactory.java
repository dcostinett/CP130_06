package app;

import edu.uw.ext.exchange.TestExchange;
import edu.uw.ext.framework.account.AccountManagerFactory;
import edu.uw.ext.framework.broker.BrokerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.HashMap;

public class ExchangeFactory {
    /** Symbol for BA (Boeing) */
    public static final String SYMBOL_BA = "BA";

    /** Initial price for BA (Boeing) */
    public static final int INITIAL_PRICE_BA = 3000;

    /** Symbol for F (Ford) */
    public static final String SYMBOL_F = "F";

    /** Initial price for F (Ford) */
    public static final int INITIAL_PRICE_F = 4000;

    /** Above initial price for F (Ford) */
    public static final int ABOVE_INITIAL_PRICE_F = 4005;

    /** Symbol for GE (General Electric) */
    public static final String SYMBOL_GE = "GE";

    /** Initial price for GE (General Electric) */
    public static final int INITIAL_PRICE_GE = 3720;

    /** Symbol for GM (General Motors) */
    public static final String SYMBOL_GM = "GM";

    /** Initial price for GM (General Motors) */
    public static final int INITIAL_PRICE_GM = 4290;

    /** Symbol for HWP (Hewlett Packard) */
    public static final String SYMBOL_HWP = "HWP";

    /** Initial price for HWP (Hewlett Packard) */
    public static final int INITIAL_PRICE_HWP = 1605;

    /** Symbol for IBM (IBM) */
    public static final String SYMBOL_IBM = "IBM";

    /** Initial price for IBM (IBM) */
    public static final int INITIAL_PRICE_IBM = 9172;

    /** Symbol for MU (Micron Technology) */
    public static final String SYMBOL_MU = "MU";

    /** Initial price for MU (Micron Technology) */
    public static final int INITIAL_PRICE_MU = 1883;

    /** Symbol for PFE (PFIZER)*/
    public static final String SYMBOL_PFE = "PFE";

    /** Initial price for PFE (PFIZER)*/
    public static final int INITIAL_PRICE_PFE = 4010;

    /** Symbol for PG (Procter & Gamble) */
    public static final String SYMBOL_PG = "PG";

    /** Initial price for PG (Procter & Gamble) */
    public static final int INITIAL_PRICE_PG = 7279;

    /** Symbol for T (AT&T)*/
    public static final String SYMBOL_T = "T";

    /** Initial price for T (AT&T)*/
    public static final int INITIAL_PRICE_T = 1930;

    public static TestExchange newTestStockExchange() {
        BeanFactory beanfactory = new FileSystemXmlApplicationContext("context.xml");
        AccountManagerFactory accountManagerFactory = beanfactory.getBean("AccountManagerFactory", AccountManagerFactory.class);

        BrokerFactory brokerFactory = beanfactory.getBean("BrokerFactory", BrokerFactory.class);

        // create exchange
        HashMap<String, Integer> stocks = new HashMap<String, Integer>();
        stocks.put(SYMBOL_BA, INITIAL_PRICE_BA);
        stocks.put(SYMBOL_F, INITIAL_PRICE_F);
        stocks.put(SYMBOL_GE, INITIAL_PRICE_GE);
        stocks.put(SYMBOL_GM, INITIAL_PRICE_GM);
        stocks.put(SYMBOL_HWP, INITIAL_PRICE_HWP);
        stocks.put(SYMBOL_IBM, INITIAL_PRICE_IBM);
        stocks.put(SYMBOL_MU, INITIAL_PRICE_MU);
        stocks.put(SYMBOL_PFE, INITIAL_PRICE_PFE);
        stocks.put(SYMBOL_PG, INITIAL_PRICE_PG);
        stocks.put(SYMBOL_T, INITIAL_PRICE_T);

        return new TestExchange(stocks);
    }
}
