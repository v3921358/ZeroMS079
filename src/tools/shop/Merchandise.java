package tools.shop;

public class Merchandise
{
    private int shopId;
    private String itemName;
    private int itemId;
    private int price;
    
    public Merchandise(final int shopId,final String itemName, final int itemId, final int price) {
        this.shopId = shopId;
       this.itemName = itemName;
        this.itemId = itemId;
        this.price = price;
    }
    
    public Merchandise( final int shopId, final int itemId, final int price) {
        this.shopId = shopId;
        this.itemId = itemId;
        this.price = price;
    }
    
    
    public int getShopId() {
        return this.shopId;
    }
    
    public void setShopId(final int shopId) {
        this.shopId = shopId;
    }
    
    public int getItemId() {
        return this.itemId;
    }
    
    public void setItemId(final int itemId) {
        this.itemId = itemId;
    }
    
    public int getPrice() {
        return this.price;
    }
    
    public void setPrice(final int price) {
        this.price = price;
    }
    
    
    public String getItemName() {
        return this.itemName;
    }
    
    public void setItemName(final String itemName) {
        this.itemName = itemName;
    }
}
