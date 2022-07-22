/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import com.alibaba.druid.pool.DruidDataSource;
import constants.ServerConfig;
import constants.ServerConstants;
import server.ServerProperties;

/**
 *
 * @author 
 */
public class DBConPool {

    private static DruidDataSource dataSource = null;

    static {
        InitDB();
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("[数据库信息] 找不到JDBC驱动.");
            System.exit(0);
        }
    }

    public static void InitDB() {
        ServerConfig.SQL_IP = ServerProperties.getProperty("SQL_IP", ServerConfig.SQL_IP);
        ServerConfig.SQL_PORT = ServerProperties.getProperty("SQL_PORT", ServerConfig.SQL_PORT);
        ServerConfig.SQL_DATABASE = ServerProperties.getProperty("SQL_DATABASE", ServerConfig.SQL_DATABASE);
        ServerConfig.SQL_USER = ServerProperties.getProperty("SQL_USER", ServerConfig.SQL_USER);
        ServerConfig.SQL_PASSWORD = ServerProperties.getProperty("SQL_PASSWORD", ServerConfig.SQL_PASSWORD);
    }

    private static class InstanceHolder {

        public static final DBConPool instance = new DBConPool();

        private InstanceHolder() {
        }
    }

    public static DBConPool getInstance() {
        return InstanceHolder.instance;
    }

    private DBConPool() {
    }

    public DruidDataSource getDataSource() {
        if (dataSource == null) {
            InitDBConPool();
        }
        return dataSource;
    }

    private void InitDBConPool() {
        dataSource = new DruidDataSource();
        dataSource.setName("mysql_pool");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + ServerConfig.SQL_IP + ":" + ServerConfig.SQL_PORT + "/" + ServerConfig.SQL_DATABASE + "?useUnicode=true&characterEncoding=UTF8");
        dataSource.setUsername(ServerConfig.SQL_USER);
        dataSource.setPassword(ServerConfig.SQL_PASSWORD);
        dataSource.setInitialSize(300);//150
        dataSource.setMinIdle(500);//250
        dataSource.setMaxActive(3000);//1000
        //dataSource.setInitialSize(750);
        //dataSource.setMinIdle(1250);
        //dataSource.setMaxActive(5000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 'x'");
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setTestWhileIdle(true);
        dataSource.setMaxWait(60000);
        dataSource.setUseUnfairLock(true);
    }

    public static final int CLOSE_CURRENT_RESULT = 1;
    /**
     * The constant indicating that the current <code>ResultSet</code> object
     * should not be closed when calling <code>getMoreResults</code>.
     *
     * @since 1.4
     */
    public static final int KEEP_CURRENT_RESULT = 2;
    /**
     * The constant indicating that all <code>ResultSet</code> objects that have
     * previously been kept open should be closed when calling
     * <code>getMoreResults</code>.
     *
     * @since 1.4
     */
    public static final int CLOSE_ALL_RESULTS = 3;
    /**
     * The constant indicating that a batch statement executed successfully but
     * that no count of the number of rows it affected is available.
     *
     * @since 1.4
     */
    public static final int SUCCESS_NO_INFO = -2;
    /**
     * The constant indicating that an error occured while executing a batch
     * statement.
     *
     * @since 1.4
     */
    public static final int EXECUTE_FAILED = -3;
    /**
     * The constant indicating that generated keys should be made available for
     * retrieval.
     *
     * @since 1.4
     */
    public static final int RETURN_GENERATED_KEYS = 1;
    /**
     * The constant indicating that generated keys should not be made available
     * for retrieval.
     *
     * @since 1.4
     */
    public static final int NO_GENERATED_KEYS = 2;

}
