package edu.uw.danco.dao;

import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;
import edu.uw.ext.framework.dao.DaoFactoryException;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/16/13
 * Time: 8:46 PM
 */
public class AccountDaoFactoryImpl implements DaoFactory {

    /**
     * Instantiates a new AccountDao object.
     * @return - a newly instantiated account DAO object
     * @throws DaoFactoryException if unable to instantiate the DAO object
     */
    @Override
    public AccountDao getAccountDao() throws DaoFactoryException {
        AccountDao accountDao = new AccountDaoImpl();
        return accountDao;
    }
}
