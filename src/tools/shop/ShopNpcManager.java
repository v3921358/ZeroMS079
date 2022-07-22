package tools.shop;

import server.MapleItemInformationProvider;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import database.DBConPool;
import java.util.ArrayList;
import java.util.List;

public class ShopNpcManager
{
    public static List<ShopNpc> getAllShopNpc() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List<ShopNpc> shopnpcs = (List<ShopNpc>)new ArrayList();
        try {
            final Connection conn = DBConPool.getInstance().getDataSource().getConnection();
            ps = conn.prepareStatement("select t.*,tt.name from shops t left join wz_npcnamedata tt on t.npcid = tt.npc");
            rs = ps.executeQuery();
            while (rs.next()) {
                final int npcId = rs.getInt("npcid");
                final int shopId = rs.getInt("shopId");
                final String npcName = rs.getString("name");
                shopnpcs.add(new ShopNpc(npcId, shopId, npcName));
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        return shopnpcs;
    }
    
    public static List<Merchandise> getAllShopItem(final int shopId) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List<Merchandise> shopItems = (List<Merchandise>)new ArrayList();
        try {
            final Connection conn = DBConPool.getInstance().getDataSource().getConnection();
            ps = conn.prepareStatement("select t.*,tt.name from shopitems t left  join wz_itemdata tt on t.itemid = tt.itemid where t.shopid = ? ");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            while (rs.next()) {
                final int shopId_ = rs.getInt("shopid");
                final String itemName = rs.getString("name");
                final int itemId = rs.getInt("itemid");
                final int price = rs.getInt("price");
                shopItems.add(new Merchandise(shopId, itemName, itemId, price));
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        return shopItems;
    }
    
    public static int updateShopData(final Merchandise merchandise) throws SQLException {
        PreparedStatement ps = null;
        int resulet = 0;
        final List<Merchandise> shopItems = (List<Merchandise>)new ArrayList();
        final Connection conn = DBConPool.getInstance().getDataSource().getConnection();
        try {
            ps = conn.prepareStatement("update shopitems set price = ?,reqitem = ?,reqitemq = ?,quantity = ? where shopitemid = ?");
            ps.setInt(1, merchandise.getPrice());
            resulet = ps.executeUpdate();
        }
        catch (SQLException ex) {
            Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        return resulet;
    }
    
    public static int addShopData(final Merchandise merchandise) throws SQLException {
        PreparedStatement ps = null;
        int resulet = 0;
        final List<Merchandise> shopItems = (List<Merchandise>)new ArrayList();
        final Connection conn = DBConPool.getInstance().getDataSource().getConnection();
        try {
            ps = conn.prepareStatement("insert into shopitems(shopid,itemid,price,reqitem,reqitemq,position,quantity,buyable) values(?,?,?,?,?,?,?,?)");
            ps.setInt(1, merchandise.getShopId());
            ps.setInt(2, merchandise.getItemId());
            ps.setInt(3, merchandise.getPrice());
            ps.setInt(6, getMaxPosition(merchandise.getShopId()) + 1);
            ps.setInt(8, (int)MapleItemInformationProvider.getInstance().getSlotMax(merchandise.getItemId()));
            resulet = ps.executeUpdate();
        }
        catch (SQLException ex) {
            Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        return resulet;
    }
    
    private static int getMaxPosition(final int shopId) throws SQLException {
        int position = -1;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Connection conn = DBConPool.getInstance().getDataSource().getConnection();
        try {
            ps = conn.prepareStatement("select max(position) as position from shopitems where shopid = ?");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            while (rs.next()) {
                position = rs.getInt("position");
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
        }
        return position;
    }
    
    public static int deleteShopData(final int shopItemId) throws SQLException {
        PreparedStatement ps = null;
        final Connection conn = DBConPool.getInstance().getDataSource().getConnection();
        int success = 0;
        try {
            ps = conn.prepareStatement("delete from shopitems where shopitemid = ?");
            ps.setInt(1, shopItemId);
            success = ps.executeUpdate();
            return success;
        }
        catch (SQLException ex) {
            Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
        }
        finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                Logger.getLogger(ShopNpcManager.class.getName()).log(Level.SEVERE, null, (Throwable)ex2);
            }
            return success;
        }
    }

    private ShopNpcManager() {
    }
}
