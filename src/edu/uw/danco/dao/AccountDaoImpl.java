package edu.uw.danco.dao;

import edu.uw.danco.account.AccountImpl;
import edu.uw.danco.account.AddressImpl;
import edu.uw.danco.account.CreditCardImpl;
import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/16/13
 * Time: 8:45 PM
 *
 * The DataSource for our DAO implementation
 */
public class AccountDaoImpl implements AccountDao {
    /** Empty string */
    private static final String EMPTY_STRING = "";

    /** The SQL used to lookup an account in the mysql DB */
    private static final String ACCOUNT_LOOKUP_SQL =
            "SELECT password_hash, balance, fullname, phone, email,"
                    + "       street, city, state, zip,"
                    + "       card_number, issuer, cardtype, holder, expires"
                    + "  FROM account"
                    + " WHERE account_name = ?";


    /** The SQL used to update an account in the mysql DB */
    private static final String ACCOUNT_UPDATE_SQL =
            "INSERT INTO account"
                    + " ( account_name, password_hash, balance, fullname, phone, email,"
                    + "   street, city, state, zip,"
                    + "   card_number, issuer, cardtype, holder, expires )"
                    + " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) "
                    + " ON DUPLICATE KEY UPDATE"
                    + "   password_hash=VALUES(password_hash), "
                    + "   balance=VALUES(balance), "
                    + "   fullname=VALUES(fullname), "
                    + "   phone=VALUES(phone), "
                    + "   email=VALUES(email), "
                    + "   street=VALUES(street), "
                    + "   city=VALUES(city), "
                    + "   state=VALUES(state), "
                    + "   zip=VALUES(zip), "
                    + "   card_number=VALUES(card_number), "
                    + "   issuer=VALUES(issuer), "
                    + "   cardtype=VALUES(cardtype), "
                    + "   holder=VALUES(holder), "
                    + "   expires=VALUES(expires) ";

    //create references to the SQL PreparedStatement parameters

    /** The SQL used to delete an account from the mysql DB */
    private static final String ACCOUNT_DELETE_SQL =
            "DELETE from account "
                    + " WHERE account_name = ?";


    /** THe SQL used to delete all the accounts */
    private static final String ACCOUNT_RESET_SQL = "DELETE from account";


    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(AccountDaoImpl.class.getName());

    /** THe name of the db to connect to */
    private static final String JDBC_ACCOUNT_DB = "jdbc/AccountDb";

    /** THe connection to the data store */
    private Connection conn = null;

    /** Prepared statement used to retrieve account information */
    private PreparedStatement getAccountPs = null;

    /** Prepared statement used to update an account */
    private PreparedStatement updateAccountPs = null;

    /** Prepared statement used to delete an account */
    private PreparedStatement deleteAccountPs = null;

    /** Prepared statement used to reset all accounts */
    private PreparedStatement resetAccountPs = null;


    /**
     * Constructs an AccountDao instance.
     */
    public AccountDaoImpl() {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup(JDBC_ACCOUNT_DB);
            ctx.close();

            try {
                conn = ds.getConnection();
                getAccountPs = conn.prepareStatement(ACCOUNT_LOOKUP_SQL);
                updateAccountPs = conn.prepareStatement(ACCOUNT_UPDATE_SQL);
                deleteAccountPs = conn.prepareStatement(ACCOUNT_DELETE_SQL);
                resetAccountPs = conn.prepareStatement(ACCOUNT_RESET_SQL);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Unable to get DB connection", e);
            }
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Unable to initialize application context.", e);
        }
    }

    /**
     * Lookup an account in the HashMap based on username
     * @param accountName - the name of the desired account
     * @return - the account if located, otherwise null
     */
    @Override
    public Account getAccount(String accountName) {
        Account account = null;

        ResultSet rs = null;
        try {
            getAccountPs.setString(1, accountName);
            rs = getAccountPs.executeQuery();

            if (rs.next()) {
                account = new AccountImpl();

                account.setName(accountName);
                account.setPasswordHash(rs.getBytes(1));
                account.setBalance(rs.getInt(2));
                account.setFullName(rs.getString(3));
                account.setPhone(rs.getString(4));
                account.setEmail(rs.getString(5));

                Address address = new AddressImpl();            //should be able to use beanfactory to create the
                // Address
                address.setStreetAddress(rs.getString(6));
                address.setCity(rs.getString(7));
                address.setState(rs.getString(8));
                address.setZipCode(rs.getString(9));
                account.setAddress(address);

                CreditCard cc = new CreditCardImpl();
                cc.setAccountNumber(rs.getString(10));
                cc.setIssuer(rs.getString(11));
                cc.setType(rs.getString(12));
                cc.setHolder(rs.getString(13));
                cc.setExpirationDate(rs.getString(14));
                account.setCreditCard(cc);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to retrieve account for accountName = " + accountName, e);
        } catch (AccountException e) {
            LOGGER.log(Level.SEVERE, "Unable to set accountname to: " + accountName, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Unable to close ResultSet", e);
            }
        }

        return account;
    }


    /**
     * Adds or updates an account
     * @param account - the account to add or update
     * @throws AccountException - if the operation fails
     *
     * account_name, password_hash, balance, fullname, phone, email,"
    + "   street, city, state, zip,"
    + "   card_number, issuer, cardtype, holder, expires
     */
    @Override
    public void setAccount(Account account) throws AccountException {
        try {
            updateAccountPs.setString(1, account.getName());
            updateAccountPs.setBytes(2, account.getPasswordHash());
            updateAccountPs.setInt(3, account.getBalance());
            updateAccountPs.setString(4, account.getFullName());
            updateAccountPs.setString(5, account.getPhone());
            updateAccountPs.setString(6, account.getEmail());

            Address addr = account.getAddress();
            if (addr != null) {
                updateAccountPs.setString(7, addr.getStreetAddress());
                updateAccountPs.setString(8, addr.getCity());
                updateAccountPs.setString(9, addr.getState());
                updateAccountPs.setString(10, addr.getZipCode());
            } else {
                updateAccountPs.setString(7, EMPTY_STRING); // prefer to use setNull for these; also create references to the column indexes rather than magic numbers
                updateAccountPs.setString(8, EMPTY_STRING);
                updateAccountPs.setString(9, EMPTY_STRING);
                updateAccountPs.setString(10, EMPTY_STRING);
            }

            CreditCard cc = account.getCreditCard();
            if (cc != null) {
                updateAccountPs.setString(11, cc.getAccountNumber());
                updateAccountPs.setString(12, cc.getIssuer());
                updateAccountPs.setString(13, cc.getType());
                updateAccountPs.setString(14, cc.getHolder());
                updateAccountPs.setString(15, cc.getExpirationDate());
            } else {
                updateAccountPs.setString(11, EMPTY_STRING);
                updateAccountPs.setString(12, EMPTY_STRING);
                updateAccountPs.setString(13, EMPTY_STRING);
                updateAccountPs.setString(14, EMPTY_STRING);
                updateAccountPs.setString(15, EMPTY_STRING);
            }

            updateAccountPs.executeUpdate();
            updateAccountPs.clearParameters();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to update account", e);
        }
    }


    /**
     * Remove the account
     * @param accountName - the name of the account to be deleted
     * @throws AccountException - if the operation fails
     */
    @Override
    public void deleteAccount(String accountName) throws AccountException {
        try {
            deleteAccountPs.setString(1, accountName);

            deleteAccountPs.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to delete account", e);
        }
    }


    /**
     * Remove all the accounts. This is primarily to facilitate testing
     * @throws AccountException - if the operation fails
     */
    @Override
    public void reset() throws AccountException {
        try {
            resetAccountPs.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to reset accounts", e);
        }
    }


    /**
     * Close the DAO, release any resources used by the DAO implementation.
     * @throws AccountException
     */
    @Override
    public void close() throws AccountException {
        try {
            // prepared statements should automatically get closed by closing the connection.
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to close connection to DB", e);
        } finally {
            conn = null;
        }
    }
}
