/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.wztosql;

import constants.ServerConfig;
import constants.ServerConstants;
import database.DBConPool;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.ServerProperties;
import tools.FileoutputUtil;
import tools.StringUtil;

/**
 *
 * @author Itzik
 */
public class DumpNpcNames {

    private static final Map<Integer, String> npcNames = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(DumpNpcNames.class);

    public static void main(String[] args) throws SQLException {
        ServerConfig.SQL_IP = ServerProperties.getProperty("sql_ip", ServerConfig.SQL_IP);
        ServerConfig.SQL_PORT = ServerProperties.getProperty("sql_port", ServerConfig.SQL_PORT);
        ServerConfig.SQL_USER = ServerProperties.getProperty("sql_user", ServerConfig.SQL_USER);
        ServerConfig.SQL_PASSWORD = ServerProperties.getProperty("sql_password", ServerConfig.SQL_PASSWORD);
        ServerConfig.SQL_DATABASE = ServerProperties.getProperty("sql_db", ServerConfig.SQL_DATABASE);
        System.out.println("Dumping npc name data.");
        DumpNpcNames dump = new DumpNpcNames();
        dump.dumpNpcNameData();
        System.out.println("Dump complete.");
    }

    public void dumpNpcNameData() throws SQLException {
        File dataFile = new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Npc.wz");
        File strDataFile = new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/String.wz");
        MapleDataProvider npcData = MapleDataProviderFactory.getDataProvider(dataFile);
        MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(strDataFile);
        MapleData npcStringData = stringDataWZ.getData("Npc.img");
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM `wz_npcnamedata`")) {
                ps.execute();
            }
            for (MapleData c : npcStringData) {
                int nid = Integer.parseInt(c.getName());
                String n = StringUtil.getLeftPaddedStr(nid + ".img", '0', 11);
                try {
                    if (npcData.getData(n) != null) {//only thing we really have to do is check if it exists. if we wanted to, we could get the script as well :3
                        String name = MapleDataTool.getString("name", c, "MISSINGNO");
                        if (name.contains("Maple TV") || name.contains("Baby Moon Bunny")) {
                            continue;
                        }
                        npcNames.put(nid, name);
                    }
                } catch (NullPointerException ex) {
                    FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
                    System.err.println(ex);
                } catch (RuntimeException ex) { //swallow, don't add if 
                    FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
                    System.err.println(ex);
                }
            }
            for (int key : npcNames.keySet()) {
                try {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO `wz_npcnamedata` (`npc`, `name`) VALUES (?, ?)")) {
                        ps.setInt(1, key);
                        ps.setString(2, npcNames.get(key));
                        ps.execute();
                    }
                    System.out.println("key: " + key + " name: " + npcNames.get(key));
                } catch (Exception ex) {
                    FileoutputUtil.outputFileError(FileoutputUtil.Exception_Log, ex);
                    System.err.println(ex);
                }
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }
}
