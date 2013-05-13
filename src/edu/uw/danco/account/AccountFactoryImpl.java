package edu.uw.danco.account;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountFactory;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/13/13
 * Time: 6:17 PM
 */
public class AccountFactoryImpl implements AccountFactory {
    private static final Logger LOGGER = Logger.getLogger(AccountFactoryImpl.class.getName());


    /**
     * Creates a new account
     * @param accountName
     * @param hashedPassword
     * @param initialBalance
     * @return
     */
    @Override
    public Account newAccount(final String accountName,
                              final byte[] hashedPassword,
                              final int initialBalance) {
        AccountImpl account = null;

        try {
            account = new AccountImpl(accountName, hashedPassword,
                                            initialBalance);
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Created account: '" + accountName
                                    + "', balance = " + initialBalance);
            }
        } catch (final AccountException ex) {
            final String msg = "Account creation failed for , account '"
                                       + accountName + "', balance = " + initialBalance;
            LOGGER.log(Level.WARNING, msg, ex);
        }

        return account;
    }
}
