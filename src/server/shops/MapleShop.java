package server.shops;

import client.MapleClient;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import database.DBConPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class MapleShop {

    private static final Set<Integer> rechargeableItems = new LinkedHashSet();
    private final int id;
    private final int npcId;
    private final List<MapleShopItem> items = new LinkedList();
    private final List<Pair<Integer, String>> ranks = new ArrayList();
    private static final Logger logger = LogManager.getLogger(MapleShop.class);

        static {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (int i = 2070000; i <= 2070026; i++) {
            if (ii.itemExists(i)) {
                rechargeableItems.add(i);
            }
        }
        for (int i = 2330000; i <= 2332000; i++) {
            if (ii.itemExists(i)) {
                rechargeableItems.add(i);
            }
        }
    }
    //rechargeableItems.add(2070019); //高科技电光镖
        //rechargeableItems.add(2070016);
  /*      for (int i = 2079987; i <= 2079999; i++) {//加上这句话，新增加的镖可以充值了
            rechargeableItems.add(Integer.valueOf(i));
        }
        for (int i = 2070000; i <= 2070021; i++) {
            rechargeableItems.add(Integer.valueOf(i));
        }
        for (int i = 2070023; i <= 2070026; i++) {
            rechargeableItems.add(Integer.valueOf(i));
        }
        rechargeableItems.remove(Integer.valueOf(2070014));
        rechargeableItems.remove(Integer.valueOf(2070015));
        //rechargeableItems.remove(Integer.valueOf(2070016));
        rechargeableItems.remove(Integer.valueOf(2070017));
        rechargeableItems.remove(Integer.valueOf(2070018));
        //rechargeableItems.remove(Integer.valueOf(2070019));
        rechargeableItems.remove(Integer.valueOf(2070020));
        rechargeableItems.remove(Integer.valueOf(2070021));

        for (int i = 2330000; i <= 2330006; i++) {
            rechargeableItems.add(Integer.valueOf(i));
        }
        rechargeableItems.add(Integer.valueOf(2331000));
        rechargeableItems.add(Integer.valueOf(2332000));
    }*/

    public MapleShop(int id, int npcId) {
        this.id = id;
        this.npcId = npcId;
    }

    public void addItem(MapleShopItem item) {
        this.items.add(item);
    }

    public List<MapleShopItem> getItems() {
        return this.items;
    }

    public void sendShop(MapleClient c) {
        if (c != null && c.getPlayer() != null) {
            MapleNPC npc = MapleLifeFactory.getNPC(getNpcId());
            if (npc == null || npc.getName().equals("MISSINGNO")) {
                c.getPlayer().dropMessage(1, "商店" + id + "找不到此代码为" + getNpcId() + "的Npc");
                return;
            } else if (c.getPlayer().isAdmin() || ServerConfig.logPackets) {
                c.getPlayer().dropMessage(5, "您已建立与商店" + id + "的连接");
                MapleShopFactory.getInstance().clear();
            }
            c.getPlayer().setShop(this);
            c.getSession().write(CField.NPCPacket.getNPCShop(getNpcId(), this, c));
        }
    }

    public void sendShop(MapleClient c, int customNpc) {
        if (c != null && c.getPlayer() != null) {
            MapleNPC npc = MapleLifeFactory.getNPC(getNpcId());
            if (npc == null || npc.getName().equals("MISSINGNO")) {
                c.getPlayer().dropMessage(1, "商店" + id + "找不到此代码为" + getNpcId() + "的Npc");
                return;
            } else if (c.getPlayer().isAdmin()) {
                c.getPlayer().dropMessage(5, "您已建立与商店" + id + "的连接");
                MapleShopFactory.getInstance().clear();
            }
            c.getPlayer().setShop(this);
            c.getSession().write(CField.NPCPacket.getNPCShop(customNpc, this, c));
        }
    }

    public void buy(MapleClient c, short slot, int itemId, short quantity) {
        if ((itemId / 10000 == 190) && (!GameConstants.isMountItemAvailable(itemId, c.getPlayer().getJob()))) {
            c.getPlayer().dropMessage(1, "你不可以购买这个道具。");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        MapleShopItem item = findById(itemId);

        if ((item != null) && (item.getPrice() > 0) && (item.getReqItem() == 0)) {

            int price = GameConstants.isRechargable(itemId) ? item.getPrice() : item.getPrice() * quantity;
            if ((price >= 0) && (c.getPlayer().getMeso() >= price)) {
                if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                    c.getPlayer().gainMeso(-price, false);
                    if (GameConstants.isPet(itemId)) {
                        MapleInventoryManipulator.addById(c, itemId, (short) (quantity), "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, false, "Bought from shop " + id + ", " + npcId + " on " + FileoutputUtil.CurrentReadable_Date());
                    } else {
                        if (GameConstants.isRechargable(itemId)) {
                            quantity = ii.getSlotMax(item.getItemId());
                        }

                        MapleInventoryManipulator.addById(c, itemId, quantity, "Bought from shop " + this.id + ", " + this.npcId + " on " + FileoutputUtil.CurrentReadable_Date());
                    }
                } else {
                    c.getPlayer().dropMessage(1, "你的背包栏已满。");
                }
                if (ServerConfig.logs_npcshop_buy) {
                    FileoutputUtil.logToFile("logs/玩家操作/NPC商店购买.txt", "\r\n 时间　[" + FileoutputUtil.NowTime() + "] IP: " + c.getSessionIPAddress() + " 玩家 " + c.getAccountName() + " " + c.getPlayer().getName() + " 从  NPCID：" + npcId + " 商店ID：" + id + " 的商店购买了" + MapleItemInformationProvider.getInstance().getName(itemId) + " (" + itemId + ") x" + quantity + " 价格为 : " + price);
                }
                c.getSession().write(CField.NPCPacket.confirmShopTransaction((byte) 0, this, c, -1));
            }
        } else if ((item != null) && (item.getReqItem() > 0) && (quantity == 1) && (c.getPlayer().haveItem(item.getReqItem(), item.getReqItemQ(), false, true))) {
            if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(item.getReqItem()), item.getReqItem(), item.getReqItemQ(), false, false);
                if (GameConstants.isPet(itemId)) {
                    MapleInventoryManipulator.addById(c, itemId, (short) (quantity), "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1, false, "Bought from shop " + id + ", " + npcId + " on " + FileoutputUtil.CurrentReadable_Date());
                } else {
                    if (GameConstants.isRechargable(itemId)) {
                        quantity = ii.getSlotMax(item.getItemId());
                    }
                    MapleInventoryManipulator.addById(c, itemId, quantity, "Bought from shop " + this.id + ", " + this.npcId + " on " + FileoutputUtil.CurrentReadable_Date());
                }
                if (ServerConfig.logs_npcshop_buy) {
                    FileoutputUtil.logToFile("logs/玩家操作/NPC商店购买.txt", "\r\n 时间　[" + FileoutputUtil.NowTime() + "] IP: " + c.getSessionIPAddress() + " 玩家 " + c.getAccountName() + " " + c.getPlayer().getName() + " 从  NPCID：" + npcId + " 商店ID：" + id + " 的商店购买了" + MapleItemInformationProvider.getInstance().getName(itemId) + " (" + itemId + ") x" + quantity + " item.getReqItem() : " + item.getReqItem() + " item.getReqItemQ():" + item.getReqItemQ());
                }
            } else {
                c.getPlayer().dropMessage(1, "你的背包栏已满。");
            }
            c.getSession().write(CField.NPCPacket.confirmShopTransaction((byte) 0, this, c, -1));
        }
    }

    public void sell(MapleClient c, MapleInventoryType type, byte slot, short quantity) {
        if ((quantity == 65535) || (quantity == 0)) {
            quantity = 1;
        }
        Item item = c.getPlayer().getInventory(type).getItem(slot);
        if (item == null) {
            return;
        }

        if ((GameConstants.isThrowingStar(item.getItemId())) || (GameConstants.isBullet(item.getItemId()))) {
            quantity = item.getQuantity();
        }

        short iQuant = item.getQuantity();
        if (iQuant == 65535) {
            iQuant = 1;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if ((ii.cantSell(item.getItemId())) || (GameConstants.isPet(item.getItemId()))) {
            return;
        }

        List<Item> listRebuy = new ArrayList<>();
        if ((quantity <= iQuant) && (iQuant > 0)) {
            /*if (GameConstants.GMS) {
                if (item.getQuantity() == quantity) {
                    if (c.getPlayer().getRebuy().size() < 10) {
                        c.getPlayer().getRebuy().add(item.copy());
                    } else if (c.getPlayer().getRebuy().size() == 10) {
                        for (int i = 1; i < 10; i++) {
                            listRebuy.add(c.getPlayer().getRebuy().get(i));
                        }
                        listRebuy.add(item.copy());
                        c.getPlayer().setRebuy(listRebuy);
                    } else {
                        int x = c.getPlayer().getRebuy().size();
                        for (int i = x - 10; i < x; i++) {
                            listRebuy.add(c.getPlayer().getRebuy().get(i));
                        }
                        c.getPlayer().setRebuy(listRebuy);
                    }
                } else {
                    c.getPlayer().getRebuy().add(item.copyWithQuantity(quantity));
                }
            }*/
            MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
            double price;
            if ((GameConstants.isThrowingStar(item.getItemId())) || (GameConstants.isBullet(item.getItemId()))) {
                price = ii.getWholePrice(item.getItemId()) / (double) ii.getSlotMax(item.getItemId());
            } else {
                price = ii.getPrice(item.getItemId());
            }
            if (item.getItemId() == 2022195) {
                price = 1;
            }
            if (item.getItemId() == 4031348) {
                price = 1;
            }
            int recvMesos = (int) Math.max(Math.ceil(price * quantity), 0.0D);
            if ((GameConstants.isThrowingStar(item.getItemId())) || (GameConstants.isBullet(item.getItemId()))) {
                recvMesos = (int) ((double) ii.getWholePrice(item.getItemId()) + ((double) ii.getPrice(item.getItemId()) * (double) quantity));
            }
            if ((price != -1.0D) && (recvMesos > 0)) {
                c.getPlayer().gainMeso(recvMesos, false);
            }
            if (ServerConfig.logs_npcshop_buy) {
                FileoutputUtil.logToFile("logs/玩家操作/NPC商店卖出.txt", "\r\n 时间　[" + FileoutputUtil.NowTime() + "] IP: " + c.getSessionIPAddress() + " 玩家 " + c.getAccountName() + " " + c.getPlayer().getName() + " 从  NPCID：" + npcId + " 商店ID：" + id + " 的商店卖出了" + MapleItemInformationProvider.getInstance().getName(item.getItemId()) + " (" + item.getItemId() + ") x" + quantity + " 价格为 : " + recvMesos);
            }
            c.getSession().write(CField.NPCPacket.confirmShopTransaction((byte) 0x8, this, c, -1));
        }
    }

    public void recharge(MapleClient c, byte slot) {
        Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);

        if ((item == null) || ((!GameConstants.isThrowingStar(item.getItemId())) && (!GameConstants.isBullet(item.getItemId())))) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        short slotMax = ii.getSlotMax(item.getItemId());
        int skill = GameConstants.getMasterySkill(c.getPlayer().getJob());

        if (skill != 0) {
            slotMax = (short) (slotMax + c.getPlayer().getTotalSkillLevel(SkillFactory.getSkill(skill)) * 10);
        }
        if (item.getQuantity() < slotMax) {
            int price = (int) Math.round(ii.getPrice(item.getItemId()) * (slotMax - item.getQuantity()));
            if (c.getPlayer().getMeso() >= price) {
                item.setQuantity(slotMax);
                c.getSession().write(CWvsContext.InventoryPacket.updateInventorySlot(MapleInventoryType.USE, item, false));
                c.getPlayer().gainMeso(-price, false, false);
                c.getSession().write(CField.NPCPacket.confirmShopTransaction((byte) 0x8, this, c, -1));
            }
        }
    }

    protected MapleShopItem findById(int itemId) {
        for (MapleShopItem item : this.items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public static MapleShop createFromDB(int id, boolean isShopId) {
        MapleShop ret = null;

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement(isShopId ? "SELECT * FROM shops WHERE shopid = ?" : "SELECT * FROM shops WHERE npcid = ?");
            int shopId;
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                shopId = rs.getInt("shopid");
                ret = new MapleShop(shopId, rs.getInt("npcid"));
                rs.close();
                ps.close();
            } else {
                rs.close();
                ps.close();
                return null;
            }
            ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            List<Integer> recharges = new ArrayList(rechargeableItems);
            while (rs.next()) {
                if (ii.itemExists(rs.getInt("itemid"))) {
                    if ((GameConstants.isThrowingStar(rs.getInt("itemid"))) || (GameConstants.isBullet(rs.getInt("itemid")))) {
                        MapleShopItem starItem = new MapleShopItem((short) 1, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("reqitem"), rs.getInt("reqitemq"));
                        ret.addItem(starItem);
                        if (rechargeableItems.contains(starItem.getItemId())) {
                            recharges.remove(Integer.valueOf(starItem.getItemId()));
                        }
                    } else {
                        ret.addItem(new MapleShopItem((short) 1000, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("reqitem"), rs.getInt("reqitemq")));
                    }
                }
            }
            for (Integer recharge : recharges) {
                ret.addItem(new MapleShopItem((short) 1000, recharge, 0, 0, 0));
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM shopranks WHERE shopid = ? ORDER BY rank ASC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (ii.itemExists(rs.getInt("itemid"))) {
                    ret.ranks.add(new Pair(Integer.valueOf(rs.getInt("itemid")), rs.getString("name")));
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        return ret;
    }

    public int getNpcId() {
        return this.npcId;
    }

    public int getId() {
        return this.id;
    }

    public List<Pair<Integer, String>> getRanks() {
        return this.ranks;
    }
}
