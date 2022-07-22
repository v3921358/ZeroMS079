/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.拍卖;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import database.DBConPool;
import handling.channel.ChannelServer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ItemInformation;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.Timer;
import server.stores.AbstractPlayerStore;
import static server.拍卖.拍卖行_MYSQL.加入商品;
import tools.FileoutputUtil;
import tools.Pair;
import tools.SearchGenerator;

/**
 * 
 * @author XM
 */
public class 拍卖行_MYSQL {
    private static final Logger logger = LogManager.getLogger(AbstractPlayerStore.class);
    private static ReentrantLock 鎖 = new ReentrantLock();
    
    
    
    public static void 初始化() {
        Start(60);//秒
    }
    
    
    private static void Start(final int time) {
        Timer.EventTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                try {
                    自动结算();
                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(拍卖行_MYSQL.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, time * 1000, time * 1000);
        
    }
    
private static void 自动结算() throws SQLException{
    Date now = new Date(); 
    StringBuilder query = new StringBuilder();
    query.append("SELECT * FROM `");
    query.append("拍卖行_商品");
    query.append("` LEFT JOIN `");
    query.append("拍卖行_装备属性");
    query.append("` USING (`inventoryitemid`) WHERE `拍卖到期时间` < '" + now.getTime() + "'");
    query.append(" and 报价角色ID != -1");
    try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
        PreparedStatement ps = con.prepareStatement(query.toString());
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StringBuilder 信息 = new StringBuilder();
                int inventoryitemid = rs.getInt("inventoryitemid");
                int PackageId = rs.getInt("PackageId");
                int itemid = rs.getInt("itemid");
                byte 货币类型 = rs.getByte("货币类型"); 
                int 最高报价 = rs.getInt("最高报价");
                int 报价角色ID = rs.getInt("报价角色ID");
                
                ItemInformation ItemInformation = MapleItemInformationProvider.getItemInformation(itemid);
                int Player_PackageId = 取packageid(报价角色ID);
                if(Player_PackageId != -1){
                    增减额度(Player_PackageId, 货币类型, -最高报价);
                    转移拍卖商品(Player_PackageId, inventoryitemid);
                    
                    信息.append("消耗");
                    switch (货币类型) {
                        case 1:
                            信息.append("金币：");
                            break;
                        case 2:
                            信息.append("点卷：");
                            break;
                        case 3:
                            信息.append("余额：");
                            break;
                    }
                    
                    信息.append(最高报价 + "　");
                    信息.append((ItemInformation == null ? " [ 商品 ] " : "[ " + ItemInformation.name + " ] ") + "已送达,请在拍卖行仓库查询");
                    MapleCharacterUtil.sendNote(ID取角色名(报价角色ID), "拍卖行", 信息.toString(), 0);
                    for (ChannelServer cs : ChannelServer.getAllInstances()) {
                        MapleCharacter chr = cs.getPlayerStorage().getCharacterById(报价角色ID);
                        if(chr != null){
                            chr.showNote();
                        }
                    }
                }
                增减额度(PackageId, 货币类型, 最高报价);
                信息 = new StringBuilder();
                信息.append((ItemInformation == null ? " [ 商品 ] " : "[ " + ItemInformation.name + " ] ") + "已拍出　");
                信息.append("获得");
                switch (货币类型) {
                    case 1:
                        信息.append("金币：");
                        break;
                    case 2:
                        信息.append("点卷：");
                        break;
                    case 3:
                        信息.append("余额：");
                        break;
                }
                信息.append(最高报价);
                int 角色ID = 取角色ID(PackageId);
                MapleCharacterUtil.sendNote(ID取角色名(角色ID), "拍卖行", 信息.toString(), 0);
                for (ChannelServer cs : ChannelServer.getAllInstances()) {
                    MapleCharacter chr = cs.getPlayerStorage().getCharacterById(角色ID);
                    if(chr != null){
                        chr.showNote();
                    }
                }
            }
        }
    } catch (SQLException ex) {
        FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
        System.err.println(ex);
    }
}
    
    
    public static void 转移拍卖商品(int PackageId, int inventoryitemid) throws SQLException{
        StringBuilder query = new StringBuilder("UPDATE `拍卖行_商品` SET packageid=?, 最高报价=?, 报价角色ID=? WHERE inventoryitemid = ?");
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, PackageId);
            ps.setInt(2, 0);
            ps.setInt(3, -1);
            ps.setLong(4, inventoryitemid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
            
        }
    }
    
    
    
    public static Pair<Integer, List<拍卖行_商品>> 搜索(int c_当前页,String c_搜索内容, MapleInventoryType c_Type,int 价格1,int 价格2) throws SQLException{
        Date now = new Date(); 
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
        //String 现行时间 = dateFormat.format( now ); 
        //System.err.println(now.getTime() + (1 * 24 * 60 * 60 * 1000));
        List<拍卖行_商品> items = new LinkedList<>();
        int 总行数 = 0,总页数 = 0;
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `");
        query.append("拍卖行_商品");
        query.append("` LEFT JOIN `");
        query.append("拍卖行_装备属性");
        query.append("` USING (`inventoryitemid`) WHERE `拍卖到期时间` > '" + now.getTime() + "'");
        
        if(!c_搜索内容.isEmpty()){
            Map<Integer, String> data = SearchGenerator.getSearchData(SearchGenerator.SearchType.valueOf(SearchGenerator.SearchType.nameOf(1)), c_搜索内容);
            if (!data.isEmpty()) {
                if(data.size() > 0){
                    query.append(" and (");
                    boolean a = false; 
                    for (int key : data.keySet()) {
                        if(!a){
                            a = true;
                            query.append("itemid = " + key);
                        } else {
                            query.append(" or itemid = " + key);
                        }
                    }
                    query.append(")");
                }
            }
        }
        
        if(c_Type != null){
            query.append(" and inventorytype = " + c_Type.getType());
        }
        if(价格1 != -1 && 价格2 != -1){
            if(价格1 < 价格2){
                query.append(" and (最高报价>=" + 价格1 + " and 最高报价<=" + 价格2 + ")");
            } else {
                query.append(" and (最高报价<=" + 价格1 + " and 最高报价>=" + 价格2 + ")");
            }
        }
        
        
        
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement(query.toString());
            try (ResultSet rs = ps.executeQuery()) {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                
                rs.last();//跳到最后位置
                总行数 = rs.getRow();//取当前位置行号
                rs.beforeFirst();//回到首位置
                
                
                int 每页行数 = 10;
                总页数 = ((总行数 % 每页行数) == 0 ? 总行数 / 每页行数 : (总行数 / 每页行数) + 1);
                int 当前页 = (c_当前页 - 1) * 每页行数;
                rs.absolute(当前页);
                byte 货币类型 = 0;
                int 起步价 = 0,最高报价 = 0,报价角色ID = 0;
                long 到期时间 = 0;
                int i = 0;
                while (rs.next()) {
                    i++;
                    if(i > 每页行数){
                        break;
                    }
                    if (!ii.itemExists(rs.getInt("itemid"))) { //EXPENSIVE
                        continue;
                    }
                    货币类型 = rs.getByte("货币类型");
                    起步价 = rs.getInt("起步价");
                    最高报价 = rs.getInt("最高报价");
                    报价角色ID = rs.getInt("报价角色ID");
                    到期时间 = rs.getLong("拍卖到期时间");
                    
                    //上架时间.getTime()
                    //上架时间.setTime(i);
                    
                    
                    MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                    if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                        Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getShort("flag"));
                        if (equip.getPosition() != -55) { //monsterbook
                            equip.setQuantity((short) 1);
                            equip.setInventoryId(rs.getLong("inventoryitemid"));
                            equip.setOwner(rs.getString("owner"));
                            equip.setExpiration(rs.getLong("expiredate"));
                            equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                            equip.setLevel(rs.getByte("level"));
                            equip.setStr(rs.getShort("str"));
                            equip.setDex(rs.getShort("dex"));
                            equip.setInt(rs.getShort("int"));
                            equip.setLuk(rs.getShort("luk"));
                            equip.setHp(rs.getShort("hp"));
                            equip.setMp(rs.getShort("mp"));
                            equip.setWatk(rs.getShort("watk"));
                            equip.setMatk(rs.getShort("matk"));
                            equip.setWdef(rs.getShort("wdef"));
                            equip.setMdef(rs.getShort("mdef"));
                            equip.setAcc(rs.getShort("acc"));
                            equip.setAvoid(rs.getShort("avoid"));
                            equip.setHands(rs.getShort("hands"));
                            equip.setSpeed(rs.getShort("speed"));
                            equip.setJump(rs.getShort("jump"));
                            equip.setViciousHammer(rs.getByte("ViciousHammer"));
                            equip.setItemEXP(rs.getLong("itemEXP"));
                            equip.setGMLog(rs.getString("GM_Log"));
                            equip.setDurability(rs.getInt("durability"));
                            equip.setEnhance(rs.getByte("enhance"));
                            equip.setPotential1(rs.getInt("potential1"));
                            equip.setPotential2(rs.getInt("potential2"));
                            equip.setPotential3(rs.getInt("potential3"));
                            equip.setBonusPotential1(rs.getInt("potential4"));
                            equip.setBonusPotential2(rs.getInt("potential5"));
                            equip.setBonusPotential3(rs.getInt("potential6"));
                            equip.setFusionAnvil(rs.getInt("fusionAnvil"));
                            equip.setSocket1(rs.getInt("socket1"));
                            equip.setSocket2(rs.getInt("socket2"));
                            equip.setSocket3(rs.getInt("socket3"));
                            equip.setGiftFrom(rs.getString("sender"));
                            equip.setIncSkill(rs.getInt("incSkill"));
                            equip.setPVPDamage(rs.getShort("pvpDamage"));
                            equip.setCharmEXP(rs.getShort("charmEXP"));
                            equip.setEnhanctBuff(rs.getByte("enhanctBuff"));
                            equip.setReqLevel(rs.getByte("reqLevel"));
                            equip.setYggdrasilWisdom(rs.getByte("yggdrasilWisdom"));
                            equip.setFinalStrike(rs.getByte("finalStrike") > 0);
                            equip.setBossDamage(rs.getByte("bossDamage"));
                            equip.setIgnorePDR(rs.getByte("ignorePDR"));
                            equip.setTotalDamage(rs.getByte("totalDamage"));
                            equip.setAllStat(rs.getByte("allStat"));
                            equip.setKarmaCount(rs.getByte("karmaCount"));
                            equip.置已打孔次数(rs.getShort("已打孔"));
                            equip.置孔位状态(1,rs.getInt("孔位1"));
                            equip.置孔位状态(2,rs.getInt("孔位2"));
                            equip.置孔位状态(3,rs.getInt("孔位3"));
                            equip.置孔位状态(4,rs.getInt("孔位4"));
                            equip.置孔位状态(5,rs.getInt("孔位5"));
                            equip.置孔位状态(6,rs.getInt("孔位6"));
                            equip.置孔位状态(7,rs.getInt("孔位7"));
                            equip.置孔位状态(8,rs.getInt("孔位8"));
                            equip.置孔位状态(9,rs.getInt("孔位9"));
                            equip.置孔位状态(10,rs.getInt("孔位10"));
                            equip.置孔位状态(11,rs.getInt("孔位11"));
                            equip.置孔位状态(12,rs.getInt("孔位12"));
                            if (equip.getCharmEXP() < 0) { //has not been initialized yet
                                equip.setCharmEXP(((Equip) ii.getEquipById(equip.getItemId())).getCharmEXP());
                            }
                            if (equip.getUniqueId() > -1) {
                                if (GameConstants.isEffectRing(rs.getInt("itemid"))) {
                                    MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), mit.equals(MapleInventoryType.EQUIPPED));
                                    if (ring != null) {
                                        equip.setRing(ring);
                                    }
                                } else if (equip.getItemId() / 10000 == 166) {
                                    MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                                    if (ring != null) {
                                        equip.setAndroid(ring);
                                    }
                                }
                            }
                        }
                        items.add(new 拍卖行_商品(mit, equip, 货币类型, 起步价, 最高报价, 报价角色ID, 到期时间));
                    } else {
                        Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getShort("flag"), rs.getInt("uniqueid"));
                        item.setOwner(rs.getString("owner"));
                        item.setInventoryId(rs.getLong("inventoryitemid"));
                        item.setExpiration(rs.getLong("expiredate"));
                        item.setGMLog(rs.getString("GM_Log"));
                        item.setGiftFrom(rs.getString("sender"));
                        //item.setExp(rs.getInt("exp")); //TODO: test
                        if (GameConstants.isPet(item.getItemId())) {
                            if (item.getUniqueId() > -1) {
                                MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                                if (pet != null) {
                                    item.setPet(pet);
                                }
                            } else {
                                //O_O hackish fix
                                item.setPet(MaplePet.createPet(item.getItemId(), MapleInventoryIdentifier.getInstance()));
                            }
                        }
                        items.add(new 拍卖行_商品(mit, item, 货币类型, 起步价, 最高报价, 报价角色ID, 到期时间));
                    }
                }
            }
            
            
            
            
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        return new Pair<>(总页数, items);
    }
    
    public static Pair<Integer, List<拍卖行_商品>> 取物主商品(MapleCharacter 物主, int c_当前页, boolean 取已到期商品) throws SQLException{
        Date now = new Date(); 
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
        //String 现行时间 = dateFormat.format( now ); 
        //System.err.println(now.getTime() + (1 * 24 * 60 * 60 * 1000));
        List<拍卖行_商品> items = new LinkedList<>();
        int packageid = 取packageid(物主.getId());
        int 总行数 = 0,总页数 = 0;
        if(packageid == -1){
            return new Pair<>(0, items);
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `");
        query.append("拍卖行_商品");
        query.append("` LEFT JOIN `");
        query.append("拍卖行_装备属性");
        query.append("` USING (`inventoryitemid`) WHERE `packageid` = ?");
        query.append(" and `拍卖到期时间` ");
        query.append((取已到期商品 ? "<":">"));
        query.append(" '" + now.getTime() + "'");
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, packageid);
            try (ResultSet rs = ps.executeQuery()) {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                
                rs.last();//跳到最后位置
                总行数 = rs.getRow();//取当前位置行号
                rs.beforeFirst();//回到首位置
                
                
                int 每页行数 = 9;
                总页数 = ((总行数 % 每页行数) == 0 ? 总行数 / 每页行数 : (总行数 / 每页行数) + 1);
                int 当前页 = (c_当前页 - 1) * 每页行数;
                rs.absolute(当前页);
                byte 货币类型 = 0;
                int 起步价 = 0,最高报价 = 0,报价角色ID = 0;
                long 到期时间 = 0;
                int i = 0;
                while (rs.next()) {
                    i++;
                    if(i > 每页行数){
                        break;
                    }
                    if (!ii.itemExists(rs.getInt("itemid"))) { //EXPENSIVE
                        continue;
                    }
                    货币类型 = rs.getByte("货币类型");
                    起步价 = rs.getInt("起步价");
                    最高报价 = rs.getInt("最高报价");
                    报价角色ID = rs.getInt("报价角色ID");
                    到期时间 = rs.getLong("拍卖到期时间");
                    
                    //上架时间.getTime()
                    //上架时间.setTime(i);
                    
                    
                    MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                    if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                        Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getShort("flag"));
                        if (equip.getPosition() != -55) { //monsterbook
                            equip.setQuantity((short) 1);
                            equip.setInventoryId(rs.getLong("inventoryitemid"));
                            equip.setOwner(rs.getString("owner"));
                            equip.setExpiration(rs.getLong("expiredate"));
                            equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                            equip.setLevel(rs.getByte("level"));
                            equip.setStr(rs.getShort("str"));
                            equip.setDex(rs.getShort("dex"));
                            equip.setInt(rs.getShort("int"));
                            equip.setLuk(rs.getShort("luk"));
                            equip.setHp(rs.getShort("hp"));
                            equip.setMp(rs.getShort("mp"));
                            equip.setWatk(rs.getShort("watk"));
                            equip.setMatk(rs.getShort("matk"));
                            equip.setWdef(rs.getShort("wdef"));
                            equip.setMdef(rs.getShort("mdef"));
                            equip.setAcc(rs.getShort("acc"));
                            equip.setAvoid(rs.getShort("avoid"));
                            equip.setHands(rs.getShort("hands"));
                            equip.setSpeed(rs.getShort("speed"));
                            equip.setJump(rs.getShort("jump"));
                            equip.setViciousHammer(rs.getByte("ViciousHammer"));
                            equip.setItemEXP(rs.getLong("itemEXP"));
                            equip.setGMLog(rs.getString("GM_Log"));
                            equip.setDurability(rs.getInt("durability"));
                            equip.setEnhance(rs.getByte("enhance"));
                            equip.setPotential1(rs.getInt("potential1"));
                            equip.setPotential2(rs.getInt("potential2"));
                            equip.setPotential3(rs.getInt("potential3"));
                            equip.setBonusPotential1(rs.getInt("potential4"));
                            equip.setBonusPotential2(rs.getInt("potential5"));
                            equip.setBonusPotential3(rs.getInt("potential6"));
                            equip.setFusionAnvil(rs.getInt("fusionAnvil"));
                            equip.setSocket1(rs.getInt("socket1"));
                            equip.setSocket2(rs.getInt("socket2"));
                            equip.setSocket3(rs.getInt("socket3"));
                            equip.setGiftFrom(rs.getString("sender"));
                            equip.setIncSkill(rs.getInt("incSkill"));
                            equip.setPVPDamage(rs.getShort("pvpDamage"));
                            equip.setCharmEXP(rs.getShort("charmEXP"));
                            equip.setEnhanctBuff(rs.getByte("enhanctBuff"));
                            equip.setReqLevel(rs.getByte("reqLevel"));
                            equip.setYggdrasilWisdom(rs.getByte("yggdrasilWisdom"));
                            equip.setFinalStrike(rs.getByte("finalStrike") > 0);
                            equip.setBossDamage(rs.getByte("bossDamage"));
                            equip.setIgnorePDR(rs.getByte("ignorePDR"));
                            equip.setTotalDamage(rs.getByte("totalDamage"));
                            equip.setAllStat(rs.getByte("allStat"));
                            equip.setKarmaCount(rs.getByte("karmaCount"));
                            equip.置已打孔次数(rs.getShort("已打孔"));
                            equip.置孔位状态(1,rs.getInt("孔位1"));
                            equip.置孔位状态(2,rs.getInt("孔位2"));
                            equip.置孔位状态(3,rs.getInt("孔位3"));
                            equip.置孔位状态(4,rs.getInt("孔位4"));
                            equip.置孔位状态(5,rs.getInt("孔位5"));
                            equip.置孔位状态(6,rs.getInt("孔位6"));
                            equip.置孔位状态(7,rs.getInt("孔位7"));
                            equip.置孔位状态(8,rs.getInt("孔位8"));
                            equip.置孔位状态(9,rs.getInt("孔位9"));
                            equip.置孔位状态(10,rs.getInt("孔位10"));
                            equip.置孔位状态(11,rs.getInt("孔位11"));
                            equip.置孔位状态(12,rs.getInt("孔位12"));
                            if (equip.getCharmEXP() < 0) { //has not been initialized yet
                                equip.setCharmEXP(((Equip) ii.getEquipById(equip.getItemId())).getCharmEXP());
                            }
                            if (equip.getUniqueId() > -1) {
                                if (GameConstants.isEffectRing(rs.getInt("itemid"))) {
                                    MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), mit.equals(MapleInventoryType.EQUIPPED));
                                    if (ring != null) {
                                        equip.setRing(ring);
                                    }
                                } else if (equip.getItemId() / 10000 == 166) {
                                    MapleAndroid ring = MapleAndroid.loadFromDb(equip.getItemId(), equip.getUniqueId());
                                    if (ring != null) {
                                        equip.setAndroid(ring);
                                    }
                                }
                            }
                        }
                        items.add(new 拍卖行_商品(mit, equip, 货币类型, 起步价, 最高报价, 报价角色ID, 到期时间));
                    } else {
                        Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getShort("flag"), rs.getInt("uniqueid"));
                        item.setOwner(rs.getString("owner"));
                        item.setInventoryId(rs.getLong("inventoryitemid"));
                        item.setExpiration(rs.getLong("expiredate"));
                        item.setGMLog(rs.getString("GM_Log"));
                        item.setGiftFrom(rs.getString("sender"));
                        //item.setExp(rs.getInt("exp")); //TODO: test
                        if (GameConstants.isPet(item.getItemId())) {
                            if (item.getUniqueId() > -1) {
                                MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                                if (pet != null) {
                                    item.setPet(pet);
                                }
                            } else {
                                //O_O hackish fix
                                item.setPet(MaplePet.createPet(item.getItemId(), MapleInventoryIdentifier.getInstance()));
                            }
                        }
                        items.add(new 拍卖行_商品(mit, item, 货币类型, 起步价, 最高报价, 报价角色ID, 到期时间));
                    }
                }
            }
            
            
            
            
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        return new Pair<>(总页数, items);
    }
    //type ：1 = 金币 2 = 点卷 3 = 余额
    public static 额度类型 取额度(MapleCharacter 物主, short type) throws SQLException{
        int PackageId = 取packageid(物主.getId());
        if(PackageId == -1){
            return null;
        }
        额度类型 额度 = new 额度类型();
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 拍卖行 WHERE PackageId = ?");
            ps.setInt(1, PackageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    switch (type) {
                        case 1:
                            额度.额度 = rs.getLong("金币");
                            额度.锁定额度 = 取锁定额度(物主.getId(), (byte) 1);
                            break;
                        case 2:
                            额度.额度 = rs.getLong("点卷");
                            额度.锁定额度 = 取锁定额度(物主.getId(), (byte) 2);
                            break;
                        case 3:
                            额度.额度 = rs.getLong("余额");
                            额度.锁定额度 = 取锁定额度(物主.getId(), (byte) 3);
                            break;
                    }

                }
                rs.close();
            }
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        额度.可用额度 = 额度.额度 - 额度.锁定额度;
        return 额度;
    }
    
    public static void 增减额度(MapleCharacter 物主, short type, int 额度) throws SQLException {
        int PackageId = 取packageid(物主.getId());
        if(PackageId == -1){
            return;
        }
        增减额度(PackageId, type, 额度);
    }
    
    public static void 增减额度(int PackageId, short type, int 额度) throws SQLException {
        增减额度(PackageId, type, 额度, true);
    }
    
    //                                                               负数减，正数加
    public static void 增减额度(int PackageId, short type, int 额度, boolean 执行锁) throws SQLException {
        if(执行锁){
            鎖.lock();
        }
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()){
            StringBuilder query = new StringBuilder();
            query.append("UPDATE `拍卖行` SET ");
            switch (type) {
                case 1:
                    query.append("金币=金币" + (额度 >= 0 ? "+" + 额度 : 额度));
                    break;
                case 2:
                    query.append("点卷=点卷" + (额度 >= 0 ? "+" + 额度 : 额度));
                    break;
                case 3:
                    query.append("余额=余额" + (额度 >= 0 ? "+" + 额度 : 额度));
                    break;
            }
            query.append(" WHERE PackageId = ?");
            PreparedStatement ps  = con.prepareStatement(query.toString());
            ps.setInt(1, PackageId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        } finally {
            if(执行锁){
                鎖.unlock();
            }
        }
    }
    
    public static long 取锁定额度(int 角色ID, byte 货币类型) throws SQLException{
        long 锁定额度 = 0;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 拍卖行_商品 WHERE 报价角色ID = ? and 货币类型 = ?");
            ps.setInt(1, 角色ID);
            ps.setByte(2, 货币类型);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    锁定额度 += rs.getInt("最高报价");
                }
                rs.close();
            }
            ps.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return 锁定额度;
    }
    
    public static void 删除物品(int inventoryitemid) throws SQLException{
        鎖.lock();
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            MapleInventoryType mit = null;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 拍卖行_商品 WHERE inventoryitemid = ?");
            ps.setInt(1, inventoryitemid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                }
                rs.close();
            }
            ps.close();
            if(mit == null){
                return;
            }
            
            if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                ps = con.prepareStatement("DELETE FROM `拍卖行_装备属性` WHERE `inventoryitemid` = ?");
                ps.setInt(1, inventoryitemid);
                ps.executeUpdate();
                ps.close();
            }
            ps = con.prepareStatement("DELETE FROM `拍卖行_商品` WHERE `inventoryitemid` = ?");
            ps.setInt(1, inventoryitemid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
            
        } finally {
            鎖.unlock();
        }
    }
    
    public static int 取角色ID(int packageid) throws SQLException{
        int 角色ID = -1;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 拍卖行 WHERE PackageId = ?");
            ps.setInt(1, packageid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    角色ID = rs.getInt(2);
                }
                rs.close();
            }
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
            
        }
        return 角色ID;
    }
    
    public static int 取packageid(int 角色ID) throws SQLException{
        int packageid = -1;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 拍卖行 WHERE characterid = ?");
            ps.setInt(1, 角色ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    packageid = rs.getInt(1);
                }
                rs.close();
            }
            
            ps.close();
            if(packageid == -1){
                ps = con.prepareStatement("INSERT INTO 拍卖行 (characterid, time) VALUES (?, ?)", DBConPool.RETURN_GENERATED_KEYS);
                ps.setInt(1, 角色ID);
                ps.setLong(2, System.currentTimeMillis());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    throw new RuntimeException("Error, adding merchant to DB");
                }
                packageid = rs.getInt(1);
                rs.close();
                ps.close();
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
            
        }
        return packageid;
    }
    
    
    public static void 加入商品(MapleCharacter 物主, MapleInventoryType 类型, short 物品位置, short 货币类型,int 起步价,short 上架数量) throws SQLException{
        final Item ivItem = 物主.getInventory(类型).getItem(物品位置).copy();
        ivItem.setQuantity(上架数量); 
        拍卖行_MYSQL.加入商品(new Pair<>(ivItem, 类型), 拍卖行_MYSQL.取packageid(物主.getId()),货币类型,起步价);
    }
    
    
    public static void 加入商品(Pair<Item, MapleInventoryType> items, int packageid, short 货币类型, int 起步价) throws SQLException {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()){
            if (items == null) {
                return;
            }
            Date now = new Date(); 
            
            StringBuilder query_2 = new StringBuilder("INSERT INTO `");
            query_2.append("拍卖行_商品");
            query_2.append("` (");
            query_2.append("packageid");
            query_2.append(", itemid, inventorytype, position, quantity, owner, GM_Log, uniqueid, expiredate, flag, `type`, sender, 货币类型, 起步价, 最高报价, `拍卖到期时间`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            PreparedStatement ps = con.prepareStatement(query_2.toString(), Statement.RETURN_GENERATED_KEYS);
            String valueStr = "";
            int values = 57;
            for (int i = 0; i < values; i++) {
                if (i == (values - 1)) {
                    valueStr += "?";
                } else {
                    valueStr += "?, ";
                }
            }
            PreparedStatement pse = con.prepareStatement("INSERT INTO 拍卖行_装备属性 VALUES (DEFAULT, " + valueStr + ")");
            Pair<Item, MapleInventoryType> pair = items;
                Item item = pair.getLeft();
                MapleInventoryType mit = pair.getRight();
                if (item.getPosition() == -55) {
                    return;
                }
                ps.setInt(1, packageid);
                ps.setInt(2, item.getItemId());
                ps.setInt(3, mit.getType());
                ps.setInt(4, item.getPosition());
                ps.setInt(5, item.getQuantity());
                ps.setString(6, item.getOwner());
                ps.setString(7, item.getGMLog());
                if (item.getPet() != null) { //expensif?
                    //item.getPet().saveToDb();
                    ps.setInt(8, Math.max(item.getUniqueId(), item.getPet().getUniqueId()));
                } else {
                    ps.setInt(8, item.getUniqueId());
                }
                ps.setLong(9, item.getExpiration());
                ps.setShort(10, item.getFlag());
                ps.setByte(11, (byte) 3);//type
                ps.setString(12, item.getGiftFrom());
                ps.setByte(13, (byte) 货币类型);
                ps.setInt(14, 起步价);
                ps.setInt(15, 起步价);
                ps.setLong(16, now.getTime() + (1 * 24 * 60 * 60 * 1000));
                ps.executeUpdate();
                final long iid;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        rs.close();
                        return;
                    }
                    iid = rs.getLong(1);
                }

                item.setInventoryId(iid);
                if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                    Equip equip = (Equip) item;
                    int i = 0;
                    pse.setLong(++i, iid);
                    pse.setInt(++i, equip.getUpgradeSlots());
                    pse.setInt(++i, equip.getLevel());
                    pse.setInt(++i, equip.getStr());
                    pse.setInt(++i, equip.getDex());
                    pse.setInt(++i, equip.getInt());
                    pse.setInt(++i, equip.getLuk());
                    pse.setInt(++i, equip.getHp());
                    pse.setInt(++i, equip.getMp());
                    pse.setInt(++i, equip.getWatk());
                    pse.setInt(++i, equip.getMatk());
                    pse.setInt(++i, equip.getWdef());
                    pse.setInt(++i, equip.getMdef());
                    pse.setInt(++i, equip.getAcc());
                    pse.setInt(++i, equip.getAvoid());
                    pse.setInt(++i, equip.getHands());
                    pse.setInt(++i, equip.getSpeed());
                    pse.setInt(++i, equip.getJump());
                    pse.setInt(++i, equip.getViciousHammer());
                    pse.setLong(++i, equip.getItemEXP());
                    pse.setInt(++i, equip.getDurability());
                    pse.setByte(++i, equip.getEnhance());
                    pse.setInt(++i, equip.getPotential1());
                    pse.setInt(++i, equip.getPotential2());
                    pse.setInt(++i, equip.getPotential3());
                    pse.setInt(++i, equip.getBonusPotential1());
                    pse.setInt(++i, equip.getBonusPotential2());
                    pse.setInt(++i, equip.getBonusPotential3());
                    pse.setInt(++i, equip.getFusionAnvil());
                    pse.setInt(++i, equip.getSocket1());
                    pse.setInt(++i, equip.getSocket2());
                    pse.setInt(++i, equip.getSocket3());
                    pse.setInt(++i, equip.getIncSkill());
                    pse.setShort(++i, equip.getCharmEXP());
                    pse.setShort(++i, equip.getPVPDamage());
                    pse.setByte(++i, equip.getEnhanctBuff());
                    pse.setByte(++i, equip.getReqLevel());
                    pse.setByte(++i, equip.getYggdrasilWisdom());
                    pse.setByte(++i, (byte) (equip.getFinalStrike() ? 1 : 0));
                    pse.setByte(++i, equip.getBossDamage());
                    pse.setByte(++i, equip.getIgnorePDR());
                    pse.setByte(++i, equip.getTotalDamage());
                    pse.setByte(++i, equip.getAllStat());
                    pse.setByte(++i, equip.getKarmaCount());
                    pse.setInt(++i, equip.取已打孔次数());
                    pse.setInt(++i, equip.取孔位状态(1));
                    pse.setInt(++i, equip.取孔位状态(2));
                    pse.setInt(++i, equip.取孔位状态(3));
                    pse.setInt(++i, equip.取孔位状态(4));
                    pse.setInt(++i, equip.取孔位状态(5));
                    pse.setInt(++i, equip.取孔位状态(6));
                    pse.setInt(++i, equip.取孔位状态(7));
                    pse.setInt(++i, equip.取孔位状态(8));
                    pse.setInt(++i, equip.取孔位状态(9));
                    pse.setInt(++i, equip.取孔位状态(10));
                    pse.setInt(++i, equip.取孔位状态(11));
                    pse.setInt(++i, equip.取孔位状态(12));
                    pse.executeUpdate();
                }
            
            pse.close();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }

    }
    
    public static void 修改物品最高报价(MapleCharacter Player, 拍卖行_商品 商品, int 报价) throws SQLException {
        int Player_PackageId = 取packageid(Player.getId());
        if(Player_PackageId == -1){
            return;
        }
        鎖.lock();
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()){
            int 当前最高报价 = 0, 报价角色ID = 0,PackageId = 0;
            
            boolean 叫价状态 = false;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `拍卖行_商品` WHERE inventoryitemid = ?");
            ps.setLong(1, 商品.物品.getInventoryId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PackageId = rs.getInt("packageid");
                    当前最高报价 = rs.getInt("最高报价");
                    报价角色ID = rs.getInt("报价角色ID");
                    
                }
                rs.close();
            }
            ps.close();
            
            if(PackageId == 0 && 报价角色ID == 0){
                Player.dropMessage(6, "[拍卖行] 该商品已下架!");
                return;
            } else if(Player_PackageId == PackageId){
                Player.dropMessage(6, "[拍卖行] 您不能拍自己的商品哦!");
                return;
            } else if(报价 <= 当前最高报价){
                Player.dropMessage(6, "[拍卖行] 非常抱歉您的报价需要高于当前最高报价哦,(当前最高报价：" + 当前最高报价 + ")(你的报价：" + 报价 + ")");
                return;
            }
            
            
            switch (商品.货币类型) {
                case 1:
                    额度类型 金币 = 取额度(Player, (short) 1);
                    if(金币.可用额度 >= 报价){
                        叫价状态 = true;
                        Player.dropMessage(6, "[拍卖行] 已锁定" + 报价 + "(金币).");
                    } else {
                        int 缺少额度 = (int) (报价 - 金币.可用额度);
                        if(Player.getMeso() >= 缺少额度){
                            叫价状态 = true;
                            Player.gainMeso(-缺少额度, false);
                            增减额度(Player_PackageId, (short) 1, 缺少额度, false);
                            Player.dropMessage(6, "[拍卖行] 拍卖金库 (金币) 额度不足,已为您从背包中转入金库并锁定" + 报价 + "(金币).");
                        } else {
                            Player.dropMessage(6, "[拍卖行] 金币不足,无法为您叫价");
			}
                    }
                    break;
                case 2:
                    额度类型 点卷 = 取额度(Player, (short) 2);
                    if(点卷.可用额度 >= 报价){
                        叫价状态 = true;
                        Player.dropMessage(6, "[拍卖行] 已锁定" + 报价 + "(点券).");
                    } else {
                        int 缺少额度 = (int) (报价 - 点卷.可用额度);
                        if(Player.getCSPoints(1) >= 缺少额度){
                            叫价状态 = true;
                            Player.modifyCSPoints(1, -缺少额度, false);
                            增减额度(Player_PackageId, (short) 2, 缺少额度, false);
                            Player.dropMessage(6, "[拍卖行] 拍卖金库 (点券) 额度不足,已为您从背包中转入金库并锁定" + 报价 + "(点券).");
                        } else {
                            Player.dropMessage(6, "[拍卖行] 点券不足,无法为您叫价");
			}
                    }
                    break;
                case 3:
                    额度类型 余额 = 取额度(Player, (short) 3);
                    if(余额.可用额度 >= 报价){
                        叫价状态 = true;
                        Player.dropMessage(6, "[拍卖行] 已锁定" + 报价 + "(余额).");
                    } else {
                        int 缺少额度 = (int) (报价 - 余额.可用额度);
                        if(Player.getmoneyb() >= 缺少额度){
                            叫价状态 = true;
                            Player.modifymoney(2, -缺少额度, true);
                            增减额度(Player_PackageId, (short) 3, 缺少额度, false);
                            Player.dropMessage(6, "[拍卖行] 拍卖金库 (余额) 额度不足,已为您从背包中转入金库并锁定" + 报价 + "(余额).");
                        } else {
                            Player.dropMessage(6, "[拍卖行] 余额不足,无法为您叫价");
			}
                    }
                    break;
            }
            
            if(!叫价状态){
                return;
            }
            
            if(报价 > 当前最高报价){
                StringBuilder query_2 = new StringBuilder("UPDATE `拍卖行_商品` SET 最高报价=?, 报价角色ID=? WHERE inventoryitemid = ?");
                ps = con.prepareStatement(query_2.toString());
                ps.setInt(1, 报价);
                ps.setInt(2, Player.getId());
                ps.setLong(3, 商品.物品.getInventoryId());
                ps.executeUpdate();
                ps.close();
                Player.dropMessage(6, "[拍卖行] 报价成功,您的报价为：" + 报价);
            }
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        } finally {
            鎖.unlock();
        }
    }
    
    public static String ID取角色名(int ID) throws SQLException{
        String name = "";
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                }
                rs.close();
            }
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
            
        }
        return name;
    }
    
    public static int 验证是否被拍下(int inventoryitemid) throws SQLException{
        int 报价角色ID = -1;
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 拍卖行_商品 WHERE inventoryitemid = ?");
            ps.setInt(1, inventoryitemid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    报价角色ID = rs.getInt("报价角色ID");
                }
                rs.close();
            }
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        return 报价角色ID;
    }
    
public static class 额度类型 {
    public long 额度;
    public long 锁定额度;
    public long 可用额度;
}

    private 拍卖行_MYSQL() {
    }
    
    
    
    
}
