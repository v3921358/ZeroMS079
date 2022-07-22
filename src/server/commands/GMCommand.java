/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server.commands;

import client.*;
import client.inventory.*;
import constants.GameConstants;
import constants.ServerConstants;
import constants.ServerConstants.PlayerGMRank;
import handling.channel.ChannelServer;
import handling.world.World;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import scripting.EventInstanceManager;
import scripting.EventManager;
import scripting.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.commands.InternCommand.Ban;
import server.commands.InternCommand.TempBan;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleReactor;
import server.quest.MapleQuest;
import server.shops.MapleShopFactory;
import tools.LoadPacket;
import tools.Pair;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InventoryPacket;

/**
 *
 * @author Emilyx3
 */
public class GMCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.GM;
    }

    public static class Fly extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            SkillFactory.getSkill(1146).getEffect(1).applyTo(c.getPlayer());
            SkillFactory.getSkill(1142).getEffect(1).applyTo(c.getPlayer());
            c.getPlayer().dispelBuff(1146);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Fly - Fly").toString();
        }
    }

    public static class FlyPerson extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            SkillFactory.getSkill(1146).getEffect(1).applyTo(chr);
            SkillFactory.getSkill(1142).getEffect(1).applyTo(chr);
            chr.dispelBuff(1146);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!FlyPerson - FlyPerson").toString();
        }
    }

    public static class FlyMap extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                SkillFactory.getSkill(1146).getEffect(1).applyTo(mch);
                SkillFactory.getSkill(1142).getEffect(1).applyTo(mch);
                mch.dispelBuff(1146);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!FlyMap - FlyMap").toString();
        }
    }

    public static class GivePet extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 6) {
                c.getPlayer().dropMessage(0, splitted[0] + " <character name> <petid> <petname> <petlevel> <petcloseness> <petfullness>");
                return false;
            }
            MapleCharacter petowner = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int id = Integer.parseInt(splitted[2]);
            String name = splitted[3];
            int level = Integer.parseInt(splitted[4]);
            int closeness = Integer.parseInt(splitted[5]);
            int fullness = Integer.parseInt(splitted[6]);
            long period = 20000;
            short flags = 0;
            if (id >= 5001000 || id < 5000000) {
                c.getPlayer().dropMessage(0, "Invalid pet id.");
                return false;
            }
            if (level > 30) {
                level = 30;
            }
            if (closeness > 30000) {
                closeness = 30000;
            }
            if (fullness > 100) {
                fullness = 100;
            }
            if (level < 1) {
                level = 1;
            }
            if (closeness < 0) {
                closeness = 0;
            }
            if (fullness < 0) {
                fullness = 0;
            }
            try {
                MapleInventoryManipulator.addById(petowner.getClient(), id, (short) 1, "", MaplePet.createPet(id, name, level, closeness, fullness, MapleInventoryIdentifier.getInstance(), id == 5000054 ? (int) period : 0, flags), 45, false, null);
            } catch (NullPointerException ex) {
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!GivePet - 给宠物").toString();
        }
    }

    public static class OpenNpc extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            NPCScriptManager.getInstance().start(c, Integer.parseInt(splitted[1]), splitted.length > 2 ? StringUtil.joinStringFrom(splitted, 2) : splitted[1]);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!OpenNpc - 打开NPC").toString();
        }
    }

    public static class OpenShop extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleShopFactory.getInstance().getShop(Integer.parseInt(splitted[1]));
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!OpenShop - 打开NPC商店").toString();
        }
    }

    public static class 清理地板 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().getMap().removeDrops();
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!清理地板 - 移除地上的物品").toString();
        }
    }

    public static class GetSkill extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            Skill skill = SkillFactory.getSkill(Integer.parseInt(splitted[1]));
            byte level = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);
            byte masterlevel = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1);

            if (level > skill.getMaxLevel()) {
                level = (byte) skill.getMaxLevel();
            }
            if (masterlevel > skill.getMaxLevel()) {
                masterlevel = (byte) skill.getMaxLevel();
            }
            c.getPlayer().changeSingleSkillLevel(skill, level, masterlevel);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!GetSkill - 获得技能").toString();
        }
    }

    public static class Fame extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter player = c.getPlayer();
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "Syntax: !fame <player> <amount>");
                return false;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int fame;
            try {
                fame = Integer.parseInt(splitted[2]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(6, "Invalid Number...");
                return false;
            }
            if (victim != null && player.allowedToTarget(victim)) {
                victim.addFame(fame);
                victim.updateSingleStat(MapleStat.FAME, victim.getFame());
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Fame - 给人气").toString();
        }
    }

    public static class SP extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().setRemainingSp(CommandProcessorUtil.getOptionalIntArg(splitted, 1, 1));
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, c.getPlayer().getRemainingSp());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!sp - 获得sp").toString();
        }
    }

    public static class AP extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String[] splitted) {
            c.getPlayer().setRemainingAp((short) CommandProcessorUtil.getOptionalIntArg(splitted, 1, 1));
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, CommandProcessorUtil.getOptionalIntArg(splitted, 1, 1));
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!AP - 增加AP").toString();
        }
    }

    public static class SP2 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().setRemainingSp(CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1), Integer.parseInt(splitted[1]));
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLESP, 0);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!sp2 - 获得sp").toString();
        }
    }

    public static class 转职 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            int jobid = Integer.parseInt(splitted[1]);
            if (!MapleJob.isExist(jobid)) {
                c.getPlayer().dropMessage(5, "Invalid Job");
                return false;
            }
            c.getPlayer().changeJob((short) jobid);
            c.getPlayer().setSubcategory(c.getPlayer().getSubcategory());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!转职 - 职业代码").toString();
        }
    }

    public static class 给玩家转职 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (!MapleJob.isExist(Integer.parseInt(splitted[2]))) {
                c.getPlayer().dropMessage(5, "Invalid Job");
                return false;
            }
            victim.changeJob((short) Integer.parseInt(splitted[2]));
            c.getPlayer().setSubcategory(c.getPlayer().getSubcategory());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!给玩家转职 - 给转职").toString();
        }
    }

    public static class Shop extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleShopFactory shop = MapleShopFactory.getInstance();
            int shopId = Integer.parseInt(splitted[1]);
            if (shop.getShop(shopId) != null) {
                shop.getShop(shopId).sendShop(c);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Shop - 打开NPC商店").toString();
        }
    }

    public static class LevelUp extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 2) {
                c.getPlayer().levelUp();
            } else {
                int up = 0;
                try {
                    up = Integer.parseInt(splitted[1]);
                } catch (Exception ex) {

                }
                for (int i = 0; i < up; i++) {
                    c.getPlayer().levelUp();
                }
            }
            c.getPlayer().setExp(0);
            c.getPlayer().updateSingleStat(MapleStat.EXP, 0);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!LevelUp - 升多少级").toString();
        }
    }

    public static class LevelUpTill extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            //for (int i = 0; i < Integer.parseInt(splitted[1]) - c.getPlayer().getLevel(); i++) {
            while (c.getPlayer().getLevel() < Integer.parseInt(splitted[1])) {
                if (c.getPlayer().getLevel() < 255) {
                    c.getPlayer().levelUp();
                }
            }
            //}
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!LevelUpTill - 升多少级").toString();
        }
    }

    public static class 给等级 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            //for (int i = 0; i < Integer.parseInt(splitted[2]) - victim.getLevel(); i++) {
            while (victim.getLevel() < Integer.parseInt(splitted[2])) {
                if (victim.getLevel() < 255) {
                    victim.levelUp();
                }
            }
            //}
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!给等级 - 给升多少级").toString();
        }
    }

    public static class 刷 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            final int itemId = Integer.parseInt(splitted[1]);
            final short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);

            if (!c.getPlayer().isAdmin()) {
                for (int i : GameConstants.itemBlock) {
                    if (itemId == i) {
                        c.getPlayer().dropMessage(5, "Sorry but this item is blocked for your GM level.");
                        return false;
                    }
                }
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " does not exist");
            } else {
                Item item;
                short flag = (short) ItemFlag.LOCK.getValue();

                if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
                    item = ii.getEquipById(itemId);
                } else {
                    item = new Item(itemId, (byte) 0, quantity, (byte) 0);

                }
                //if (!c.getPlayer().isSuperGM()) {
                //    item.setFlag(flag);
                //}
                if (!c.getPlayer().isAdmin()) {
                    item.setOwner(c.getPlayer().getName());
                    item.setGMLog(c.getPlayer().getName() + " used !getitem");
                }
                MapleInventoryManipulator.addbyItem(c, item);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!刷 - 刷物品").toString();
        }
    }

    public static class PotentialItem extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            final int itemId = Integer.parseInt(splitted[1]);
            if (!c.getPlayer().isAdmin()) {
                for (int i : GameConstants.itemBlock) {
                    if (itemId == i) {
                        c.getPlayer().dropMessage(5, "Sorry but this item is blocked for your GM level.");
                        return false;
                    }
                }
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (itemId >= 2000000) {
                c.getPlayer().dropMessage(5, "You can only get equips.");
            } else if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " does not exist");
            } else {
                Equip equip;
                equip = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                equip.setPotential1(Integer.parseInt(splitted[2]));
                equip.setPotential2(Integer.parseInt(splitted[3]));
                equip.setPotential3(Integer.parseInt(splitted[4]));
                equip.setBonusPotential1(Integer.parseInt(splitted[5]));
                equip.setBonusPotential2(Integer.parseInt(splitted[6]));
                equip.setBonusPotential3(Integer.parseInt(splitted[7]));
                equip.setOwner(c.getPlayer().getName());
                MapleInventoryManipulator.addbyItem(c, equip);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!PotentialItem - 制作装备潜能").toString();
        }
    }

    public static class ProItem extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (!c.getPlayer().isAdmin()) {
                return false;
            }
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(5, "!proitem id stats potential stats");
                return false;
            }
            final int itemId = Integer.parseInt(splitted[1]);
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (itemId >= 2000000) {
                c.getPlayer().dropMessage(5, "You can only get equips.");
            } else if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " does not exist");
            } else {
                Equip equip;
                equip = ii.randomizeStats((Equip) ii.getEquipById(itemId));
                equip.setStr(Short.parseShort(splitted[2]));
                equip.setDex(Short.parseShort(splitted[2]));
                equip.setInt(Short.parseShort(splitted[2]));
                equip.setLuk(Short.parseShort(splitted[2]));
                equip.setWatk(Short.parseShort(splitted[2]));
                equip.setMatk(Short.parseShort(splitted[2]));
                equip.setPotential1(Integer.parseInt(splitted[3]));
                equip.setPotential2(Integer.parseInt(splitted[3]));
                equip.setPotential3(Integer.parseInt(splitted[3]));
                equip.setOwner(c.getPlayer().getName());
                MapleInventoryManipulator.addbyItem(c, equip);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!ProItem - 制作装备").toString();
        }
    }

    public static class 等级 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().setLevel(Short.parseShort(splitted[1]));
            c.getPlayer().updateSingleStat(MapleStat.LEVEL, Integer.parseInt(splitted[1]));
            c.getPlayer().setExp(0);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!等级 - 调等级").toString();
        }
    }

    public static class 给玩家等级 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setLevel(Short.parseShort(splitted[2]));
            victim.updateSingleStat(MapleStat.LEVEL, Integer.parseInt(splitted[2]));
            victim.setExp(0);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!给玩家等级 - 名字 等级").toString();
        }
    }

    public static class StartAutoEvent extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            final EventManager em = c.getChannelServer().getEventSM().getEventManager("AutomatedEvent");
            if (em != null) {
                em.setWorldEvent();
                em.scheduleRandomEvent();
                System.out.println("Scheduling Random Automated Event.");
            } else {
                System.out.println("Could not locate Automated Event script.");
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!StartAutoEvent - StartAutoEvent").toString();
        }
    }

    public static class SetEvent extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleEvent.onStartEvent(c.getPlayer());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!SetEvent - SetEvent").toString();
        }
    }

    public static class AutoEvent extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleEvent.onStartEvent(c.getPlayer());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!AutoEvent - AutoEvent").toString();
        }
    }

    public static class StartEvent extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (c.getChannelServer().getEvent() == c.getPlayer().getMapId()) {
                MapleEvent.setEvent(c.getChannelServer(), false);
                c.getPlayer().dropMessage(5, "Started the event and closed off");
                return true;
            } else {
                c.getPlayer().dropMessage(5, "!event must've been done first, and you must be in the event map.");
                return false;
            }
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!StartEvent - StartEvent").toString();
        }
    }

    public static class Event extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            final MapleEventType type = MapleEventType.getByString(splitted[1]);
            if (type == null) {
                final StringBuilder sb = new StringBuilder("Wrong syntax: ");
                for (MapleEventType t : MapleEventType.values()) {
                    sb.append(t.name()).append(",");
                }
                c.getPlayer().dropMessage(5, sb.toString().substring(0, sb.toString().length() - 1));
                return false;
            }
            final String msg = MapleEvent.scheduleEvent(type, c.getChannelServer());
            if (msg.length() > 0) {
                c.getPlayer().dropMessage(5, msg);
                return false;
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Event - Event").toString();
        }
    }

    public static class RemoveItem extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "Need <name> <itemid>");
                return false;
            }
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chr == null) {
                c.getPlayer().dropMessage(6, "This player does not exist");
                return false;
            }
            chr.removeAll(Integer.parseInt(splitted[2]), false);
            c.getPlayer().dropMessage(6, "All items with the ID " + splitted[2] + " has been removed from the inventory of " + splitted[1] + ".");
            return true;

        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!RemoveItem - RemoveItem").toString();
        }
    }

    public static class LockItem extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "Need <name> <itemid>");
                return false;
            }
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chr == null) {
                c.getPlayer().dropMessage(6, "This player does not exist");
                return false;
            }
            int itemid = Integer.parseInt(splitted[2]);
            MapleInventoryType type = GameConstants.getInventoryType(itemid);
            for (Item item : chr.getInventory(type).listById(itemid)) {
                item.setFlag((byte) (item.getFlag() | ItemFlag.LOCK.getValue()));
                chr.getClient().getSession().write(InventoryPacket.updateSpecialItemUse(item, type.getType(), item.getPosition(), true, chr));
            }
            if (type == MapleInventoryType.EQUIP) {
                type = MapleInventoryType.EQUIPPED;
                for (Item item : chr.getInventory(type).listById(itemid)) {
                    item.setFlag((byte) (item.getFlag() | ItemFlag.LOCK.getValue()));
                    //chr.getClient().getSession().write(CField.updateSpecialItemUse(item, type.getType()));
                }
            }
            c.getPlayer().dropMessage(6, "All items with the ID " + splitted[2] + " has been locked from the inventory of " + splitted[1] + ".");
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!LockItem - LockItem").toString();
        }
    }

    public static class Smega extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(3, c.getPlayer() == null ? c.getChannel() : c.getPlayer().getClient().getChannel(), c.getPlayer() == null ? c.getPlayer().getName() : c.getPlayer().getName() + " : " + StringUtil.joinStringFrom(splitted, 1), true));
            /*if (splitted.length < 2) {
             c.getPlayer().dropMessage(0, "!smega <itemid> <message>");
             return false;
             }
             final List<String> lines = new LinkedList<>();
             for (int i = 0; i < 4; i++) {
             final String text = StringUtil.joinStringFrom(splitted, 2);
             if (text.length() > 55) {
             continue;
             }
             lines.add(text);
             }
             final boolean ear = true;
             World.Broadcast.broadcastSmega(CWvsContext.getAvatarMega(c.getPlayer(), c.getChannel(), Integer.parseInt(splitted[1]), lines, ear)); */
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Smega - Smega").toString();
        }
    }

    public static class SpeakMega extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null) {
                c.getPlayer().dropMessage(0, "The person isn't login, or doesn't exists.");
                return false;
            }
            World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(3, victim.getClient().getChannel(), victim.getName() + " : " + StringUtil.joinStringFrom(splitted, 2), true));
            /* 
             if (splitted.length < 2) {
             c.getPlayer().dropMessage(0, "!smega <itemid> <victim> <message>");
             return false;
             }
             final List<String> lines = new LinkedList<>();
             for (int i = 0; i < 4; i++) {
             final String text = StringUtil.joinStringFrom(splitted, 3);
             if (text.length() > 55) {
             continue;
             }
             lines.add(text);
             }
             final boolean ear = true;
             World.Broadcast.broadcastSmega(CWvsContext.getAvatarMega(victim, victim.getClient().getChannel(), Integer.parseInt(splitted[1]), lines, ear));
             */
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!SpeakMega - SpeakMega").toString();
        }
    }

    public static class SpeakAll extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (mch == null) {
                    return false;
                } else {
                    mch.getMap().broadcastMessage(CField.getChatText(mch.getId(), StringUtil.joinStringFrom(splitted, 1), mch.isGM(), 0));
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!SpeakAll - SpeakAll").toString();
        }
    }

    public static class Speak extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null) {
                c.getPlayer().dropMessage(5, "unable to find '" + splitted[1]);
                return false;
            } else {
                victim.getMap().broadcastMessage(CField.getChatText(victim.getId(), StringUtil.joinStringFrom(splitted, 2), victim.isGM(), 0));
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Speak - Speak").toString();
        }
    }

    public static class DiseaseMap extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "!disease <type> <level> where type = SEAL/DARKNESS/WEAKEN/STUN/CURSE/POISON/SLOW/SEDUCE/REVERSE/ZOMBIFY/POTION/SHADOW/BLIND/FREEZE/POTENTIAL");
                return false;
            }
            int type;
            if (splitted[1].equalsIgnoreCase("SEAL")) {
                type = 120;
            } else if (splitted[1].equalsIgnoreCase("DARKNESS")) {
                type = 121;
            } else if (splitted[1].equalsIgnoreCase("WEAKEN")) {
                type = 122;
            } else if (splitted[1].equalsIgnoreCase("STUN")) {
                type = 123;
            } else if (splitted[1].equalsIgnoreCase("CURSE")) {
                type = 124;
            } else if (splitted[1].equalsIgnoreCase("POISON")) {
                type = 125;
            } else if (splitted[1].equalsIgnoreCase("SLOW")) {
                type = 126;
            } else if (splitted[1].equalsIgnoreCase("SEDUCE")) { //24, 289 and 29 are cool.
                type = 128;
            } else if (splitted[1].equalsIgnoreCase("REVERSE")) {
                type = 132;
            } else if (splitted[1].equalsIgnoreCase("ZOMBIFY")) {
                type = 133;
            } else if (splitted[1].equalsIgnoreCase("POTION")) {
                type = 134;
            } else if (splitted[1].equalsIgnoreCase("SHADOW")) {
                type = 135;
            } else if (splitted[1].equalsIgnoreCase("BLIND")) {
                type = 136;
            } else if (splitted[1].equalsIgnoreCase("FREEZE")) {
                type = 137;
            } else if (splitted[1].equalsIgnoreCase("POTENTIAL")) {
                type = 138;
            } else if (splitted[1].equalsIgnoreCase("SLOW2")) {
                type = 172;
            } else if (splitted[1].equalsIgnoreCase("TORNADO")) {
                type = 173;
            } else if (splitted[1].equalsIgnoreCase("FLAG")) {
                type = 799;
            } else {
                c.getPlayer().dropMessage(6, "!disease <type> <level> where type = SEAL/DARKNESS/WEAKEN/STUN/CURSE/POISON/SLOW/SEDUCE/REVERSE/ZOMBIFY/POTION/SHADOW/BLIND/FREEZE/POTENTIAL/SLOW2/TORNADO/FLAG");
                return false;
            }
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() == c.getPlayer().getMapId()) {
                    if (splitted.length == 4) {
                        if (mch == null) {
                            c.getPlayer().dropMessage(5, "Not found.");
                            return false;
                        }
                        mch.disease(type, CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1));
                    } else {
                        mch.disease(type, CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1));
                    }
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!DiseaseMap - DiseaseMap").toString();
        }
    }

    public static class Disease extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "!disease <type> [charname] <level> where type = SEAL/DARKNESS/WEAKEN/STUN/CURSE/POISON/SLOW/SEDUCE/REVERSE/ZOMBIFY/POTION/SHADOW/BLIND/FREEZE/POTENTIAL");
                return false;
            }
            int type;
            if (splitted[1].equalsIgnoreCase("SEAL")) {
                type = 120;
            } else if (splitted[1].equalsIgnoreCase("DARKNESS")) {
                type = 121;
            } else if (splitted[1].equalsIgnoreCase("WEAKEN")) {
                type = 122;
            } else if (splitted[1].equalsIgnoreCase("STUN")) {
                type = 123;
            } else if (splitted[1].equalsIgnoreCase("CURSE")) {
                type = 124;
            } else if (splitted[1].equalsIgnoreCase("POISON")) {
                type = 125;
            } else if (splitted[1].equalsIgnoreCase("SLOW")) {
                type = 126;
            } else if (splitted[1].equalsIgnoreCase("SEDUCE")) {
                type = 128;
            } else if (splitted[1].equalsIgnoreCase("REVERSE")) {
                type = 132;
            } else if (splitted[1].equalsIgnoreCase("ZOMBIFY")) {
                type = 133;
            } else if (splitted[1].equalsIgnoreCase("POTION")) {
                type = 134;
            } else if (splitted[1].equalsIgnoreCase("SHADOW")) {
                type = 135;
            } else if (splitted[1].equalsIgnoreCase("BLIND")) {
                type = 136;
            } else if (splitted[1].equalsIgnoreCase("FREEZE")) {
                type = 137;
            } else if (splitted[1].equalsIgnoreCase("POTENTIAL")) {
                type = 138;
            } else if (splitted[1].equalsIgnoreCase("SLOW2")) {
                type = 172;
            } else if (splitted[1].equalsIgnoreCase("TORNADO")) {
                type = 173;
            } else if (splitted[1].equalsIgnoreCase("FLAG")) {
                type = 799;
            } else {
                c.getPlayer().dropMessage(6, "!disease <type> [charname] <level> where type = SEAL/DARKNESS/WEAKEN/STUN/CURSE/POISON/SLOW/SEDUCE/REVERSE/ZOMBIFY/POTION/SHADOW/BLIND/FREEZE/POTENTIAL/SLOW2/TORNADO/FLAG");
                return false;
            }
            if (splitted.length == 4) {
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[2]);
                if (victim == null) {
                    c.getPlayer().dropMessage(5, "Not found.");
                    return false;
                }
                victim.disease(type, CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1));
            } else {
                for (MapleCharacter victim : c.getPlayer().getMap().getCharactersThreadsafe()) {
                    victim.disease(type, CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1));
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Disease - Disease").toString();
        }
    }

    public static class CloneMe extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().cloneLook();
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!CloneMe - 克隆自己").toString();
        }
    }

    public static class DisposeClones extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().dropMessage(6, c.getPlayer().getCloneSize() + " clones disposed.");
            c.getPlayer().disposeClones();
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!DisposeClones - 移除克隆体").toString();
        }
    }

    public static class SetInstanceProperty extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
            if (em == null || em.getInstances().size() <= 0) {
                c.getPlayer().dropMessage(5, "none");
            } else {
                em.setProperty(splitted[2], splitted[3]);
                for (EventInstanceManager eim : em.getInstances()) {
                    eim.setProperty(splitted[2], splitted[3]);
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!SetInstanceProperty - SetInstanceProperty").toString();
        }
    }

    public static class ListInstanceProperty extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
            if (em == null || em.getInstances().size() <= 0) {
                c.getPlayer().dropMessage(5, "none");
            } else {
                for (EventInstanceManager eim : em.getInstances()) {
                    c.getPlayer().dropMessage(5, "Event " + eim.getName() + ", eventManager: " + em.getName() + " iprops: " + eim.getProperty(splitted[2]) + ", eprops: " + em.getProperty(splitted[2]));
                }
            }
            return false;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!ListInstanceProperty - ListInstanceProperty").toString();
        }
    }

    public static class LeaveInstance extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (c.getPlayer().getEventInstance() == null) {
                c.getPlayer().dropMessage(5, "You are not in one");
            } else {
                c.getPlayer().getEventInstance().unregisterPlayer(c.getPlayer());
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!LeaveInstance - LeaveInstance").toString();
        }
    }

    public static class WhosThere extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            StringBuilder builder = new StringBuilder("Players on Map: ").append(c.getPlayer().getMap().getCharactersThreadsafe().size()).append(", ");
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                if (builder.length() > 150) { // wild guess :o
                    builder.setLength(builder.length() - 2);
                    c.getPlayer().dropMessage(6, builder.toString());
                    builder = new StringBuilder();
                }
                builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                builder.append(", ");
            }
            builder.setLength(builder.length() - 2);
            c.getPlayer().dropMessage(6, builder.toString());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!WhosThere - WhosThere").toString();
        }
    }

    public static class StartInstance extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (c.getPlayer().getEventInstance() != null) {
                c.getPlayer().dropMessage(5, "You are in one");
            } else if (splitted.length > 2) {
                EventManager em = c.getChannelServer().getEventSM().getEventManager(splitted[1]);
                if (em == null || em.getInstance(splitted[2]) == null) {
                    c.getPlayer().dropMessage(5, "Not exist");
                } else {
                    em.getInstance(splitted[2]).registerPlayer(c.getPlayer());
                }
            } else {
                c.getPlayer().dropMessage(5, "!startinstance [eventmanager] [eventinstance]");
            }
            return true;

        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!StartInstance - StartInstance").toString();
        }
    }

    public static class 刷新地图 extends CommandExecute {

        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter player = c.getPlayer();
            boolean custMap = splitted.length >= 2;
            int mapid = custMap ? Integer.parseInt(splitted[1]) : player.getMapId();
            MapleMap map = custMap ? player.getClient().getChannelServer().getMapFactory().getMap(mapid) : player.getMap();
            if (player.getClient().getChannelServer().getMapFactory().destroyMap(mapid)) {
                MapleMap newMap = player.getClient().getChannelServer().getMapFactory().getMap(mapid);
                MaplePortal newPor = newMap.getPortal(0);
                LinkedHashSet<MapleCharacter> mcs = new LinkedHashSet<>(map.getCharacters()); // do NOT remove, fixing ConcurrentModificationEx.
                outerLoop:
                for (MapleCharacter m : mcs) {
                    for (int x = 0; x < 5; x++) {
                        try {
                            m.changeMap(newMap, newPor);
                            continue outerLoop;
                        } catch (Throwable t) {
                        }
                    }
                    c.getPlayer().dropMessage(5,"传送玩家 " + m.getName() + " 到新地图失败. 自动省略...");
                }
                c.getPlayer().dropMessage(5,"地图刷新完成.");
                return true;
            }
            c.getPlayer().dropMessage(5,"刷新地图失败!");
            return true;
        }

        public String getMessage() {
            return new StringBuilder().append("!刷新地图 <地圖id> - 刷新某个地图").toString();
        }
    }
    
    public static class 取坐标 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().dropMessage(5,"当前坐标 X:" + c.getPlayer().getPosition().x + " - Y:" + c.getPlayer().getPosition().y + " (地图ID：" + c.getPlayer().getMapId() + ")");
            
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!取坐标").toString();
        }
    }
    
    public static class 重置怪物 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().getMap().killAllMonsters(false);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!重置怪物").toString();
        }
    }
    
    public static class KillMonsterByOID extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleMap map = c.getPlayer().getMap();
            int targetId = Integer.parseInt(splitted[1]);
            MapleMonster monster = map.getMonsterByOid(targetId);
            if (monster != null) {
                map.killMonster(monster, c.getPlayer(), false, false, (byte) 1);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!KillMonsterByOID - KillMonsterByOID").toString();
        }
    }

    public static class RemoveNPCs extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().getMap().resetNPCs();
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!RemoveNPCs - RemoveNPCs").toString();
        }
    }

    public static class GMChatNotice extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MapleCharacter all : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                all.dropMessage(-6, StringUtil.joinStringFrom(splitted, 1));
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!GMChatNotice - GMChatNotice").toString();
        }
    }

    public static class Notice extends CommandExecute {

        protected static int getNoticeType(String typestring) {
            switch (typestring) {
                case "1":
                    return -1;
                case "2":
                    return -2;
                case "3":
                    return -3;
                case "4":
                    return -4;
                case "5":
                    return -5;
                case "6":
                    return -6;
                case "7":
                    return -7;
                case "8":
                    return -8;
                case "n":
                    return 0;
                case "p":
                    return 1;
                case "l":
                    return 2;
                case "nv":
                    return 5;
                case "v":
                    return 5;
                case "b":
                    return 6;
            }
            return -1;
        }

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            int joinmod = 1;
            int range = -1;
            switch (splitted[1]) {
                case "m":
                    range = 0;
                    break;
                case "c":
                    range = 1;
                    break;
                case "w":
                    range = 2;
                    break;
            }
            int tfrom = 2;
            if (range == -1) {
                range = 2;
                tfrom = 1;
            }
            int type = getNoticeType(splitted[tfrom]);
            if (type == -1) {
                type = 0;
                joinmod = 0;
            }
            StringBuilder sb = new StringBuilder();
            if (splitted[tfrom].equals("nv")) {
                sb.append("[Notice]");
            } else {
                sb.append("");
            }
            joinmod += tfrom;
            sb.append(StringUtil.joinStringFrom(splitted, joinmod));

            byte[] packet = CWvsContext.broadcastMsg(type, sb.toString());
            if (range == 0) {
                c.getPlayer().getMap().broadcastMessage(packet);
            } else if (range == 1) {
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else if (range == 2) {
                World.Broadcast.broadcastMessage(packet);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Notice - Notice").toString();
        }
    }

    public static class Yellow extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            int range = -1;
            switch (splitted[1]) {
                case "m":
                    range = 0;
                    break;
                case "c":
                    range = 1;
                    break;
                case "w":
                    range = 2;
                    break;
            }
            if (range == -1) {
                range = 2;
            }
            byte[] packet = CWvsContext.yellowChat((splitted[0].equals("!y") ? ("[" + c.getPlayer().getName() + "] ") : "") + StringUtil.joinStringFrom(splitted, 2));
            if (range == 0) {
                c.getPlayer().getMap().broadcastMessage(packet);
            } else if (range == 1) {
                ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
            } else if (range == 2) {
                World.Broadcast.broadcastMessage(packet);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Yellow - Yellow").toString();
        }
    }

    public static class Y extends Yellow {
    }

    public static class WhatsMyIP extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().dropMessage(5, "IP: " + c.getSession().getRemoteAddress().toString().split(":")[0]);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!WhatsMyIP - WhatsMyIP").toString();
        }
    }

    public static class TempBanIP extends TempBan {

        public TempBanIP() {
            ipBan = true;
        }
    }

    public static class BanIP extends Ban {

        public BanIP() {
            ipBan = true;
        }
    }

    public static class TDrops extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().getMap().toggleDrops();
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!TDrops - TDrops").toString();

        }
    }

    public static class Jail extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "jail [name] [minutes, 0 = forever]");
                return false;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            final int minutes = Math.max(0, Integer.parseInt(splitted[2]));
            if (victim != null && c.getPlayer().getGMLevel() >= victim.getGMLevel()) {
                MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(GameConstants.JAIL);
                victim.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST)).setCustomData(String.valueOf(minutes * 60));
                victim.changeMap(target, target.getPortal(0));
            } else {
                c.getPlayer().dropMessage(6, "Please be on their channel.");
                return false;
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Jail - Jail").toString();

        }
    }

    public static class LookNPC extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllNPCsThreadsafe()) {
                MapleNPC reactor2l = (MapleNPC) reactor1l;
                c.getPlayer().dropMessage(5, "NPC: oID: " + reactor2l.getObjectId() + " npcID: " + reactor2l.getId() + " Position: " + reactor2l.getPosition().toString() + " Name: " + reactor2l.getName());
            }
            return false;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!LookNPC - LookNPC").toString();

        }
    }

    public static class LookReactor extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllReactorsThreadsafe()) {
                MapleReactor reactor2l = (MapleReactor) reactor1l;
                c.getPlayer().dropMessage(5, "Reactor: oID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getReactorId() + " Position: " + reactor2l.getPosition().toString() + " State: " + reactor2l.getState() + " Name: " + reactor2l.getName());
            }
            return false;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!LookReactor - LookReactor").toString();

        }
    }

    public static class LookPortals extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MaplePortal portal : c.getPlayer().getMap().getPortals()) {
                c.getPlayer().dropMessage(5, "Portal: ID: " + portal.getId() + " script: " + portal.getScriptName() + " name: " + portal.getName() + " pos: " + portal.getPosition().x + "," + portal.getPosition().y + " target: " + portal.getTargetMapId() + " / " + portal.getTarget());
            }
            return false;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!LookPortals - LookPortals").toString();

        }
    }

    public static class MyNPCPos extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            Point pos = c.getPlayer().getPosition();
            c.getPlayer().dropMessage(6, "X: " + pos.x + " | Y: " + pos.y + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getFH());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!MyNPCPos - MyNPCPos").toString();

        }
    }

    public static class Letter extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "syntax: !letter <color (green/red)> <word>");
                return false;
            }
            int start;
            int nstart;
            if (splitted[1].equalsIgnoreCase("green")) {
                start = 3991026;
                nstart = 3990019;
            } else if (splitted[1].equalsIgnoreCase("red")) {
                start = 3991000;
                nstart = 3990009;
            } else {
                c.getPlayer().dropMessage(6, "Unknown color!");
                return false;
            }
            String splitString = StringUtil.joinStringFrom(splitted, 2);
            List<Integer> chars = new ArrayList();
            splitString = splitString.toUpperCase();

            for (int i = 0; i < splitString.length(); ++i) {
                char chr = splitString.charAt(i);
                if (chr == ' ') {
                    chars.add(Integer.valueOf(-1));
                } else if ((chr >= 'A') && (chr <= 'Z')) {
                    chars.add(Integer.valueOf(chr));
                } else if ((chr >= '0') && (chr <= '9')) {
                    chars.add(Integer.valueOf(chr + 200));
                }
            }
            int w = 32;
            int dStart = c.getPlayer().getPosition().x - (splitString.length() / 2 * 32);
            for (Integer i : chars) {
                if (i.intValue() == -1) {
                    dStart += 32;
                } else {
                    int val;
                    Item item;
                    if (i.intValue() < 200) {
                        val = start + i.intValue() - 65;
                        item = new Item(val, (byte) 0, (short) 1);
                        c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, new Point(dStart, c.getPlayer().getPosition().y), false, false);
                        dStart += 32;
                    } else if ((i.intValue() >= 200) && (i.intValue() <= 300)) {
                        val = nstart + i.intValue() - 48 - 200;
                        item = new Item(val, (byte) 0, (short) 1);
                        c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, new Point(dStart, c.getPlayer().getPosition().y), false, false);
                        dStart += 32;
                    }
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Letter - Letter").toString();

        }
    }

    public static class Spawn extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            final int mid = Integer.parseInt(splitted[1]);
            final int num = Math.min(CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1), 500);
            Integer level = CommandProcessorUtil.getNamedIntArg(splitted, 1, "lvl");
            Long hp = CommandProcessorUtil.getNamedLongArg(splitted, 1, "hp");
            Integer exp = CommandProcessorUtil.getNamedIntArg(splitted, 1, "exp");
            Double php = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "php");
            Double pexp = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "pexp");

            MapleMonster onemob;
            try {
                onemob = MapleLifeFactory.getMonster(mid);
            } catch (RuntimeException e) {
                c.getPlayer().dropMessage(5, "Error: " + e.getMessage());
                return false;
            }
            if (onemob == null) {
                c.getPlayer().dropMessage(5, "Mob does not exist");
                return false;
            }
            long newhp;
            int newexp;
            if (hp != null) {
                newhp = hp;
            } else if (php != null) {
                newhp = (int) (onemob.getMobMaxHp() * (php.doubleValue() / 100));
            } else {
                newhp = onemob.getMobMaxHp();
            }
            if (exp != null) {
                newexp = exp.intValue();
            } else if (pexp != null) {
                newexp = (int) (onemob.getMobExp() * (pexp.doubleValue() / 100));
            } else {
                newexp = onemob.getMobExp();
            }
            if (newhp < 1) {
                newhp = 1;
            }

            final OverrideMonsterStats overrideStats = new OverrideMonsterStats(newhp, onemob.getMobMaxMp(), newexp, false);
            for (int i = 0; i < num; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mid);
                mob.setHp(newhp);
                if (level != null) {
                    mob.changeLevel(level.intValue(), false);
                } else {
                    mob.setOverrideStats(overrideStats);
                }
                c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Spawn - Spawn").toString();

        }
    }

    public static class SpawnMob extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            final String mname = splitted[2];
            final int num = Integer.parseInt(splitted[1]);
            int mid = 0;
            for (Pair<Integer, String> mob : MapleMonsterInformationProvider.getInstance().getAllMonsters()) {
                if (mob.getRight().toLowerCase().equals(mname.toLowerCase())) {
                    mid = mob.getLeft();
                    break;
                }
            }

            MapleMonster onemob;
            try {
                onemob = MapleLifeFactory.getMonster(mid);
            } catch (RuntimeException e) {
                c.getPlayer().dropMessage(5, "Error: " + e.getMessage());
                return false;
            }
            if (onemob == null) {
                c.getPlayer().dropMessage(5, "Mob does not exist");
                return false;
            }
            for (int i = 0; i < num; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mid);
                c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!SpawnMob - SpawnMob").toString();

        }
    }

    public static class Mute extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.canTalk(false);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Mute - Mute").toString();

        }
    }

    public static class UnMute extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.canTalk(true);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!UnMute - UnMute").toString();

        }
    }

    public static class MuteMap extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                chr.canTalk(false);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!MuteMap - MuteMap").toString();

        }
    }

    public static class UnMuteMap extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                chr.canTalk(true);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!UnMuteMap - UnMuteMap").toString();

        }
    }

    public static class 文件封包 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getSession().write(LoadPacket.getPacket());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!文件封包 - 文件封包").toString();
        }
    }

    public static class 高级检索 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String[] splitted) {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(CWvsContext.enableActions());
            NPCScriptManager.getInstance().start(c, 9010000, "高级检索");
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!高级检索 - 各种功能检索功能").toString();
        }
    }

    private GMCommand() {
    }
}
