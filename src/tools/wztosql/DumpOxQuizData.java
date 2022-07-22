/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.wztosql;

import constants.ServerConfig;
import constants.ServerConstants;
import database.DBConPool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.ServerProperties;
import tools.FileoutputUtil;

/**
 *
 * @author Itzik
 */
public class DumpOxQuizData {

    static CharsetEncoder asciiEncoder = Charset.forName(ServerConstants.ASCII).newEncoder();
    private static final Logger logger = LogManager.getLogger(DumpOxQuizData.class);

    public static void main(String args[]) throws FileNotFoundException, IOException, SQLException {
        ServerConfig.SQL_IP = ServerProperties.getProperty("sql_ip", ServerConfig.SQL_IP);
        ServerConfig.SQL_PORT = ServerProperties.getProperty("sql_port", ServerConfig.SQL_PORT);
        ServerConfig.SQL_USER = ServerProperties.getProperty("sql_user", ServerConfig.SQL_USER);
        ServerConfig.SQL_PASSWORD = ServerProperties.getProperty("sql_password", ServerConfig.SQL_PASSWORD);
        ServerConfig.SQL_DATABASE = ServerProperties.getProperty("sql_db", ServerConfig.SQL_DATABASE);
        //String output = args[0];
        //File outputDir = new File(output);
        //File cashTxt = new File("ox.sql");
        //outputDir.mkdir();
        //cashTxt.createNewFile();
        System.out.println("OXQuiz.img Loading ...");
        //try (PrintWriter writer = new PrintWriter(new FileOutputStream(cashTxt))) {
        //    writer.println("INSERT INTO `wz_oxdata` (`questionset`, `questionid`, `question`, `display`, `answer`) VALUES");
        DumpOxQuizData dump = new DumpOxQuizData();
        dump.dumpOxData();
        //    writer.flush();
        //}
        System.out.println("Ox quiz data is complete");
    }

    public void dumpOxData() throws SQLException {
        MapleDataProvider stringProvider;
        stringProvider = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/Etc.wz"));
        MapleData ox = stringProvider.getData("OXQuiz.img");
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM `wz_oxdata`");
            ps.execute();
            ps.close();
            for (MapleData child1 : ox.getChildren()) {
                for (MapleData child2 : child1.getChildren()) {
                    MapleData q = child2.getChildByPath("q");
                    MapleData d = child2.getChildByPath("d");
                    int a = MapleDataTool.getIntConvert(child2.getChildByPath("a"));
                    String qs = "";
                    String ds = "";
                    String as;
                    if (a == 0) {
                        as = "x";
                    } else {
                        as = "o";
                    }
                    if (q != null) {
                        qs = (String) q.getData();
                    }
                    if (d != null) {
                        ds = (String) d.getData();
                    }
                    if (!asciiEncoder.canEncode(child1.getName()) || !asciiEncoder.canEncode(child2.getName())
                            || !asciiEncoder.canEncode(qs) || !asciiEncoder.canEncode(ds)
                            || !asciiEncoder.canEncode(as)) {
                        continue;
                    }
                    ps = con.prepareStatement("INSERT INTO `wz_oxdata`"
                            + " (`questionset`, `questionid`, `question`, `display`, `answer`)"
                            + " VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, child1.getName());
                    ps.setString(2, child2.getName());
                    ps.setString(3, qs);
                    ps.setString(4, ds);
                    ps.setString(5, as);
                    ps.execute();
                    ps.close();
                    System.out.println("(" + child1.getName() + "," + child2.getName() + ", '" + qs + "', '" + ds + "', '" + as + "'), ");
                }
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }
}
