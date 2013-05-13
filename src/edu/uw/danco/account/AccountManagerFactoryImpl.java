package edu.uw.danco.account;

import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.account.AccountManagerFactory;
import edu.uw.ext.framework.dao.AccountDao;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/16/13
 * Time: 8:43 PM
 */
public class AccountManagerFactoryImpl implements AccountManagerFactory {

    /**
     * Instantiates a new account manager instance.
     * @param dao - the data access object to be used by the account manager
     * @return - a newly instantiated account
     */
    @Override
    public AccountManager newAccountManager(AccountDao dao) {
        AccountManager accountManager = new AccountManagerImpl(dao);
        return accountManager;
    }
}
