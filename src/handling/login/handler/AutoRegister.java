package handling.login.handler;

import client.LoginCrypto;
import database.DBConPool;
import handling.login.LoginServer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.FileoutputUtil;

public class AutoRegister {

    private static final int ACCOUNTS_PER_Mac = 3;
    public static boolean autoRegister = LoginServer.getAutoReg();
    public static boolean success = false;
    private static final Logger logger = LogManager.getLogger(AutoRegister.class);

    public static boolean getAccountExists(String login) {
        boolean accountExists = false;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                accountExists = true;
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        return accountExists;
    }

    public static boolean createAccount(String login, String pwd, String eip, String mac) {
        String sockAddr = eip;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            ResultSet rs;
            try (PreparedStatement ipc = con.prepareStatement("SELECT SessionIP FROM accounts WHERE macs = ?")) {
                ipc.setString(1, mac);
                rs = ipc.executeQuery();
                if (rs.first() == false || rs.last() == true && rs.getRow() < ACCOUNTS_PER_Mac) {
                    try {
                        try (PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, SessionIP,qq) VALUES (?, ?, ?, ?, ?, ?,?)")) {
                            ps.setString(1, login);
                            ps.setString(2, LoginCrypto.hexSha1(pwd));
                            ps.setString(3, "autoregister@mail.com");
                            ps.setString(4, "2008-04-07");
                            ps.setString(5, mac);
                            ps.setString(6, sockAddr.substring(1, sockAddr.lastIndexOf(':')));
                            ps.setString(7, "123456");
                            ps.executeUpdate();
                        }

                        success = true;
                        return true;
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        return false;
                    }
                }else {
                 success = false;   
            }
            }
            rs.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        return false;
    }

    private AutoRegister() {
    }
}
