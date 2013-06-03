package edu.uw.danco.exchange;

import java.security.PrivateKey;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/21/13
 * Time: 10:05 AM
 *
 * Constants for the command strings composing the exchange protocol. The protocol supports events and commands. The
 * events and commands are represented by enumerated values, other constants are simply static final variables.
 *
 * Events are one way messages sent from the exchange to the broker(s).
 * The protocol supports the following events:
 * Event: [OPEN_EVENT]
 * -
 * Event: [CLOSED_EVENT]
 * -
 * Event: [PRICE_CHANGE_EVENT][ELEMENT_DELIMITER]symbol[ELEMENT_DELIMITER]price
 *
 *
 * Commands conform to a request/response model where requests are sent from a broker and the result is a response
 * sent to the requesting broker from the exchange.
 *
 * The protocol supports the following commands (all requests sent on single line -- no newlines):
 * Request:  [GET_STATE_CMD]
 * Response: [OPEN_STATE]|[CLOSED_STATE]
 * -
 * Request:  [GET_TICKERS_CMD]
 * Response: symbol[ELEMENT_DELIMITER]symbol...
 * -
 * Request:  [GET_QUOTE_CMD][ELEMENT_DELIMITER]symbol
 * Response: price
 * -
 * Request:  [EXECUTE_TRADE_CMD][ELEMENT_DELIMITER][BUY_ORDER]|[SELL_ORDER]
 *           [ELEMENT_DELIMITER]account_id[ELEMENT_DELIMITER] symbol[ELEMENT_DELIMITER]shares
 * Response: execution_price

 */
public enum ProtocolConstants {
    OPEN_EVENT("OPEN_EVENT"),
    CLOSED_EVENT("CLOSED_EVENT"),
    PRICE_CHANGE_EVENT("PRICE_CHANGE_EVENT"),

    EXECUTE_TRADE_CMD("EXECUTE_TRADE_CMD"),
    GET_QUOTE_CMD("GET_QUOTE_CMD"),
    GET_STATE_CMD("GET_STATE_CMD"),
    GET_TICKERS_CMD("GET_TICKERS_CMD"),

    OPEN_STATE("OPEN_STATE"),
    CLOSED_STATE("CLOSED_EVENT"),

    BUY_ORDER("BUY_ORDER"),
    SELL_ORDER("SELL_ORDER"),

    ELEMENT_DELIMITER(":");


    private final String value;

    private ProtocolConstants(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
