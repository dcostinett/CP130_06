package edu.uw.danco.exchange;

import edu.uw.ext.framework.exchange.ExchangeEvent;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 5/27/13
 * Time: 12:37 PM
 */
public class NetEventProcessor {

    public static String GetEventString(final ExchangeEvent event) {
        StringBuilder sb = new StringBuilder();

        if (event.getEventType().equals(ExchangeEvent.EventType.CLOSED)) {
            sb.append(ProtocolConstants.CLOSED_EVENT);
        } else if (event.getEventType().equals(ExchangeEvent.EventType.OPENED)) {
            sb.append(ProtocolConstants.OPEN_EVENT);
        } else if (event.getEventType().equals(ExchangeEvent.EventType.PRICE_CHANGED)) {
            sb.append(ProtocolConstants.PRICE_CHANGE_EVENT);
            sb.append(ProtocolConstants.ELEMENT_DELIMITER);
            sb.append(event.getTicker());
            sb.append(ProtocolConstants.ELEMENT_DELIMITER);
            sb.append(event.getPrice());
        }

        return sb.toString();
    }

    public static String GetQuoteFor(final String ticker) {
        StringBuilder sb = new StringBuilder();

        sb.append(ProtocolConstants.GET_QUOTE_CMD);
        sb.append(ProtocolConstants.ELEMENT_DELIMITER);
        sb.append(ticker);

        return sb.toString();
    }
}
