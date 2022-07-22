/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client.inventory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Itzik
 */
public class MaplePotionPot {

    private int chrId;
    private int itemId;
    private int hp;
    private int mp;
    private int maxValue;
    private int maxlimit = 10000000;
    private long startTime;
    private long endTime;

    private MaplePotionPot(int chrId, int itemId, int maxValue, int hp, int mp, long start, long end) {
        this.chrId = chrId;
        this.itemId = itemId;
        this.maxValue = maxValue;
        this.hp = hp;
        this.mp = mp;
        this.startTime = start;
        this.endTime = end;
    }

    /*public static MaplePotionPot loadFromResult(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            int maxValue = rs.getInt("maxValue");
            int hp = rs.getInt("hp");
            int mp = rs.getInt("mp");
            long start = rs.getLong("startDate");
            long end = rs.getLong("endDate");
            return new MaplePotionPot(id, maxValue, hp, mp, start, end);
        } catch (SQLException ex) {
            return null;
        }
    }*/
    public int getChrId() {
        return this.chrId;
    }

    public int getItmeId() {
        return this.itemId;
    }

    public int getHp() {
        if (this.hp < 0) {
            this.hp = 0;
        } else if (this.hp > this.maxValue) {
            this.hp = this.maxValue;
        }
        return this.hp;
    }

    public void setHp(int value) {
        this.hp = value;
    }

    public void addHp(int value) {
        if (value <= 0) {
            return;
        }
        this.hp += value;
        if (this.hp > this.maxValue) {
            this.hp = this.maxValue;
        }
    }

    public int getMp() {
        if (this.mp < 0) {
            this.mp = 0;
        } else if (this.mp > this.maxValue) {
            this.mp = this.maxValue;
        }
        return this.mp;
    }

    public void setMp(int value) {
        this.mp = value;
    }

    public void addMp(int value) {
        if (value <= 0) {
            return;
        }
        this.mp += value;
        if (this.mp > this.maxValue) {
            this.mp = this.maxValue;
        }
    }

    public int getMaxValue() {
        if (this.maxValue > this.maxlimit) {
            this.maxValue = this.maxlimit;
        }
        return this.maxValue;
    }

    public void setMaxValue(int value) {
        this.maxValue = value;
    }

    public boolean addMaxValue() {
        if (this.maxValue + 1000000 > this.maxlimit) {
            return false;
        }
        this.maxValue += 1000000;
        return true;
    }

    public long getStartDate() {
        return this.startTime;
    }

    public long getEndDate() {
        return this.endTime;
    }

    public boolean isFullHp() {
        return getHp() >= getMaxValue();
    }

    public boolean isFullMp() {
        return getMp() >= getMaxValue();
    }

    public boolean isFull(int addHp, int addMp) {
        if ((addHp > 0) && (addMp > 0)) {
            return (isFullHp()) && (isFullMp());
        }
        if ((addHp > 0) && (addMp == 0)) {
            return isFullHp();
        }
        if ((addHp == 0) && (addMp >= 0)) {
            return isFullMp();
        }
        return true;
    }
}
