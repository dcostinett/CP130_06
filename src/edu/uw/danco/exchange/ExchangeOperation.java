package edu.uw.danco.exchange;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 6/2/13
 * Time: 9:18 AM
 *
 * Represents an exchange operation
 */
public abstract class ExchangeOperation {

    protected String command;
    protected String result;

    protected ExchangeOperation(final String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public String getResult() {
        return result;
    }

    public void setResult(final String result) {
        this.result = result;
    }
}
