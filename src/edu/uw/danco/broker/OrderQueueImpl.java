package edu.uw.danco.broker;

import edu.uw.ext.framework.broker.OrderDispatchFilter;
import edu.uw.ext.framework.broker.OrderProcessor;
import edu.uw.ext.framework.broker.OrderQueue;
import edu.uw.ext.framework.order.Order;

import java.util.Comparator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/28/13
 * Time: 3:05 PM
 *
 * A simple OrderQueue implementation backed by a BlockingQueue.
 *
 */
public final class OrderQueueImpl<E extends Order> implements OrderQueue<E>, Runnable {

    /** The logger for this class */
    private static final Logger LOGGER = Logger.getLogger(OrderQueueImpl.class.getName());

    /** Backing store for orders */
    private BlockingQueue<E> queue;
    // can continue to use a TreeSt

    /** The processor used during order processing */
    private OrderProcessor orderProcessor;

    /** The dispatch filter used to control dispatching from this queue */
    private OrderDispatchFilter<?, E> filter;

    /** Dispatcher that handles dispatching orders in an executor thread */
    private final ExecutorService dispatcher;

    // ** Lock to protect access to queue while modifying queue data */
    private final Lock queuelock = new ReentrantLock();

    /** Boolean to determine if the order getting processed has been queued to the active pool */
    private AtomicBoolean isQueuedToPool = new AtomicBoolean(false);

    /**
     * Constructor
     * @param orderComparator - Comparator to be used for ordering
     * @param filter - the dispatch filter used to control dispatching from this queue
     */
    public OrderQueueImpl(final Comparator<E> orderComparator,
                          final OrderDispatchFilter<?, E> filter,
                          final ExecutorService dispatcher) {
        queue = new PriorityBlockingQueue<E>(20, orderComparator);
        this.filter = filter;
        this.dispatcher = dispatcher;
        this.filter.setOrderQueue(this);
    }


    /**
     * Constructor
     * @param filter - the dispatch filter used to control dispatching from this queue
     */
    public OrderQueueImpl(final OrderDispatchFilter<?, E> filter, final ExecutorService dispatcher) {
        queue = new PriorityBlockingQueue<E>();
        this.filter = filter;
        this.dispatcher = dispatcher;
        this.filter.setOrderQueue(this);
    }


    /**
     * Adds the specified order to the queue. Subsequent to adding the order dispatches any dispatchable orders.
     * @param order - the order to be added to the queue
     */
    @Override
    public void enqueue(final E order) {
        queuelock.lock();
        try {
            if (!queue.contains(order)) {
                queue.add(order);
            }
        } finally {
            queuelock.unlock();
        }
        dispatchOrders();
    }


    /**
     * Removes the highest dispatchable order in the queue. If there are orders in the queue but they do not meet the
     * dispatch threshold order will not be removed and null will be returned.
     * @return - the first dispatchable order in the queue, or null if there are no dispatchable orders in the queue
     */
    @Override
    public E dequeue() {
        E order = null;
        queuelock.lock();
        try {
            if (!queue.isEmpty()) {
                if (filter != null && filter.check(queue.peek())) {
                    try {
                        order = queue.take();
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "Interrupted waiting for queue element", e);
                    }
                }
            }
        } finally {
            queuelock.unlock();
        }
        return order;
    }


    /**
     * Executes the orderProcessor for each dispatchable order. Each dispatchable order is in turn removed from the
     * queue and passed to the callback. If no callback is registered the order is simply removed from the queue.
     */
    @Override
    public void dispatchOrders() {
        queuelock.lock();
        try {
            if (isQueuedToPool.compareAndSet(false, true)) {
                dispatcher.execute(this);
            }
        } finally {
            queuelock.unlock();
        }
    }


    /**
     * Registers the callback to be used during order processing.
     * @param proc - the callback to be registered
     */
    @Override
    public void setOrderProcessor(final OrderProcessor proc) {
        orderProcessor = proc;
    }


    /**
     * Dispatcher process
     */
    @Override
    public void run() {
        while (true) {
            queuelock.lock();
            Order order;
            try {
                order = dequeue();
                if (order == null) {
                    isQueuedToPool.set(false);
                    break;
                }
            } finally {
                queuelock.unlock();
            }
            OrderProcessor op = orderProcessor;
            if (op != null) {
                op.process(order);
            }
        }
    }
}
