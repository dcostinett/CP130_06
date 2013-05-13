package edu.uw.danco;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountManager;
import edu.uw.ext.framework.account.AccountManagerFactory;
import edu.uw.ext.framework.order.MarketSellOrder;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;


/**
 * Defines JUnit tests for the AccountManager class.
 *
 * @author Russ Moul
 */
public final class AccountManagerTest {
    /** Class' logger. */
    private static Logger logger =
            Logger.getLogger(AccountManagerTest.class.getName());

    /** Fred test account name. */
    private static final String FRED_ACCOUNT_NAME = "fflinstone";

    /** Bad test account name. */
    private static final String BAD_ACCOUNT_NAME = "f_flinstone";

    /** Good Fred test password. */
    private static final String FRED_GOOD_PASSWORD = "password2";

    /** Bad Fred test password. */
    private static final String FRED_BAD_PASSWORD = "password";

    /** Wilma test account name. */
    private static final String WILMA_ACCOUNT_NAME = "wflintstone";

    /** Wilma test password. */
    private static final String WILMA_GOOD_PASSWORD = "pebbles";

    /** Betty test account name. */
    private static final String BETTY_ACCOUNT_NAME = "b_rubble";

    /** Betty test password. */
    private static final String BETTY_GOOD_PASSWORD = "betty1";

    /** One thousand dollars in cents. */
    private static final int ONE_THOUSAND_DOLLARS_IN_CENTS = 100000;

    /** Five thousand dollars in cents. */
    private static final int FIVE_THOUSAND_DOLLARS_IN_CENTS = 500000;

    /** An execution price used for testing. */
    private static final int TEST_EXECUTION_PRICE = 10000;

    /** The primary account manager instance used for the tests. */
    private AccountManager accountManager;

    /** The account manager factory used for the tests. */
    private AccountManagerFactory accountManagerFactory;

    /** The account DAO used for the tests. */
    private AccountDao dao;

    /** Spring bean factory. */
    private BeanFactory beanfactory;

    /**
     * Initialize the Account manager and create two new accounts for use in
     * testing.
     *
     * @throws Exception if an exception is raised
     */
    @Before
    public void setUp() throws Exception {
        beanfactory = new FileSystemXmlApplicationContext("context.xml");

        accountManagerFactory = beanfactory.getBean("AccountManagerFactory", AccountManagerFactory.class);

        // initialize the account manager
        setUpAccountManager();
        dao.reset();

        // create two test accounts
        accountManager.createAccount(FRED_ACCOUNT_NAME, FRED_GOOD_PASSWORD,
                                            ONE_THOUSAND_DOLLARS_IN_CENTS);
        accountManager.createAccount(WILMA_ACCOUNT_NAME, WILMA_GOOD_PASSWORD,
                                            FIVE_THOUSAND_DOLLARS_IN_CENTS);
    }

    /**
     * Initialize an account manager.
     *
     * @throws Exception if an exception is raised
     */
    private void setUpAccountManager() throws Exception {
        // create the account manager, storage, and broker
        DaoFactory fact = beanfactory.getBean("DaoFactory", DaoFactory.class);

        dao = fact.getAccountDao();

        accountManager = accountManagerFactory.newAccountManager(dao);
    }

    /**
     * Test that persist() works correctly
     *
     * @throws Exception if an exception is raised
     */
    @Test
    public void testPersist() throws Exception {
        Account account = accountManager.getAccount(FRED_ACCOUNT_NAME);
        account.setBalance(FIVE_THOUSAND_DOLLARS_IN_CENTS);
        accountManager.persist(account);

        accountManager.close();
        setUpAccountManager();
        account = accountManager.getAccount(FRED_ACCOUNT_NAME);
        assertEquals(FIVE_THOUSAND_DOLLARS_IN_CENTS, account.getBalance());
    }

    /**
     * Test that validateLogin() works correctly
     *
     * @throws Exception if an exception is raised
     */
    @Test
    public void testValidate() throws Exception {
        assertTrue(accountManager.validateLogin(FRED_ACCOUNT_NAME,
                                                       FRED_GOOD_PASSWORD));
        assertTrue(!accountManager.validateLogin(FRED_ACCOUNT_NAME,
                                                        FRED_BAD_PASSWORD));
        assertTrue(!accountManager.validateLogin(BAD_ACCOUNT_NAME,
                                                        FRED_GOOD_PASSWORD));
        assertTrue(accountManager.validateLogin(WILMA_ACCOUNT_NAME,
                                                       WILMA_GOOD_PASSWORD));
    }

    /**
     * Test that getAccount() works correctly
     *
     * @throws Exception if an exception is raised
     */
    @Test
    public void testGetAccount() throws Exception {
        Account account = accountManager.getAccount(FRED_ACCOUNT_NAME);
        assertEquals(FRED_ACCOUNT_NAME, account.getName());
        assertEquals(ONE_THOUSAND_DOLLARS_IN_CENTS, account.getBalance());
    }

    /**
     * Test with multiple accounts
     *
     * @throws Exception if an exception is raised
     */
    @Test
    public void testMultipleGetAccount() throws Exception {
        accountManager.createAccount(BETTY_ACCOUNT_NAME, WILMA_GOOD_PASSWORD,
                                            ONE_THOUSAND_DOLLARS_IN_CENTS);

        Account fredAccount = accountManager.getAccount(FRED_ACCOUNT_NAME);
        assertEquals(FRED_ACCOUNT_NAME, fredAccount.getName());
        assertEquals(ONE_THOUSAND_DOLLARS_IN_CENTS, fredAccount.getBalance());

        Account barneyAccount = accountManager.getAccount(BETTY_ACCOUNT_NAME);
        assertEquals(BETTY_ACCOUNT_NAME, barneyAccount.getName());
        assertEquals(ONE_THOUSAND_DOLLARS_IN_CENTS, barneyAccount.getBalance());
    }

    /**
     * Test that the information is saved and loaded correctly
     *
     * @throws Exception if an exception is raised
     */
    @Test
    public void testReloadFile() throws Exception {
        setUpAccountManager();

        Account account = accountManager.getAccount(FRED_ACCOUNT_NAME);
        assertEquals(FRED_ACCOUNT_NAME, account.getName());
        assertEquals(ONE_THOUSAND_DOLLARS_IN_CENTS, account.getBalance());
    }

    /**
     * Test deleteAccount()
     *
     * @throws Exception if an exception is raised
     */
    @Test
    public void testDeleteAccount() throws Exception {
        accountManager.deleteAccount(FRED_ACCOUNT_NAME);

        Account account = accountManager.getAccount(FRED_ACCOUNT_NAME);
        assertEquals(null, account);
    }

    /**
     * Make sure that creating an account with an existing username fails
     *
     * @throws Exception if an exception is raised
     */
    @Test
    public void testCreateDuplicateUsername() throws Exception {
        try {
            accountManager.createAccount(FRED_ACCOUNT_NAME,
                                                FRED_GOOD_PASSWORD, ONE_THOUSAND_DOLLARS_IN_CENTS);
            fail("Should have thrown AccountException");
        } catch (AccountException e) {
            logger.info(
                               "testCreateDuplicateUsername threw exception as expected");
        }
    }

    /**
     * Tests the close method.  Just verify no exception is thrown.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testClose() throws Exception {
        accountManager.close();
    }

    /**
     * Test account manager registration.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testRegistration() throws Exception {
        Account account = accountManager.getAccount(FRED_ACCOUNT_NAME);
        int balance = account.getBalance();
        MarketSellOrder order = new MarketSellOrder(account.getName(), 1, "BA");
        try {
            account.reflectOrder(order, TEST_EXECUTION_PRICE);
            assertEquals(balance + TEST_EXECUTION_PRICE, account.getBalance());
        } catch (NullPointerException npe) {
            logger.info("Account.reflectOrder() not yet implemented");
        }
    }
}

