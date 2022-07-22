/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package handling.channel;

import client.MapleCharacter;
import database.DBConPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.FileoutputUtil;

public class MapleGuildRanking {

    private static final MapleGuildRanking instance = new MapleGuildRanking();
    private final List<GuildRankingInfo> ranks = new LinkedList<>();
    private final List<levelRankingInfo> levelranks = new LinkedList<>();
    private final List<mesoRankingInfo> mesoranks = new LinkedList<>();
    private final List<fameRankingInfo> fameranks = new LinkedList<>();
    private final List<strRankingInfo> strranks = new LinkedList<>();
    private final List<dexRankingInfo> dexranks = new LinkedList<>();
    private final List<intRankingInfo> intranks = new LinkedList<>();
    private final List<lukRankingInfo> lukranks = new LinkedList<>();
    private static final Logger logger = LogManager.getLogger(MapleGuildRanking.class);

    public static MapleGuildRanking getInstance() {
        return instance;
    }

    public void load() {
        if (ranks.isEmpty()) {
            reload();
        }
    }

    public List<GuildRankingInfo> getRank() {
        reload();
        return ranks;
    }

    public List<levelRankingInfo> getLevelRank() {

        showLevelRank();

        return levelranks;
    }

    public List<mesoRankingInfo> getMesoRank() {

        showMesoRank();

        return mesoranks;
    }

    public List<fameRankingInfo> getFameRank() {
        showFameRank();
        return fameranks;
    }

    public List<strRankingInfo> getStrRank() {
        strranks.clear();
        Map<String, Integer> map = new TreeMap<>();
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                map.put(mch.getName(), mch.getStat().getTotalStr());
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for (Map.Entry<String, Integer> mapping : list) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                MapleCharacter mch = cserv.getPlayerStorage().getCharacterByName(mapping.getKey());
                if (mch != null) {
                    if (!mch.isIntern()) {
                        final strRankingInfo rank1 = new strRankingInfo(
                                mch.getName(),
                                mch.getStat().getTotalStr(),
                                mch.getStr(),
                                mch.getDex(),
                                mch.getInt(),
                                mch.getLuk());
                        strranks.add(rank1);
                    }
                }
            }  
        }
        return strranks;
    }

    public List<dexRankingInfo> getDexRank() {
        dexranks.clear();
        Map<String, Integer> map = new TreeMap<>();
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                map.put(mch.getName(), mch.getStat().getTotalDex());
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for (Map.Entry<String, Integer> mapping : list) {

            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                MapleCharacter mch = cserv.getPlayerStorage().getCharacterByName(mapping.getKey());
                if (mch != null) {
                    if (!mch.isIntern()) {
                        final dexRankingInfo rank1 = new dexRankingInfo(
                                mch.getName(),
                                mch.getStat().getTotalDex(),
                                mch.getStr(),
                                mch.getDex(),
                                mch.getInt(),
                                mch.getLuk());
                        dexranks.add(rank1);
                    }
                }
            }
        }

        return dexranks;
    }

    public List<intRankingInfo> getIntRank() {
        dexranks.clear();
        Map<String, Integer> map = new TreeMap<>();
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                map.put(mch.getName(), mch.getStat().getTotalInt());
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for (Map.Entry<String, Integer> mapping : list) {

            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                MapleCharacter mch = cserv.getPlayerStorage().getCharacterByName(mapping.getKey());
                if (mch != null) {
                    if (!mch.isIntern()) {
                        final intRankingInfo rank1 = new intRankingInfo(
                                mch.getName(),
                                mch.getStat().getTotalInt(),
                                mch.getStr(),
                                mch.getDex(),
                                mch.getInt(),
                                mch.getLuk());
                        intranks.add(rank1);
                    }
                }
            }
        }

        return intranks;
    }

    public List<lukRankingInfo> getLukRank() {
        lukranks.clear();
        Map<String, Integer> map = new TreeMap<>();
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                map.put(mch.getName(), mch.getStat().getTotalLuk());
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for (Map.Entry<String, Integer> mapping : list) {

            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                MapleCharacter mch = cserv.getPlayerStorage().getCharacterByName(mapping.getKey());
                if (mch != null) {
                    if (!mch.isIntern()) {
                        final lukRankingInfo rank1 = new lukRankingInfo(
                                mch.getName(),
                                mch.getStat().getTotalLuk(),
                                mch.getStr(),
                                mch.getDex(),
                                mch.getInt(),
                                mch.getLuk());
                        lukranks.add(rank1);
                    }
                }
            }
        }

        return lukranks;
    }

    private void showLevelRank() {
        levelranks.clear();
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm < 1 ORDER BY `level` DESC LIMIT 100");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                final levelRankingInfo rank1 = new levelRankingInfo(
                        rs.getString("name"),
                        rs.getInt("level"),
                        rs.getInt("str"),
                        rs.getInt("dex"),
                        rs.getInt("int"),
                        rs.getInt("luk"));
                levelranks.add(rank1);
            }
            ps.close();
            rs.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }

    private void showMesoRank() {
        mesoranks.clear();
        ResultSet rs;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection();) {
            //PreparedStatement ps = con.prepareStatement("SELECT *, ( chr.meso + s.meso ) as money FROM `characters` as chr , `storages` as s WHERE chr.gm < 1  AND s.accountid = chr.accountid ORDER BY money DESC LIMIT 20");
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm < 1 ORDER BY `meso` DESC LIMIT 20");
            rs = ps.executeQuery();
            while (rs.next()) {
                final mesoRankingInfo rank2 = new mesoRankingInfo(
                        rs.getString("name"),
                        rs.getLong("meso"),
                        rs.getInt("str"),
                        rs.getInt("dex"),
                        rs.getInt("int"),
                        rs.getInt("luk"));
                mesoranks.add(rank2);
            }

            rs.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }

    private void showFameRank() {
        fameranks.clear();
        ResultSet rs;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE gm < 1 ORDER BY `fame` DESC LIMIT 100");
            rs = ps.executeQuery();
            while (rs.next()) {
                final fameRankingInfo rank4 = new fameRankingInfo(
                        rs.getString("name"),
                        rs.getInt("fame"),
                        rs.getInt("str"),
                        rs.getInt("dex"),
                        rs.getInt("int"),
                        rs.getInt("luk"));
                fameranks.add(rank4);
            }

            rs.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }

    private void reload() {
        ranks.clear();
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            ResultSet rs;
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM guilds ORDER BY `GP` DESC LIMIT 50")) {
                rs = ps.executeQuery();
                while (rs.next()) {
                    final GuildRankingInfo rank = new GuildRankingInfo(
                            rs.getString("name"),
                            rs.getInt("GP"),
                            rs.getInt("logo"),
                            rs.getInt("logoColor"),
                            rs.getInt("logoBG"),
                            rs.getInt("logoBGColor"));

                    ranks.add(rank);
                }
            }
            rs.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }

    public static class GuildRankingInfo {

        private final String name;
        private final int gp, logo, logocolor, logobg, logobgcolor;

        public GuildRankingInfo(String name, int gp, int logo, int logocolor, int logobg, int logobgcolor) {
            this.name = name;
            this.gp = gp;
            this.logo = logo;
            this.logocolor = logocolor;
            this.logobg = logobg;
            this.logobgcolor = logobgcolor;
        }

        public String getName() {
            return name;
        }

        public int getGP() {
            return gp;
        }

        public int getLogo() {
            return logo;
        }

        public int getLogoColor() {
            return logocolor;
        }

        public int getLogoBg() {
            return logobg;
        }

        public int getLogoBgColor() {
            return logobgcolor;
        }
    }

    public static class mesoRankingInfo {

        private final String name;
        private final long meso;
        private final int str, dex, _int, luk;

        public mesoRankingInfo(String name, long meso, int str, int dex, int intt, int luk) {
            this.name = name;
            this.meso = meso;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }

        public String getName() {
            return name;
        }

        public long getMeso() {
            return meso;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getInt() {
            return _int;
        }

        public int getLuk() {
            return luk;
        }
    }

    public static class levelRankingInfo {

        private final String name;
        private final int level, str, dex, _int, luk;

        public levelRankingInfo(String name, int level, int str, int dex, int intt, int luk) {
            this.name = name;
            this.level = level;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }

        public String getName() {
            return name;
        }

        public int getLevel() {
            return level;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getInt() {
            return _int;
        }

        public int getLuk() {
            return luk;
        }
    }

    public static class fameRankingInfo {

        private final String name;
        private final int fame, str, dex, _int, luk;

        public fameRankingInfo(String name, int fame, int str, int dex, int intt, int luk) {
            this.name = name;
            this.fame = fame;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }

        public String getName() {
            return name;
        }

        public int getFame() {
            return fame;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getInt() {
            return _int;
        }

        public int getLuk() {
            return luk;
        }

    }

    public static class strRankingInfo {

        private final String name;
        private final int strall, str, dex, _int, luk;

        public strRankingInfo(String name, int strall, int str, int dex, int intt, int luk) {
            this.name = name;
            this.strall = strall;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }

        public String getName() {
            return name;
        }

        public int getStrall() {
            return strall;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getInt() {
            return _int;
        }

        public int getLuk() {
            return luk;
        }

    }

    public static class dexRankingInfo {

        private final String name;
        private final int dexall, str, dex, _int, luk;

        public dexRankingInfo(String name, int dexall, int str, int dex, int intt, int luk) {
            this.name = name;
            this.dexall = dexall;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }

        public String getName() {
            return name;
        }

        public int getDexall() {
            return dexall;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getInt() {
            return _int;
        }

        public int getLuk() {
            return luk;
        }

    }

    public static class intRankingInfo {

        private final String name;
        private final int intall, str, dex, _int, luk;

        public intRankingInfo(String name, int intall, int str, int dex, int intt, int luk) {
            this.name = name;
            this.intall = intall;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }

        public String getName() {
            return name;
        }

        public int getIntall() {
            return intall;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getInt() {
            return _int;
        }

        public int getLuk() {
            return luk;
        }

    }

    public static class lukRankingInfo {

        private final String name;
        private final int lukall, str, dex, _int, luk;

        public lukRankingInfo(String name, int lukall, int str, int dex, int intt, int luk) {
            this.name = name;
            this.lukall = lukall;
            this.str = str;
            this.dex = dex;
            this._int = intt;
            this.luk = luk;
        }

        public String getName() {
            return name;
        }

        public int getLukall() {
            return lukall;
        }

        public int getStr() {
            return str;
        }

        public int getDex() {
            return dex;
        }

        public int getInt() {
            return _int;
        }

        public int getLuk() {
            return luk;
        }

    }
}
