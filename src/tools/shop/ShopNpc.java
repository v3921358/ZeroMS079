package tools.shop;

public class ShopNpc
{
    private int npcId;
    private String npcName;
    private int shopId;
    
    public ShopNpc() {
    }
    
    public ShopNpc(final int npcId, final int shopId, final String name) {
        this.npcId = npcId;
        this.shopId = shopId;
        this.npcName = name;
    }
    
    @Override
    public String toString() {
        if (this.npcId == 0) {
            return "" + this.getNpcName();
        }
        return "【商店序号: "+ shopId +"】"+  "【商店ID: "+ npcId +"】"+"【商店名称: "+ getNpcName() +"】";
    }
    
    public int getNpcId() {
        return this.npcId;
    }
    
    public void setNpcId(final int npcId) {
        this.npcId = npcId;
    }
    
    public String getNpcName() {
        return this.npcName;
    }
    
    public void setNpcName(final String npcName) {
        this.npcName = npcName;
    }
    
    public int getShopId() {
        return this.shopId;
    }
    
    public void setShopId(final int shopId) {
        this.shopId = shopId;
    }
}
