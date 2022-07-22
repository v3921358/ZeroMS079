/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.pvp;

import client.MapleBuffStat;
import client.MapleCharacter;
import constants.ServerConfig;
import handling.channel.handler.AttackInfo;
import java.awt.Point;
import java.awt.Rectangle;
import server.MapleStatEffect;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.packet.CField;
import tools.packet.CWvsContext;

/**
 *
 * @author alienware
 */
public class MaplePvp {

    private static PvpAttackInfo parsePvpAttack(AttackInfo attack, MapleCharacter player, MapleStatEffect effect) {
        PvpAttackInfo ret = new PvpAttackInfo();
        double maxdamage = player.getLevel() + 100.0D;
        int skillId = attack.skill;
        ret.skillId = skillId;
        ret.critRate = 5;
        ret.ignoreDef = 0;
        ret.skillDamage = 100;
        ret.mobCount = 1;
        ret.attackCount = 1;
        int pvpRange = attack.isCloseRangeAttack ? 35 : 70;
        ret.facingLeft = (attack.animation < 0);

        if ((skillId != 0) && (effect != null)) {
            ret.skillDamage = effect.getDamage();
            ret.mobCount = Math.max(1, effect.getMobCount());
            ret.attackCount = Math.max(effect.getBulletCount(), effect.getAttackCount());
            ret.box = effect.calculateBoundingBox(player.getTruePosition(), ret.facingLeft, pvpRange);
        } else {
            ret.box = calculateBoundingBox(player.getTruePosition(), ret.facingLeft, pvpRange);
        }

        boolean mirror = player.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null;
        ret.attackCount *= (mirror ? 2 : 1);
        maxdamage *= ret.skillDamage / 100.0D;
        ret.maxDamage = (maxdamage * ret.attackCount);

        if (ServerConfig.logPackets) {
            player.dropMessage(5, "Pvp伤害解析 - 最大攻击: " + maxdamage + " 数量: " + ret.mobCount + " 次数: " + ret.attackCount + " 爆击: " + ret.critRate + " 无视: " + ret.ignoreDef + " 技能伤害: " + ret.skillDamage);
        }
        return ret;
    }

    private static Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, int range) {
        Point lt = new Point(-70, -30);
        Point rb = new Point(-10, 0);
        Point myrb;

        Point mylt;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x - range, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(lt.x * -1 + posFrom.x + range, rb.y + posFrom.y);
            mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
        }
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    public static boolean inArea(MapleCharacter chr) {
        for (Rectangle rect : chr.getMap().getAreas()) {
            if (rect.contains(chr.getTruePosition())) {
                return true;
            }
        }
        return false;
    }

    private static void monsterBomb(MapleCharacter player, MapleCharacter attacked, MapleMap map, PvpAttackInfo attack) {
        if ((player == null) || (attacked == null) || (map == null)) {
            return;
        }

        double maxDamage = attack.maxDamage;
        boolean isCritDamage = false;

        if (player.getLevel() > attacked.getLevel() + 10) {
            maxDamage *= 1.05D;
        } else if (player.getLevel() < attacked.getLevel() - 10) {
            maxDamage /= 1.05D;
        } else if (player.getLevel() > attacked.getLevel() + 20) {
            maxDamage *= 1.1D;
        } else if (player.getLevel() < attacked.getLevel() - 20) {
            maxDamage /= 1.1D;
        } else if (player.getLevel() > attacked.getLevel() + 30) {
            maxDamage *= 1.15D;
        } else if (player.getLevel() < attacked.getLevel() - 30) {
            maxDamage /= 1.15D;
        }

        if (server.Randomizer.nextInt(100) < attack.critRate) {
            maxDamage *= 1.5D;
            isCritDamage = true;
        }

        int attackedDamage = (int) Math.floor(Math.random() * ((int) maxDamage * 0.35D) + (int) maxDamage * 0.65D);
        int MAX_PVP_DAMAGE = (int) (player.getStat().getLimitBreak(player) / 100.0D);
        int MIN_PVP_DAMAGE = 100;
        if (attackedDamage > MAX_PVP_DAMAGE) {
            attackedDamage = MAX_PVP_DAMAGE;
        }
        if (attackedDamage < MIN_PVP_DAMAGE) {
            attackedDamage = MIN_PVP_DAMAGE;
        }

        int hploss = attackedDamage;
        int mploss = 0;

        if (attackedDamage > 0) {
            if (attacked.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null) {
                mploss = (int) (attackedDamage * (attacked.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0D));
                hploss -= mploss;
                if (attacked.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                    mploss = 0;
                } else if (mploss > attacked.getStat().getMp()) {
                    mploss = attacked.getStat().getMp();
                    hploss -= mploss;
                }
                attacked.addMPHP(-hploss, -mploss);
            } else {
                attacked.addHP(-hploss);
            }
        }

        MapleMonster pvpMob = MapleLifeFactory.getMonster(9400711);
        map.spawnMonsterOnGroundBelow(pvpMob, attacked.getPosition());
        map.broadcastMessage(CField.PVPdamagePlayer(attacked.getId(), 2, pvpMob.getId(), hploss));

        if (isCritDamage) {
            player.dropMessage(6, "你对玩家 " + attacked.getName() + " 造成了 " + hploss + " 点爆击伤害! 对方血量: " + attacked.getStat().getHp() + "/" + attacked.getStat().getCurrentMaxHp());
            attacked.dropMessage(6, "玩家 " + player.getName() + " 对你造成了 " + hploss + " 点爆击伤害!");
        } else {
            player.getClient().getSession().write(CWvsContext.getTopMsg("你对玩家 " + attacked.getName() + " 造成了 " + hploss + " 点伤害! 对方血量: " + attacked.getStat().getHp() + "/" + attacked.getStat().getCurrentMaxHp()));
            attacked.getClient().getSession().write(CWvsContext.getTopMsg("玩家 " + player.getName() + " 对你造成了 " + hploss + " 点伤害!"));

        }

        map.killMonster(pvpMob, player, false, false, (byte) 1);

        if ((attacked.getStat().getHp() <= 0) && (!attacked.isAlive())) {
            int expReward = attacked.getLevel() * 10 * (attacked.getLevel() / player.getLevel());
            int gpReward = (int) Math.floor(Math.random() * 10.0D + 10.0D);

            player.gainExp(expReward, true, false, true);

            player.dropMessage(6, "你击败了玩家 " + attacked.getName() + "!! ");

            attacked.dropMessage(6, player.getName() + " 将你击败!");
        }
    }

    public static synchronized void doPvP(MapleCharacter player, MapleMap map, AttackInfo attack, MapleStatEffect effect) {
        PvpAttackInfo pvpAttack = parsePvpAttack(attack, player, effect);

        int mobCount = 0;
        for (MapleCharacter attacked : player.getMap().getCharactersIntersect(pvpAttack.box)) {
            if ((attacked.getId() != player.getId()) && (attacked.isAlive()) && (!attacked.isHidden()) && (mobCount < pvpAttack.mobCount)) {
                mobCount++;

                monsterBomb(player, attacked, map, pvpAttack);
            }
        }
    }

    public static synchronized void doPartyPvP(MapleCharacter player, MapleMap map, AttackInfo attack, MapleStatEffect effect) {
        PvpAttackInfo pvpAttack = parsePvpAttack(attack, player, effect);
        int mobCount = 0;
        for (MapleCharacter attacked : player.getMap().getCharactersIntersect(pvpAttack.box)) {
            if ((attacked.getId() != player.getId()) && (attacked.isAlive()) && (!attacked.isHidden()) && ((player.getParty() == null) || (player.getParty() != attacked.getParty())) && (mobCount < pvpAttack.mobCount)) {
                mobCount++;
                monsterBomb(player, attacked, map, pvpAttack);
            }
        }
    }

    public static synchronized void doGuildPvP(MapleCharacter player, MapleMap map, AttackInfo attack, MapleStatEffect effect) {
        PvpAttackInfo pvpAttack = parsePvpAttack(attack, player, effect);
        int mobCount = 0;
        for (MapleCharacter attacked : player.getMap().getCharactersIntersect(pvpAttack.box)) {
            if ((attacked.getId() != player.getId()) && (attacked.isAlive()) && (!attacked.isHidden()) && ((player.getGuildId() == 0) || (player.getGuildId() != attacked.getGuildId())) && (mobCount < pvpAttack.mobCount)) {
                mobCount++;
                monsterBomb(player, attacked, map, pvpAttack);
            }
        }
    }

    private MaplePvp() {
    }
}
