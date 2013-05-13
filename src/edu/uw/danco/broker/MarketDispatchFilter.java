package edu.uw.danco.broker;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.order.Order;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/28/13
 * Time: 2:54 PM
 *
 * Dispatch filter that dispatches orders as long as the market is open. The threshold object will be a Boolean object
 * indicating the state of the market.
 */
public class MarketDispatchFilter extends OrderDispatchFilter<Boolean, Order> {

    /**
     * Constructor
     * @param marketState - the initial state of the market
     */
    public MarketDispatchFilter(boolean marketState) {
        setThreshold(marketState);
    }


    /**
     * Test if the order may be dispatched
     * @param order - the order to be tested for dispatch
     * @return - the threshold
     */
    public boolean check(final Order order) {
        return getThreshold();
    }
}
