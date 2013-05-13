package edu.uw.danco.dao;

import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;
import edu.uw.ext.framework.dao.DaoFactoryException;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/23/13
 * Time: 9:20 PM
 */
public class FileAccountDaoFactoryImpl implements DaoFactory {

    /**
     * Instantiates a new AccountDao object.
     * @return - a newly instantiated account DAO object
     * @throws DaoFactoryException - if unable to instantiate the DAO object
     */
    @Override
    public AccountDao getAccountDao() throws DaoFactoryException {
        FileAccountDaoImpl fileAccountDao = new FileAccountDaoImpl();
        return fileAccountDao;
    }
}
