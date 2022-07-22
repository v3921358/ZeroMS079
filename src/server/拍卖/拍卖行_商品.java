/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.拍卖;

import client.inventory.Item;
import client.inventory.MapleInventoryType;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
/**
 *
 * @author XM
 */
public class 拍卖行_商品 {
    public Item 物品;
    public byte 货币类型;
    public int 起步价;
    public int 最高报价;
    public int 报价角色ID;
    public long 拍卖到期时间;
    public MapleInventoryType 类型;
    
    public 拍卖行_商品() {
        this.物品 = null;
        this.类型 = null;
        this.货币类型 = 0;
        this.起步价 = 0;
        this.最高报价 = 0;
        this.报价角色ID = 0;
        this.拍卖到期时间 = 0;
    }
    
    public 拍卖行_商品(MapleInventoryType 类型,Item 物品, byte 货币类型,int 起步价, int 最高报价, int 报价角色ID,long 拍卖到期时间) {
        this.物品 = 物品;
        this.货币类型 = 货币类型;
        this.起步价 = 起步价;
        this.最高报价 = 最高报价;
        this.报价角色ID = 报价角色ID;
        this.类型 = 类型;
        this.拍卖到期时间 = 拍卖到期时间;
    }
}

