package edu.uw.danco.dao;

import edu.uw.danco.account.AddressImpl;
import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/23/13
 * Time: 9:17 PM
 */
public class FileAccountDaoImpl implements AccountDao {
    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(FileAccountDaoImpl.class.getName());

    /** Name of folder in which to save the files */
    private static final String ACCOUNTS_FOLDER = "accounts";

    /** Size of buffer */
    private static final int BUFF_SIZE = 1024;

    /** Character encoding to use when converting strings to/from bytes */
    private static final String ENCODING = "UTF-8";

    /** Account field names */
    public static final String NAME = "name";
    public static final String BALANCE = "balance";
    public static final String FULL_NAME = "fullName";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String PASSWORD_HASH = "passwordHash";

    /** Address field names */
    public static final String STREET_ADDRESS = "streetAddress";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String ZIP_CODE = "zipCode";

    /** Credit card field names */
    public static final String ISSUER = "issuer";
    public static final String TYPE = "type";
    public static final String HOLDER = "holder";
    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String EXPIRATION_DATE = "expirationDate";

    /** file names for stored values */
    public static final String ACCOUNT = "account";
    public static final String ADDRESS = "address";
    public static final String CREDIT_CARD = "credit_card";
    public static final String ACCOUNT_NAME = "account_name";

    /** name and relative location of zip file */
    public static final String ZIP_FILE_NAME = "%s/%s.zip";

    /** Name of xml file with application configuration information */
    public static final String APPLICATION_CONTEXT_FILE_NAME = "context.xml";


    /** No arg constructor for bean support */
    public FileAccountDaoImpl() {
        final File dir = new File(ACCOUNTS_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Restore an account from a zip file
     * @param accountName - the name of the desired account
     * @return - the account if located, otherwise null
     */
    @Override
    public Account getAccount(String accountName) {
        BeanFactory beanFactory = new FileSystemXmlApplicationContext(APPLICATION_CONTEXT_FILE_NAME);
        Account acct = null;

        File file = new File(String.format(ZIP_FILE_NAME, ACCOUNTS_FOLDER, accountName));
        if (file.exists()) {
            acct = beanFactory.getBean(Account.class);
            try {
                FileInputStream fis = new FileInputStream(file);
                ZipInputStream zin = new ZipInputStream(fis);
                ZipEntry entry;
                byte[] buff = new byte[BUFF_SIZE];

                while ((entry = zin.getNextEntry()) != null) {
                    String name = entry.getName();
                    File f = new File(name);
                    FileOutputStream fos = new FileOutputStream(f);
                    int len = -1;
                    while ( (len = zin.read(buff, 0, BUFF_SIZE)) != -1) {
                        fos.write(buff, 0, len);
                    }
                    zin.closeEntry();

                    Properties props = new Properties();
                    props.load(new FileInputStream(f));
                    if (name.equalsIgnoreCase(ACCOUNT)) {
                        updateAccount(acct, props);
                        continue;
                    }
                    if (name.equalsIgnoreCase(ADDRESS)) {
                        updateAccountAddress(acct, props);
                        continue;
                    }
                    if (name.equalsIgnoreCase(CREDIT_CARD)) {
                        updateCreditCard(acct, props);
                    }
                }
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Unable to open zip file for account = " + accountName, e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to ope zip file: " + file.getName(), e);
            }
        }

        return acct;
    }

    /**
     * Update the account with basic account information
     * @param acct the account to update
     * @param props - the properties object from which to read values
     */
    private void updateAccount(Account acct, Properties props) {
        try {
            acct.setName(props.getProperty(NAME, null));
            acct.setBalance(Integer.parseInt(props.getProperty(BALANCE)));
            acct.setFullName(props.getProperty(FULL_NAME));
            acct.setPhone(props.getProperty(PHONE));
            acct.setEmail(props.getProperty(EMAIL));
            String pw = props.getProperty(PASSWORD_HASH);
            String[] theBytes = pw.substring(1, pw.length() - 1).split(",");
            byte[] bytes = new byte[theBytes.length];
            int index = 0;
            for (String b : theBytes) {
                bytes[index++] = Byte.parseByte(b.trim());
            }
            acct.setPasswordHash(bytes);
        } catch (AccountException e) {
            LOGGER.log(Level.SEVERE,
                  String.format("Unable to set account value: %s to %s", "name",
                               props.getProperty(NAME)), e);
        }
    }


    /**
     * Update the account address
     * @param acct the account to update
     * @param props - the properties object from which to read values
     */
    private void updateAccountAddress(Account acct, Properties props) {
        BeanFactory beanFactory = new FileSystemXmlApplicationContext(APPLICATION_CONTEXT_FILE_NAME);

        Address addr = beanFactory.getBean(AddressImpl.class);
        addr.setStreetAddress(props.getProperty(STREET_ADDRESS));
        addr.setCity(props.getProperty(CITY));
        addr.setState(props.getProperty(STATE));
        addr.setZipCode(props.getProperty(ZIP_CODE));

        acct.setAddress(addr);
    }


    /**
     * Update the account with credit card info
     * @param acct the account to update
     * @param props - the properties object from which to read values
     */
    private void updateCreditCard(Account acct, Properties props) {
        BeanFactory beanFactory = new FileSystemXmlApplicationContext(APPLICATION_CONTEXT_FILE_NAME);

        CreditCard cc = beanFactory.getBean(CreditCard.class);
        cc.setIssuer(props.getProperty(ISSUER));
        cc.setType(props.getProperty(TYPE));
        cc.setHolder(props.getProperty(HOLDER));
        cc.setAccountNumber(props.getProperty(ACCOUNT_NUMBER));
        cc.setExpirationDate(props.getProperty(EXPIRATION_DATE));
        acct.setCreditCard(cc);
    }


    /**
     * Write out an account to separate files representing the account, address and cc info
     * @param account - the account
     * @throws AccountException - if unable to create the file
     */
    @Override
    public void setAccount(Account account) throws AccountException {
        try {
            FileOutputStream fos = new FileOutputStream(
                    String.format(ZIP_FILE_NAME, ACCOUNTS_FOLDER, account.getName()));
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ZipOutputStream zipped = new ZipOutputStream(bos);
            addEntry(zipped, ACCOUNT, account.toString());

            if (account.getAddress() != null) {
                addEntry(zipped, ADDRESS, account.getAddress().toString());
            }
            if (account.getCreditCard() != null) {
                addEntry(zipped, CREDIT_CARD, account.getCreditCard().toString());
            }
            zipped.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to open zip file for " + account.getName(), e);
            throw new AccountException(e);
        }
    }


    /**
     * Deletes a specific account file representation
     * @param accountName
     * @throws AccountException
     */
    @Override
    public void deleteAccount(String accountName) throws AccountException {
        File f = new File(String.format(ZIP_FILE_NAME, ACCOUNTS_FOLDER, accountName));
        if (f.exists()) {
            f.delete();
        } else {
            LOGGER.log(Level.SEVERE, "Unable to delete account " + accountName);
            throw new AccountException("Unable to delete account " + accountName);
        }
    }


    /**
     * Deletes all the files in the default store
     * @throws AccountException
     */
    @Override
    public void reset() throws AccountException {
        File dir = new File(ACCOUNTS_FOLDER);
        for (File f : dir.listFiles()) {
            f.delete();
        }
    }

    @Override
    public void close() throws AccountException {
        //no op
    }


    /**
     * Helper method to zip up the account file along with the address & cc info
     * */
    private void addEntry(ZipOutputStream zip, String name, String value) throws IOException {

        byte[] buff = new byte[BUFF_SIZE];
        int bytes = 0;
        InputStream in = new ByteArrayInputStream(value.getBytes(ENCODING));
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        while ( (bytes = in.read(buff)) != -1) {
            zip.write(buff, 0, bytes);
        }
        zip.closeEntry();
    }


    /** Get a properties object representing the account information */
    private Properties getAccountProperties(Account account) {
        Properties props = new Properties();
        props.setProperty(ACCOUNT_NAME, account.getName());
        props.setProperty(PASSWORD_HASH, new String(account.getPasswordHash()));
        props.setProperty(BALANCE, Integer.toString(account.getBalance()));
        props.setProperty(FULL_NAME, account.getFullName());
        props.setProperty(PHONE, account.getPhone());
        props.setProperty(EMAIL, account.getEmail());

        return props;
    }


    /** Get a properties object representing the Address information */
    private Properties getAddressProperties(Address addr) {
        Properties props = new Properties();
        if (addr != null) {
            props.setProperty(STREET_ADDRESS, addr.getStreetAddress());
            props.setProperty(CITY, addr.getCity());
            props.setProperty(STATE, addr.getState());
            props.setProperty(ZIP_CODE, addr.getZipCode());
        }

        return props;
    }


    /** Get a properties object representing the CreditCard information */
    private Properties getCreditCardProperties(CreditCard cc) {
        Properties props = new Properties();
        if (cc != null) {
            props.setProperty(CREDIT_CARD, cc.getAccountNumber());
            props.setProperty(ISSUER, cc.getIssuer());
            props.setProperty(TYPE, cc.getType());
            props.setProperty(HOLDER, cc.getHolder());
            props.setProperty(EXPIRATION_DATE, cc.getExpirationDate());
        }

        return props;
    }
}
