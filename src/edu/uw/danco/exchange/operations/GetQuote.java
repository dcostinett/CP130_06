package edu.uw.danco.exchange.operations;

import edu.uw.danco.exchange.ExchangeOperation;
import edu.uw.danco.exchange.ProtocolConstants;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 6/2/13
 * Time: 9:47 AM
 */
public class GetQuote extends ExchangeOperation {

    public GetQuote(final String ticker) {
        super(ProtocolConstants.GET_QUOTE_CMD.toString() + ProtocolConstants.ELEMENT_DELIMITER + ticker);
    }

}
