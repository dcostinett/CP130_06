package edu.uw.danco.exchange;

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
 * Event: [OPEN_EVNT]
 * -
 * Event: [CLOSED_EVNT]
 * -
 * Event: [PRICE_CHANGE_EVNT][ELEMENT_DELIMITER]symbol[ELEMENT_DELIMITER]price
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
}
