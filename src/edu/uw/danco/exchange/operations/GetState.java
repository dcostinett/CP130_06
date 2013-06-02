package edu.uw.danco.exchange.operations;

import edu.uw.danco.exchange.ExchangeOperation;
import edu.uw.danco.exchange.ProtocolConstants;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 6/2/13
 * Time: 1:15 PM
 */
public class GetState extends ExchangeOperation {
    public GetState() {
        super(ProtocolConstants.GET_STATE_CMD.toString());
    }
}
