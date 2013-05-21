package edu.uw.danco.account;

import edu.uw.ext.framework.account.*;
import edu.uw.ext.framework.order.Order;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/13/13
 * Time: 6:13 PM
 */
public class AccountImpl implements Account {
    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(AccountImpl.class.getName());

    /** The minimum allowed account length */
    private static int minAcctLen;

    /** The minimum allowed initial account balance */
    private static int minAcctBal;

    static {
        final Preferences prefs = Preferences.userNodeForPackage(Account.class);
        minAcctLen = prefs.getInt("minAccountLength", 0);
        minAcctBal = prefs.getInt("minAccountBalance", 0);
        LOGGER.info("minAccountLength " + minAcctLen);
        LOGGER.info("minAccountBalance " + minAcctBal);
    }

    /** Name of account holder */
    private String name;

    /** Account balance */
    private int balance;

    /** Full name of account holder */
    private String fullName;

    /** Address of account holder */
    private Address address;

    /** Phone number for account holder */
    private String phone;

    /** Email address for account holder */
    private String email;

    /** Credit card number of account holder */
    private CreditCard creditCard;

    /** Hash of account holder's password */
    private byte[] passwordHash;

    /** The account manager */
    private AccountManager accountManager;

    /**
     * Default constructor for bean support
     */
    public AccountImpl() {
    }


    /**
     * Constructor, validates length of account name and the balance based on
     * the preferences.
     *
     * @param acctName the account name
     * @param passwordHash the password hash
     * @param balance the balance
     *
     * @throws AccountException if the account name is too short or balance
     *                          too low
     */
    public AccountImpl(final String acctName, final byte[] passwordHash,
                         final int balance)
            throws AccountException {
        if (!editAccountName(acctName)) {
            final String msg = "Account creation failed for , account '" + acctName
                                       + "' invalid account name";
            LOGGER.warning(msg);
            throw new AccountException(msg);
        }

        if (balance < minAcctBal) {
            final String msg = "Account creation failed for , account '" + acctName
                                       + "' minimum balance not met, " + balance;
            LOGGER.warning(msg);
            throw new AccountException(msg);
        }

        name = acctName;
        final byte[] copy = new byte[passwordHash.length];
        System.arraycopy(passwordHash, 0, copy, 0, passwordHash.length);
        this.passwordHash = copy;
        this.balance = balance;
    }


    /**
     * Gets the account name
     * @return - the name of the account
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Sets the name. This operation is not generally used but is provided for JavaBean conformance.
     * @param name - the value to be set for the account name
     * @throws AccountException - if the account name is unacceptable
     */
    @Override
    public void setName(final String name) throws AccountException {
        if (!editAccountName(name)) {
            final String msg = "Account name '" + name + "' is unacceptable.";
            LOGGER.warning(msg);
            throw new AccountException(msg);
        }

        this.name = name;
    }


    /**
     * Edits the account name.
     *
     * @param acctName the value to be edited
     *
     * @return true if the provided value is acceptable
     */
    private boolean editAccountName(final String acctName) {
        return (acctName != null) && (acctName.length() >= minAcctLen);
    }

    /**
     * Gets the hashed password.
     * @return - the hashed password
     */
    @Override
    public byte[] getPasswordHash() {
        return passwordHash;
    }


    /**
     * Sets the hashed password
     * @param passwordHash - the value to be stored for the password hash
     */
    @Override
    public void setPasswordHash(final byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }


    /**
     * Gets the account balance
     * @return - the current balance of the account
     */
    @Override
    public int getBalance() {
        return balance;
    }


    /**
     * Sets the account balance.
     * @param balance - the account balance.
     */
    @Override
    public void setBalance(final int balance) {
        Preferences prefs = Preferences.userNodeForPackage(Account.class);
        if (balance < prefs.getInt("minAccountBalance", 0)) {

        }
        this.balance = balance;
    }


    /**
     * Gets the full name of the account holder
     * @return - the account holder's full name
     */
    @Override
    public String getFullName() {
        return fullName;
    }


    /**
     * Sets the full name of the account holder
     * @param fullName - the full name of the account holder
     */
    @Override
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }


    /**
     * Gets the account address
     * @return - the address for the account holder
     */
    @Override
    public Address getAddress() {
        return address;
    }


    /**
     * Sets the address of the account holder
     * @param address - the address value to use
     */
    @Override
    public void setAddress(final Address address) {
        if (isEmptyAddress(address)) {
            this.address = null;
        } else {
        this.address = address;
        }
    }

    private boolean isEmptyAddress(Address address) {
        return (address.getStreetAddress() == null || address.getStreetAddress().length() == 0) &&
               (address.getCity() == null || address.getCity().length() == 0) &&
               (address.getState() == null || address.getState().length() == 0) &&
               (address.getZipCode() == null || address.getZipCode().length() == 0);
    }


    /**
     * Gets the phone number
     * @return - the phone number of the account holder
     */
    @Override
    public String getPhone() {
        return phone;
    }


    /**
     * Sets the phone number of the account holder
     * @param phone - the value to use for the account holder
     */
    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }


    /**
     * Gets the email address
     * @return - the email address of the account holder
     */
    @Override
    public String getEmail() {
        return email;
    }


    /**
     * Sets the email address
     * @param email - the value to use for the account holder's email
     */
    @Override
    public void setEmail(String email) {
        this.email = email;
    }


    /**
     * Gets the account holder's credit card number
     * @return - the credit card number
     */
    @Override
    public CreditCard getCreditCard() {
        return creditCard;
    }


    /**
     * Sets the credit card number
     * @param card - the value to use for the credit card
     */
    @Override
    public void setCreditCard(CreditCard card) {
        if (isEmptyCreditCard(card)) {
            this.creditCard = null;
        } else {
            this.creditCard = card;
        }
    }

    private boolean isEmptyCreditCard(CreditCard card) {
        return (card.getAccountNumber() == null || card.getAccountNumber().length() == 0) &&
               (card.getHolder() == null || card.getHolder().length() == 0);
    }


    /**
     * Sets the account manager responsible for persisting/managing this account.
     * This may be invoked exactly once on any given account, any subsequent
     * invocations should be ignored. The account manager member should not be
     * serialized with implementing class object.
     * @param m - the account manager to set
     */
    @Override
    public void registerAccountManager(AccountManager m) {
        if (accountManager == null) {
            accountManager = m;
        } else {
            LOGGER.log(Level.FINEST, "Attempting to set the account manager, after it has been initialized.");
        }
    }


    /**
     * Incorporates the effect of an order in the balance.
     * @param order - the order to be reflected in the account
     * @param executionPrice - the price at which to execute the order
     */
    @Override
    public void reflectOrder(Order order, int executionPrice) {
        try {
            balance += order.valueOfOrder(executionPrice);
            if (accountManager != null) {
                accountManager.persist(this);
            } else {
                LOGGER.log(Level.SEVERE, "Account manager has not been initialized.",
                                  new Exception());
            }
        } catch (final AccountException ex) {
            LOGGER.log(Level.SEVERE, "Failed to persist account " + name
                                             + " after adjusting for order.", ex);
        }
    }


    @Override
    public String toString() {
        return "name=" + name + "\n" +
               "balance=" + balance + "\n" +
               "fullName=" + fullName + "\n" +
               "phone=" + phone + "\n" +
               "email=" + email + "\n" +
               "passwordHash=" + Arrays.toString(passwordHash) + "\n";
    }
}
