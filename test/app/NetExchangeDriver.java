
package app;

import edu.uw.ext.exchange.SimpleExchange;
import edu.uw.ext.framework.exchange.ExchangeAdapter;
import edu.uw.ext.framework.exchange.NetworkExchangeAdapterFactory;
import edu.uw.ext.framework.exchange.StockExchangeSpi;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.prefs.Preferences;


/**
 * Test driver for the network ExchangeAdapter.
 *
 * @author Russ Moul
 */
public final class NetExchangeDriver {
    /** This class' logger. */
    private static final Logger LOG =
                         Logger.getLogger(NetExchangeDriver.class.getName());

    /**
     * Private constructor prevents instantiation.
     */
    private NetExchangeDriver() {
        super();
    }

    /**
     * Initialize OrderManager to be exercised.
     *
     * @param args (not used)
     *
     * @throws Exception if any exceptions are raised
     */
    public static void main(final String[] args) throws Exception {
        Preferences prefs;
        String eventIp;
        int eventPort;
        int cmdPort;

        // load preferences file
        prefs = Preferences.userNodeForPackage(NetExchangeDriver.class);
        eventIp = prefs.get(NetExchangeTestConstants.EVENT_IP_PREF,
                NetExchangeTestConstants.EVENT_IP_DEFAULT);
        eventPort = prefs.getInt(NetExchangeTestConstants.EVENT_PORT_PREF,
                NetExchangeTestConstants.EVENT_PORT_DEFAULT);
        cmdPort = prefs.getInt(NetExchangeTestConstants.COMMAND_PORT_PREF,
                NetExchangeTestConstants.COMMAND_PORT_DEFAULT);
        LOG.info("Preferences loaded.");

        // initialize the factories
        /** Spring bean factory. */
        BeanFactory beanFactory = new FileSystemXmlApplicationContext("context.xml");
        NetworkExchangeAdapterFactory factory;
        factory = beanFactory.getBean("NetworkExchangeAdapterFactory", NetworkExchangeAdapterFactory.class);

        LOG.info("Factories initialized.");

        StockExchangeSpi exchange =
                         new SimpleExchange(new File("exchange.dat"), true);
        LOG.info("Exchange initialized.");

        System.out.println("#");
        System.out.println("#");
        System.out.println("#");
        System.out.println("# Press [Enter] to open the exchange");
        System.out.println("#       [Q-Enter] to quit");
        System.out.println("# Once running press [Enter] "
                         + "to close the exchange");
        System.out.println("#");
        System.out.println("#");
        System.out.println("#");

        ExchangeAdapter adapter = factory.newAdapter(exchange, eventIp, eventPort, cmdPort);

        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            String s = in.readLine();

            if ((null != s && s.length() > 0)
                   && ((s.charAt(0) == 'Q') || (s.charAt(0) == 'q'))) {
                break;
            }

            exchange.open();
            System.out.println("#");
            System.out.println("#");
            System.out.println("#");
            System.out.println("# Press [Enter] to close the exchange");
            System.out.println("#");
            System.out.println("#");
            System.out.println("#");
            in.readLine();
            exchange.close();
            System.out.println("#");
            System.out.println("#");
            System.out.println("#");
            System.out.println("# Press [Enter] to open the exchange");
            System.out.println("#       [Q-Enter] to quit");
            System.out.println("# Once running press [Enter] "
                             + "to close the exchange");
            System.out.println("#");
            System.out.println("#");
            System.out.println("#");
        }

        // Release the adapter resources
        adapter.close();

        System.exit(0);
    }
}

