package edu.uw.danco.account;

import edu.uw.danco.dao.AccountDaoImpl;
import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.AccountFactory;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.dao.AccountDao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/16/13
 * Time: 8:48 PM
 */
public class AccountManagerImpl implements AccountManager {

    private static final Logger LOGGER = Logger.getLogger(AccountManagerImpl.class.getName());

    /** THe hashing algorithm */
    public static final String ALGORITHM = "SHA1";

    /** The data access object */
    private final AccountDao dao;

    /** The account factory */
    private final AccountFactory accountFactory;

    /**
     * Instatiates a new AccountManager
     * @param dao
     */
    public AccountManagerImpl(final AccountDao dao) {
        this.dao = dao;

        accountFactory = new AccountFactoryImpl();
    }

    /**
     * Default constructor to support JavaBean instantiation
     */
    public AccountManagerImpl() {
        this.dao = new AccountDaoImpl();

        accountFactory = new AccountFactoryImpl();
    }

    /**
     * Used to persist an account
     * @param account - the account to persist
     * @throws AccountException - if operation fails
     */
    @Override
    public void persist(final Account account) throws AccountException {
        dao.setAccount(account);
    }

    /**
     * Lookup the account based on the accountname
     * @param accountName - the name of the desired account
     * @return - the account if located otherwise null
     * @throws AccountException - if operation fails
     */
    @Override
    public Account getAccount(final String accountName) throws AccountException {
        final Account account = dao.getAccount(accountName);

        if (account != null) {
            account.registerAccountManager(this);
        }

        return account;
    }

    /**
     * Remove the account
     * @param accountName - the name of the account to remove
     * @throws AccountException - if operation fails
     */
    @Override
    public void deleteAccount(final String accountName) throws AccountException {
        dao.deleteAccount(accountName);
    }

    /**
     * Creates an account. The creation process should include persisting the account and setting the account
     * manager reference (through the Account registerAccountManager operation).
     * @param accountName - the name for account to add
     * @param password - the password used to gain access to the account
     * @param balance - the initial balance of the account
     * @return - the newly created account
     * @throws AccountException - if operation fails
     */
    @Override
    public Account createAccount(final String accountName, final String password, int balance) throws AccountException {
        Account account = null;
        account = dao.getAccount(accountName);
        if (account != null) {
            throw new AccountException(String.format("Account %s already exists", accountName));
        }
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(password.getBytes());

            account = accountFactory.newAccount(accountName, md.digest(), balance);
            account.registerAccountManager(this);
            dao.setAccount(account);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Unable to create SHA1 hash for password", e);
        }

        return account;
    }

    /**
     *
     * @param accountName - name of account the password is to be validated for
     * @param password - password is to be validated
     * @return - true if password is valid for account identified by username
     * @throws AccountException - if error occurs accessing accounts
     */
    @Override
    public boolean validateLogin(final String accountName, final String password) throws AccountException {
        boolean isPasswordMatch = false;
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(password.getBytes());                     // in general use the getBytes method that takes an encoding type

            Account account = dao.getAccount(accountName);
            if (account != null) {
                isPasswordMatch = Arrays.equals(account.getPasswordHash(), (md.digest()));
                //MessageDigest has an isEquals method
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Unable to crete message digest for password", e);
            throw new AccountException(e.getMessage());
        }
        return isPasswordMatch;
    }

    /**
     * Release any resources used by the AccountManager implementation. Once closed
     * further operations on the AccountManager may fail.
     * @throws AccountException - if error occurs accessing accounts
     */
    @Override
    public void close() throws AccountException {
        dao.close();
    }
}
