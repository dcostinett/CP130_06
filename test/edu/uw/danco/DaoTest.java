package edu.uw.danco;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountFactory;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;


/**
 * Defines JUnit tests for the AccountDao and DaoFactory implementations.
 *
 * @author Russ Moul
 */
public final class DaoTest {
    /* Common account information */

    /** The city of Beadrock */
    private static final String BEDROCK_CITY = "Bedrock";

    /** Arizona state code */
    private static final String ARIZONA_STATE_CODE = "AZ";

    /** The Bedrock ZIP code */
    private static final String BEDROCK_ZIP = "86046";

    /** Flintstone's account phone number */
    private static final String FLINTSTONE_PHONE_NUMBER = "(123) 567-8900";

    /** Flintstone's street address */
    private static final String FLINTSTONE_STREET_ADDRESS = "101 Stoney Lane";

    /* Fred's account info */

    /** Fred's account name */
    private static final String FRED_ACCOUNT_NAME = "fflintstone";

    /** Fred's account password */
    private static final byte[] FRED_ACCOUNT_PASSWD = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};

    /** Fred's initial account balance */
    private static final int FRED_ACCOUNT_INIT_BALANCE = 100000;

    /** Fred's account full name */
    private static final String FRED_ACCOUNT_FULL_NAME = "Fred Flintstone";

    /** Fred's account email address */
    private static final String FRED_EMAIL_ADDRESS = "fred@slate-rock.com";

    /** Fred's credit card issuer */
    private static final String FRED_CREDIT_CARD_ISSUER = "Marble Stone Bank";

    /** Fred's credit card holder name */
    private static final String FRED_CREDIT_CARD_HOLDER = "Fredrick Flintstone";

    /** Fred's credit card number */
    private static final String FRED_CREDIT_CARD_NUM = "1234-5678-9012-3456";

    /** Fred's credit card expiration date */
    private static final String FRED_CREDIT_CARD_EXPIRES = "03/05";

    /* Wilma's account info */

    /** Wilma's account name */
    private static final String WILMA_ACCOUNT_NAME = "wflintstone";

    /** Wilma's account password */
    private static final byte[] WILMA_ACCOUNT_PASSWD = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '2'};

    /** Wilma's initial account balance */
    private static final int WILMA_ACCOUNT_INIT_BALANCE = 500000;

    /** Wilma's account full name */
    private static final String WILMA_ACCOUNT_FULL_NAME = "Wilma Flintstone";

    /** Wilma's account phone number */
    private static final String WILMA_EMAIL_ADDRESS = "wilma@yabadabado.com";

    /** Wilma's credit card issuer */
    private static final String WILMA_CREDIT_CARD_ISSUER = "Granite Bank";

    /** Wilma's credit card holder name */
    private static final String WILMA_CREDIT_CARD_HOLDER = "W Flintstone";

    /** Wilma's credit card number */
    private static final String WILMA_CREDIT_CARD_NUM = "5678-9012-3456-1234";

    /** Wilma's credit card expiration date */
    private static final String WILMA_CREDIT_CARD_EXPIRES = "02/06";

    /* Barney's account info */

    /** Barney's account name */
    private static final String BARNEY_ACCOUNT_NAME = "b_rubble";

    /** Barney's account password */
    private static final byte[] BARNEY_ACCOUNT_PASSWD = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd', '3'};

    /** Barney's initial account balance */
    private static final int BARNEY_ACCOUNT_INIT_BALANCE = 200000;

    /** Barney's account full name */
    private static final String BARNEY_ACCOUNT_FULL_NAME = "Barney Rubble";

    /** Barney's account phone number */
    private static final String BARNEY_PHONE_NUMBER = "(987) 654-3210";

    /** Barney's account email address */
    private static final String BARNEY_EMAIL_ADDRESS = "barney@slate-rock.com";

    /** Barney's account street address */
    private static final String BARNEY_STREET_ADDRESS = "103 Stoney Lane";

    /** Barney's credit card issuer */
    private static final String BARNEY_CREDIT_CARD_ISSUER = "First Rock Bank";

    /** Barney's credit card holder name */
    private static final String BARNEY_CREDIT_CARD_HOLDER = "Barney Rubble";

    /** Barney's credit card number */
    private static final String BARNEY_CREDIT_CARD_NUM = "7890-1234-5678-9000";

    /** Barney's credit card expiration date */
    private static final String BARNEY_CREDIT_CARD_EXPIRES = "04/06";

    /* Mr. Slate's account info */

    /** Mr. Slate's account name */
    private static final String MR_SLATE_ACCOUNT_NAME = "mr_slate";

    /** Mr. Slate's account password */
    private static final byte[] MR_SLATE_ACCOUNT_PASSWD = {'\r', '\n', '\t', ',', ' ', '=', ':', '"', '.'};

    /** Mr. Slate's initial account balance */
    private static final int MR_SLATE_ACCOUNT_INIT_BALANCE = 500000;

    /** Mr. Slate's account full name */
    private static final String MR_SLATE_ACCOUNT_FULL_NAME = "Mr. Slate";

    /** Mr. Slate's account phone number */
    private static final String MR_SLATE_PHONE_NUMBER = "(210) 987-6543";

    /** Mr. Slate's account email address */
    private static final String MR_SLATE_EMAIL_ADDRESS = "boss@slate-rock.com";

    /** Fred's account for testing */
    private Account fredAcct;

    /** Wilma's account for testing */
    private Account wilmaAcct;

    /** Barney's account for testing */
    private Account barneyAcct;

    /** Mr. Slate's account for testing */
    private Account mrSlateAcct;

    /** Account factory used for testing */
    private AccountFactory accountFactory;

    /** DAO factory used for testing */
    private DaoFactory daoFactory;

    /** Spring bean factory. */
    private BeanFactory beanfactory;

    /**
     * Initialize the DaoFactory and create test accounts.
     *
     * @throws Exception if initialization fails
     */
    @Before
    public void setUp() throws Exception {
        beanfactory = new FileSystemXmlApplicationContext("context.xml");
        accountFactory = beanfactory.getBean("AccountFactory", AccountFactory.class);

        daoFactory = beanfactory.getBean("DaoFactory", DaoFactory.class);

        accountFactory = beanfactory.getBean("AccountFactory", AccountFactory.class);
        if (accountFactory == null) {
            throw new Exception("Unable to create account factory!");
        }

        // Create test accounts
        Address addr;
        CreditCard card;

        // Fred
        fredAcct = accountFactory.newAccount(FRED_ACCOUNT_NAME,
                                                    FRED_ACCOUNT_PASSWD,
                                                    FRED_ACCOUNT_INIT_BALANCE);
        if (fredAcct == null) {
            throw new Exception("Factory unable to create account!");
        }
        fredAcct.setFullName(FRED_ACCOUNT_FULL_NAME);
        fredAcct.setPhone(FLINTSTONE_PHONE_NUMBER);
        fredAcct.setEmail(FRED_EMAIL_ADDRESS);

        addr = beanfactory.getBean("Address", Address.class);
        addr.setStreetAddress(FLINTSTONE_STREET_ADDRESS);
        addr.setCity(BEDROCK_CITY);
        addr.setState(ARIZONA_STATE_CODE);
        addr.setZipCode(BEDROCK_ZIP);
        fredAcct.setAddress(addr);

        card = beanfactory.getBean("CreditCard", CreditCard.class);
        card.setIssuer(FRED_CREDIT_CARD_ISSUER);
        card.setHolder(FRED_CREDIT_CARD_HOLDER);
        card.setAccountNumber(FRED_CREDIT_CARD_NUM);
        card.setExpirationDate(FRED_CREDIT_CARD_EXPIRES);
        fredAcct.setCreditCard(card);

        // Wilma
        wilmaAcct = accountFactory.newAccount(WILMA_ACCOUNT_NAME,
                                                     WILMA_ACCOUNT_PASSWD, WILMA_ACCOUNT_INIT_BALANCE);
        wilmaAcct.setFullName(WILMA_ACCOUNT_FULL_NAME);
        wilmaAcct.setPhone(FLINTSTONE_PHONE_NUMBER);
        wilmaAcct.setEmail(WILMA_EMAIL_ADDRESS);

        addr = beanfactory.getBean("Address", Address.class);
        addr.setStreetAddress(FLINTSTONE_STREET_ADDRESS);
        addr.setCity(BEDROCK_CITY);
        addr.setState(ARIZONA_STATE_CODE);
        addr.setZipCode(BEDROCK_ZIP);
        wilmaAcct.setAddress(addr);

        card = beanfactory.getBean("CreditCard", CreditCard.class);
        card.setIssuer(WILMA_CREDIT_CARD_ISSUER);
        card.setHolder(WILMA_CREDIT_CARD_HOLDER);
        card.setAccountNumber(WILMA_CREDIT_CARD_NUM);
        card.setExpirationDate(WILMA_CREDIT_CARD_EXPIRES);
        wilmaAcct.setCreditCard(card);

        // Barney
        barneyAcct = accountFactory.newAccount(BARNEY_ACCOUNT_NAME,
                                                      BARNEY_ACCOUNT_PASSWD, BARNEY_ACCOUNT_INIT_BALANCE);
        barneyAcct.setFullName(BARNEY_ACCOUNT_FULL_NAME);
        barneyAcct.setPhone(BARNEY_PHONE_NUMBER);
        barneyAcct.setEmail(BARNEY_EMAIL_ADDRESS);

        addr = beanfactory.getBean("Address", Address.class);
        addr.setStreetAddress(BARNEY_STREET_ADDRESS);
        addr.setCity(BEDROCK_CITY);
        addr.setState(ARIZONA_STATE_CODE);
        addr.setZipCode(BEDROCK_ZIP);
        barneyAcct.setAddress(addr);

        card = beanfactory.getBean("CreditCard", CreditCard.class);
        card.setIssuer(BARNEY_CREDIT_CARD_ISSUER);
        card.setHolder(BARNEY_CREDIT_CARD_HOLDER);
        card.setAccountNumber(BARNEY_CREDIT_CARD_NUM);
        card.setExpirationDate(BARNEY_CREDIT_CARD_EXPIRES);
        barneyAcct.setCreditCard(card);

        // MrSlate
        mrSlateAcct = accountFactory.newAccount(MR_SLATE_ACCOUNT_NAME,
                                                       MR_SLATE_ACCOUNT_PASSWD, MR_SLATE_ACCOUNT_INIT_BALANCE);
        mrSlateAcct.setFullName(MR_SLATE_ACCOUNT_FULL_NAME);
        mrSlateAcct.setPhone(MR_SLATE_PHONE_NUMBER);
        mrSlateAcct.setEmail(MR_SLATE_EMAIL_ADDRESS);
        /* No address or creditcard info for Mr. Slate */
    }

    /**
     * Compares two accounts.
     *
     * @param expected account having the expected values
     * @param actual  account having the actual values
     */
    private void compareAccounts(final Account expected, final Account actual) {
        assertEquals(expected.getName(), actual.getName());
        assertTrue(Arrays.equals(expected.getPasswordHash(), actual.getPasswordHash()));
        assertEquals(expected.getBalance(), actual.getBalance());
        assertEquals(expected.getFullName(), actual.getFullName());

        assertEquals(expected.getPhone(), actual.getPhone());
        assertEquals(expected.getEmail(), actual.getEmail());

        Address expectedAddr = expected.getAddress();
        Address actualAddr = actual.getAddress();
        if (expectedAddr == null) {
            assertNull(actualAddr);
        } else {
            assertEquals(expectedAddr.getStreetAddress(), actualAddr.getStreetAddress());
            assertEquals(expectedAddr.getCity(), actualAddr.getCity());
            assertEquals(expectedAddr.getState(), actualAddr.getState());
            assertEquals(expectedAddr.getZipCode(), actualAddr.getZipCode());
        }

        CreditCard expectedCc = expected.getCreditCard();
        CreditCard actualCc = actual.getCreditCard();
        if (expectedCc == null) {
            assertNull(actualCc);
        } else {
            assertEquals(expectedCc.getIssuer(), actualCc.getIssuer());
            assertEquals(expectedCc.getHolder(), actualCc.getHolder());
            assertEquals(expectedCc.getAccountNumber(), actualCc.getAccountNumber());
            assertEquals(expectedCc.getExpirationDate(), actualCc.getExpirationDate());
        }
    }

    /**
     * Tests the setAccount and getAccount methods.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testSetGet() throws Exception {
        AccountDao dao = daoFactory.getAccountDao();
        dao.setAccount(fredAcct);
        dao.setAccount(wilmaAcct);
        dao.setAccount(barneyAcct);
        dao.setAccount(mrSlateAcct);

        Account acct;
        acct = dao.getAccount(FRED_ACCOUNT_NAME);
        compareAccounts(fredAcct, acct);
        acct = dao.getAccount(WILMA_ACCOUNT_NAME);
        compareAccounts(wilmaAcct, acct);
        acct = dao.getAccount(BARNEY_ACCOUNT_NAME);
        compareAccounts(barneyAcct, acct);
        acct = dao.getAccount(MR_SLATE_ACCOUNT_NAME);
        compareAccounts(mrSlateAcct, acct);

    }

    /**
     * Tests the storing and loading of accounts.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testReload() throws Exception {
        AccountDao dao = daoFactory.getAccountDao();
        dao.setAccount(fredAcct);
        dao.setAccount(wilmaAcct);
        dao.setAccount(barneyAcct);
        dao.setAccount(mrSlateAcct);

        dao = daoFactory.getAccountDao();

        Account acct;
        acct = dao.getAccount(FRED_ACCOUNT_NAME);
        compareAccounts(fredAcct, acct);
        acct = dao.getAccount(WILMA_ACCOUNT_NAME);
        compareAccounts(wilmaAcct, acct);
        acct = dao.getAccount(BARNEY_ACCOUNT_NAME);
        compareAccounts(barneyAcct, acct);
        acct = dao.getAccount(MR_SLATE_ACCOUNT_NAME);
        compareAccounts(mrSlateAcct, acct);
    }

    /**
     * Tests the deleteAccount method.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testDelete() throws Exception {
        AccountDao dao = daoFactory.getAccountDao();
        dao.setAccount(fredAcct);
        dao.setAccount(wilmaAcct);
        dao.setAccount(barneyAcct);
        dao.setAccount(mrSlateAcct);

        Account acct;
        acct = dao.getAccount(WILMA_ACCOUNT_NAME);
        compareAccounts(wilmaAcct, acct);
        dao.deleteAccount(WILMA_ACCOUNT_NAME);
        acct = dao.getAccount(WILMA_ACCOUNT_NAME);
        assertNull(acct);
    }

    /**
     * Tests the reset method.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testReset() throws Exception {
        AccountDao dao = daoFactory.getAccountDao();
        dao.setAccount(fredAcct);
        dao.setAccount(wilmaAcct);
        dao.setAccount(barneyAcct);
        dao.setAccount(mrSlateAcct);

        dao.getAccount(WILMA_ACCOUNT_NAME);

        dao.reset();

        Account acct;
        acct = dao.getAccount(FRED_ACCOUNT_NAME);
        assertNull(acct);
        acct = dao.getAccount(WILMA_ACCOUNT_NAME);
        assertNull(acct);
        acct = dao.getAccount(BARNEY_ACCOUNT_NAME);
        assertNull(acct);
        acct = dao.getAccount(MR_SLATE_ACCOUNT_NAME);
        assertNull(acct);
    }

    /**
     * Tests the close method.  Just verify no exception is thrown.
     *
     * @throws Exception if any exceptions are raised
     */
    @Test
    public void testClose() throws Exception {
        AccountDao dao = daoFactory.getAccountDao();
        dao.setAccount(fredAcct);
        dao.setAccount(wilmaAcct);
        dao.setAccount(barneyAcct);
        dao.setAccount(mrSlateAcct);
        dao.getAccount(WILMA_ACCOUNT_NAME);

        dao.close();
    }
}

