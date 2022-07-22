package tools.wztosql;

import constants.ServerConfig;
import constants.ServerConstants;
import database.DBConPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ServerProperties;
import tools.FileoutputUtil;

public class FixCharSets {

    private static final Logger logger = LogManager.getLogger(FixCharSets.class);

    public static void main(String[] args) {
        ServerConfig.SQL_IP = ServerProperties.getProperty("sql_ip", ServerConfig.SQL_IP);
        ServerConfig.SQL_PORT = ServerProperties.getProperty("sql_port", ServerConfig.SQL_PORT);
        ServerConfig.SQL_USER = ServerProperties.getProperty("sql_user", ServerConfig.SQL_USER);
        ServerConfig.SQL_PASSWORD = ServerProperties.getProperty("sql_password", ServerConfig.SQL_PASSWORD);
        ServerConfig.SQL_DATABASE = ServerProperties.getProperty("sql_db", ServerConfig.SQL_DATABASE);

        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            try (ResultSet rs = con.prepareStatement("SELECT CONCAT('ALTER TABLE `', tbl.`TABLE_SCHEMA`, '`.`', tbl.`TABLE_NAME`, '` CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;') FROM `information_schema`.`TABLES` tbl WHERE tbl.`TABLE_SCHEMA` = '" + ServerConfig.SQL_DATABASE + "'").executeQuery()) {
                PreparedStatement ps;
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                    ps = con.prepareStatement(rs.getString(1));
                    ps.execute();
                    ps.close();

                }
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }
}
