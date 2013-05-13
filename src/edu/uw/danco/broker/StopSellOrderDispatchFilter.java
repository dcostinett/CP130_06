package edu.uw.danco.broker;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.order.StopSellOrder;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/28/13
 * Time: 5:00 PM
 *
 * Dispatch filter that dispatches any orders having a price above the current market price (threshold).
 */
public class StopSellOrderDispatchFilter extends OrderDispatchFilter<Integer, StopSellOrder> {

    /**
     * Cosntructor
     * @param initPrice - the initial price
     */
    public StopSellOrderDispatchFilter(final int initPrice) {
        setThreshold(initPrice);
    }


    /**
     * Test the provided order against the threshold
     * @param order - the order to be tested for dispatch
     * @return - true if the order to be tested is above or equal to the threshold
     */
    @Override
    public boolean check(final StopSellOrder order) {
        return order.getPrice() >= getThreshold();
    }
}
