package edu.uw.danco;

import edu.uw.danco.account.AccountFactoryImpl;
import edu.uw.ext.framework.account.Account;
import org.junit.Test;

import java.nio.charset.Charset;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: dcostinett
 * Date: 4/14/13
 * Time: 8:13 AM
 */
public class AccountImplTest {
    @Test
    public void testSetName() throws Exception {
        Account myAccount = new AccountFactoryImpl().newAccount(
                "MyTestAccount", "password".getBytes(Charset.defaultCharset()), 0);

        assertEquals("Didn't get expected account name", "MyTestAccount", myAccount.getName());
        assertEquals("Didn't get expected password hash",
                new String("password".getBytes(Charset.defaultCharset())),
                new String(myAccount.getPasswordHash()));
        assertEquals("Didn't get expected account name", 0, myAccount.getBalance());
    }
}
