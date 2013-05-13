package edu.uw.danco;

/*****************************************************************************
 * Replace this import with an import of your OrderManager implementation    *
 * class.                                                                    *
 *****************************************************************************/
import edu.uw.danco.broker.OrderManagerImpl;
import edu.uw.ext.framework.broker.OrderManager;

import test.AbstractOrderManagerTest;

/**
 * Concrete subclass of AbstractOrderManagerTest, provides an implementation of
 * createOrderManager which creates  an instance of "my" OrderManager
 * implementation class.
 */
public class OrderManagerTest extends AbstractOrderManagerTest {
    /**
     * Creates an instance of "my" OrderManger implementation class.
     *
     * @param ticker the ticker symbol of the stock the order manager is to manage
     * @param initPrice the initial price of the stock being managed
     * 
     * @return a new OrderManager instance
     */
    protected final OrderManager createOrderManager(final String ticker, final int initPrice) {
        /*********************************************************************
         * This needs to be an instance of your OrderManager implementation  *
         * class.                                                            *
         *********************************************************************/
        return new OrderManagerImpl(ticker, initPrice);

    }
}
