package fengshen.bean;

import java.util.Objects;


/**
 * @author 光影九曜 QQ: 150032099
 * @description [商城物品实体类]
 * @date 2021/3/29 18:24
 * @since 1.0.0
 */
public class CashShopItemBean {
    private int sn;
    private int id;
    private String name;
    private int price;

    private boolean soldOn = true;

    public CashShopItemBean(int sn, int id, String name, int price) {
        this.sn = sn;
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public CashShopItemBean(int sn, int id, String name, int price, int soldOn) {
        this(sn, id, name, price);
        this.soldOn = soldOn > 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;//一行多少个
        hash = 83 * hash + this.sn;
        hash = 83 * hash + this.id;
        hash = 83 * hash + Objects.hashCode(this.name);
        hash = 83 * hash + this.price;
        hash = 83 * hash + (this.soldOn ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CashShopItemBean other = (CashShopItemBean) obj;
        if (this.sn != other.sn) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        if (this.price != other.price) {
            return false;
        }
        if (this.soldOn != other.soldOn) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CashShopItemBean{" + "sn=" + sn + ", id=" + id + ", name=" + name + ", price=" + price + ", soldOn=" + soldOn + '}';
    }

    /**
     * @return the sn
     */
    public int getSn() {
        return sn;
    }

    /**
     * @param sn the sn to set
     */
    public void setSn(int sn) {
        this.sn = sn;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the price
     */
    public int getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * @return the soldOn
     */
    public boolean isSoldOn() {
        return soldOn;
    }

    /**
     * @param soldOn the soldOn to set
     */
    public void setSoldOn(boolean soldOn) {
        this.soldOn = soldOn;
    }

}
