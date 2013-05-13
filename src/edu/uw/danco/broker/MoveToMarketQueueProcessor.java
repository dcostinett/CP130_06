package edu.uw.danco.broker;

import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/28/13
 * Time: 2:59 PM
 *
 * Moves orders to a brokers market order queue.
 */
public class MoveToMarketQueueProcessor implements OrderProcessor {

    /** The queue for orders */
    private OrderQueue<Order> marketQueue;


    /**
     * Constructor
     * @param marketQueue - the queue the orders will be moved to
     */
    public MoveToMarketQueueProcessor(final OrderQueue<Order> marketQueue) {
        this.marketQueue = marketQueue;
    }


    /**
     * Enqueues the order into the market order queue.
     * @param order - the order to process
     */
    @Override
    public void process(final Order order) {
        marketQueue.enqueue(order);
    }
}
