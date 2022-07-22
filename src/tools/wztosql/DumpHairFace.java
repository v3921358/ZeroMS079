/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.wztosql;

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
import tools.FileoutputUtil;
import tools.StringUtil;

/**
 *
 * @author alienware
 */
public class DumpHairFace {

    private static final Map<Integer, String> chrNames = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(DumpHairFace.class);

    public static void main(String[] args) throws SQLException {
        DumpHairFace dump = new DumpHairFace();
        System.out.println("正在转存发型数据到MySQL......");
        dump.dumpHairFaceData("Hair");
        System.out.println("正在转存脸型数据到MySQL......");
        dump.dumpHairFaceData("Face");
        System.out.println("转存结束。");
    }

    public void dumpHairFaceData(String type) throws SQLException {
        File dataFile = new File((System.getProperty("path") != null ? System.getProperty("path") : "") + "wz/Character.wz/" + type);
        File strDataFile = new File((System.getProperty("path") != null ? System.getProperty("path") : "") + "wz/String.wz");
        MapleDataProvider chrData = MapleDataProviderFactory.getDataProvider(dataFile);
        MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(strDataFile);
        MapleData chrStringData = stringDataWZ.getData("Eqp.img").getChildByPath("Eqp/" + type);
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM `wz_" + type.toLowerCase() + "data`");
            ps.execute();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        for (MapleData c : chrStringData) {
            int chrid = Integer.parseInt(c.getName());
            String n = StringUtil.getLeftPaddedStr(chrid + ".img", '0', 12);
            try {
                if (chrData.getData(n) != null) {
                    String name = MapleDataTool.getString("name", c, "无");
                    chrNames.put(chrid, name);
                }
            } catch (NullPointerException e) {
            } catch (RuntimeException e) {
            }
        }
        for (int key : chrNames.keySet()) {
            try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO `wz_" + type.toLowerCase() + "data` (`" + type.toLowerCase() + "id`, `name`) VALUES (?, ?)");
                ps.setInt(1, key);
                ps.setString(2, chrNames.get(key));
                ps.execute();

                System.out.println("键值: " + key + " 名称: " + chrNames.get(key));
            } catch (SQLException ex) {
                FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
                System.err.println(ex);
            }
        }

        chrNames.clear();
    }
}
