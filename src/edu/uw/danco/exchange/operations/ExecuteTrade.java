package edu.uw.danco.exchange.operations;

import edu.uw.danco.exchange.ExchangeOperation;
import edu.uw.danco.exchange.ProtocolConstants;
import edu.uw.ext.framework.order.Order;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 6/2/13
 * Time: 2:02 PM
 *
 * Request:  [EXECUTE_TRADE_CMD][ELEMENT_DELIMITER][BUY_ORDER]|[SELL_ORDER][ELEMENT_DELIMITER]account_id[ELEMENT_DELIMITER]symbol[ELEMENT_DELIMITER]shares
 * Response: execution_price
 *
 */
public class ExecuteTrade extends ExchangeOperation {

        /*
        */

    public ExecuteTrade(final Order order) {
        super(ProtocolConstants.EXECUTE_TRADE_CMD.toString() +
                ProtocolConstants.ELEMENT_DELIMITER +
                (order.isBuyOrder() ? ProtocolConstants.BUY_ORDER : ProtocolConstants.SELL_ORDER) +
                ProtocolConstants.ELEMENT_DELIMITER +
                order.getAccountId() +
                ProtocolConstants.ELEMENT_DELIMITER +
                order.getStockTicker() +
                ProtocolConstants.ELEMENT_DELIMITER +
                order.getNumberOfShares()
        );
    }
}
