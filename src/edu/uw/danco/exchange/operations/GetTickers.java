package edu.uw.danco.exchange.operations;

import edu.uw.danco.exchange.ExchangeOperation;
import edu.uw.danco.exchange.ProtocolConstants;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 6/2/13
 * Time: 1:05 PM
 */
public class GetTickers extends ExchangeOperation {
    public GetTickers() {
        super(ProtocolConstants.GET_TICKERS_CMD.toString());
    }
}
