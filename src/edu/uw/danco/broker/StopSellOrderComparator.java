package edu.uw.danco.broker;

import edu.uw.ext.framework.order.StopSellOrder;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/28/13
 * Time: 4:57 PM
 *
 * Comparator which places the orders in descending order - the highest priced order is the first item in the list.
 */
public final class StopSellOrderComparator implements Comparator<StopSellOrder> {

    /**
     * Private constructor to prevent instantiation
     */
    private StopSellOrderComparator() {}

    /** Instance for use in Singleton pattern */
    public static final StopSellOrderComparator INSTANCE = new StopSellOrderComparator();

    /**
     * Performs the comparison
     * @param o1 - first of two orders to be compared
     * @param o2 - second of two orders to be compared
     * @return - a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
     * greater than the second. Where the lesser order has the the higher price, if prices are equal the lesser order
     * will have the highest order quantity, finally if price and quantity are equal the lesser order will have the
     * lower order id.
     */
    @Override
    public int compare(final StopSellOrder o1, final StopSellOrder o2) {
        int result;

        //could also use Integer's compare to method
        result = o1.getPrice() > o2.getPrice() ? -1 : o1.getPrice() < o2.getPrice() ? 1 : 0;

        if (result == 0) {
            // Russ implemented Comparable and uses o1.compareTo(o2); -- this encapsulates both the number and orderId
            result = o1.compareTo(o2);
        }

        return result;
    }
}
