package server.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import constants.ServerConstants.PlayerGMRank;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import java.awt.Point;
import java.util.Arrays;
import scripting.NPCScriptManager;
import server.MapleItemInformationProvider;
import server.Timer;
import server.commands.CommandExecute.TradeExecute;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.SavedLocationType;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.packet.CField.NPCPacket;
import tools.packet.CWvsContext;

/**
 *
 * @author Emilyx3
 */
public class PlayerCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.NORMAL;
    }

    public static class STR extends DistributeStatCommands {

        public STR() {
            stat = MapleStat.STR;
        }
    }

    public static class DEX extends DistributeStatCommands {

        public DEX() {
            stat = MapleStat.DEX;
        }
    }

    public static class INT extends DistributeStatCommands {

        public INT() {
            stat = MapleStat.INT;
        }
    }

    public static class LUK extends DistributeStatCommands {

        public LUK() {
            stat = MapleStat.LUK;
        }
    }

    public static class HP extends DistributeStatCommands {

        public HP() {
            stat = MapleStat.MAXHP;
        }
    }

    public static class MP extends DistributeStatCommands {

        public MP() {
            stat = MapleStat.MAXMP;
        }
    }

    public abstract static class DistributeStatCommands extends CommandExecute {

        protected MapleStat stat = null;
        private static final int statLim = 999;
        private static final int hpMpLim = 30000;

        private void setStat(MapleCharacter player, int current, int amount) {
            switch (stat) {
                case STR:
                    player.getStat().setStr((short) (current + amount), player);
                    player.updateSingleStat(MapleStat.STR, player.getStat().getStr());
                    break;
                case DEX:
                    player.getStat().setDex((short) (current + amount), player);
                    player.updateSingleStat(MapleStat.DEX, player.getStat().getDex());
                    break;
                case INT:
                    player.getStat().setInt((short) (current + amount), player);
                    player.updateSingleStat(MapleStat.INT, player.getStat().getInt());
                    break;
                case LUK:
                    player.getStat().setLuk((short) (current + amount), player);
                    player.updateSingleStat(MapleStat.LUK, player.getStat().getLuk());
                    break;
                case MAXHP:
                    long maxhp = Math.min(30000, Math.abs(current + amount));
                    player.getStat().setMaxHp((short) maxhp, player);
                    player.updateSingleStat(MapleStat.HP, player.getStat().getHp());
                    break;
                case MAXMP:
                    long maxmp = Math.min(30000, Math.abs(current + amount));
                    player.getStat().setMaxMp((short) maxmp, player);
                    player.updateSingleStat(MapleStat.MP, player.getStat().getMp());
                    break;
            }
        }

        private int getStat(MapleCharacter player) {
            switch (stat) {
                case STR:
                    return player.getStat().getStr();
                case DEX:
                    return player.getStat().getDex();
                case INT:
                    return player.getStat().getInt();
                case LUK:
                    return player.getStat().getLuk();
                case MAXHP:
                    return player.getStat().getMaxHp();
                case MAXMP:
                    return player.getStat().getMaxMp();
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "Invalid number entered.");
                return false;
            }
            int change;
            try {
                change = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(5, "Invalid number entered.");
                return false;
            }
            int hpUsed = 0;
            int mpUsed = 0;
            if (stat == MapleStat.MAXHP) {
                hpUsed = change;
                short job = c.getPlayer().getJob();
                change *= GameConstants.getHpApByJob(job);
            }
            if (stat == MapleStat.MAXMP) {
                mpUsed = change;
                short job = c.getPlayer().getJob();
                if (GameConstants.isDemonSlayer(job) || GameConstants.isAngelicBuster(job) || GameConstants.isDemonAvenger(job)) {
                    c.getPlayer().dropMessage(5, "You cannot raise MP.");
                    return false;
                }
                change *= GameConstants.getMpApByJob(job);
            }
            if (change <= 0) {
                c.getPlayer().dropMessage(5, "You don't have enough AP Resets that.");
                return false;
            }
            if (c.getPlayer().getRemainingAp() < change) {
                c.getPlayer().dropMessage(5, "You don't have enough AP for that.");
                return false;
            }
            if (getStat(c.getPlayer()) + change > statLim && stat != MapleStat.MAXHP && stat != MapleStat.MAXMP) {
                c.getPlayer().dropMessage(5, "The stat limit is " + statLim + ".");
                return false;
            }
            if (getStat(c.getPlayer()) + change > hpMpLim && (stat == MapleStat.MAXHP || stat == MapleStat.MAXMP)) {
                c.getPlayer().dropMessage(5, "The stat limit is " + hpMpLim + ".");
                return false;
            }
            setStat(c.getPlayer(), getStat(c.getPlayer()), change);
            c.getPlayer().setRemainingAp((short) (c.getPlayer().getRemainingAp() - change));
            c.getPlayer().setHpApUsed((short) (c.getPlayer().getHpApUsed() + hpUsed));
            c.getPlayer().setHpApUsed((short) (c.getPlayer().getHpApUsed() + mpUsed));
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp());
            c.getPlayer().dropMessage(5, StringUtil.makeEnumHumanReadable(stat.name()) + " has been raised by " + change + ".");
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("@DistributeStatCommands - DistributeStatCommands").toString();

        }
    }
   public static class 解卡组队
     extends CommandExecute
   {
     public boolean execute(MapleClient c, String[] splitted)
     {
       c.getPlayer().setParty(null);
       c.getPlayer().dropMessage(1, "解卡组队成功，赶紧去组队把。");
       return true;
     }
     
     public String getMessage() { return "@解卡组队-游戏无法进行组队的时候输入"; }
   }
   
    public static class Mob extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleMonster mob = null;
            for (final MapleMapObject monstermo : c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 100000, Arrays.asList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (mob.isAlive()) {
                    c.getPlayer().dropMessage(6, "Monster " + mob.toString());
                    break; //only one
                }
            }
            if (mob == null) {
                c.getPlayer().dropMessage(6, "No monster was found.");
            }
            return true;
        }

        public String getMessage() {
            return new StringBuilder().append("@Mob - Mob").toString();

        }
    }

    /*public abstract static class OpenNPCCommand extends CommandExecute {

     protected int npc = -1;
     private static int[] npcs = { //Ish yur job to make sure these are in order and correct ;(
     9270035,
     9900000,
     9000000,
     9000030,
     9010000,
     9000085,
     9000018,
     9900000, // yes, i know, again but for other reason.
     9010000 // again too, but used for opening a scriptname
     };

     @Override
     public boolean execute(MapleClient c, String splitted[]) {
     if (npc != 6 && npc != 5 && npc != 4 && npc != 3 && npc != 1 && c.getPlayer().getMapId() != 910000000) { //drpcash can use anywhere
     if (c.getPlayer().getLevel() < 10 && c.getPlayer().getJob() != 200) {
     c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
     return false;
     }
     if (c.getPlayer().isInBlockedMap()) {
     c.getPlayer().dropMessage(5, "You may not use this command here.");
     return false;
     }
     } else if (npc == 1) {
     if (c.getPlayer().getLevel() < 70) {
     c.getPlayer().dropMessage(5, "You must be over level 70 to use this command.");
     return false;
     }
     }
     if (c.getPlayer().hasBlockedInventory()) {
     c.getPlayer().dropMessage(5, "You may not use this command here.");
     return false;
     }
     NPCScriptManager.getInstance().start(c, npcs[npc], npc == 1 ? "CashDrop" : npc == 8 ? "BossWarp" : null);
     return true;
     }
     }

     public static class Style extends Stylist {
     }

     public static class Stylist extends OpenNPCCommand {

     public Stylist() {
     npc = 7;
     }
     }

     public static class Npc extends Maple {
     }

     public static class Maple extends OpenNPCCommand {

     public Crescent() {
     npc = 0;
     }
     }

     public static class BossWarp extends OpenNPCCommand {

     public BossWarp() {
     npc = 8;
     }
     }

     public static class Advance extends CommandExecute {

     @Override
     public boolean execute(MapleClient c, String splitted[]) {
     if (c.getPlayer().isInBlockedMap()) {
     c.getPlayer().dropMessage(5, "You may not use this command here.");
     return false;
     }
     if (c.getPlayer().getLevel() < 10) {
     c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
     return false;
     }
     NPCScriptManager.getInstance().start(c, 9900002, null);
     return true;
     }
     }

     public static class Save extends CommandExecute {

     @Override
     public boolean execute(MapleClient c, String splitted[]) {
     c.getPlayer().saveToDB(false, false);
     return true;
     }
     }

     public static class DCash extends OpenNPCCommand {

     public DCash() {
     npc = 1;
     }
     }
     */
    public static class Event extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (c.getPlayer().isInBlockedMap() || c.getPlayer().hasBlockedInventory()) {
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return false;
            }
            NPCScriptManager.getInstance().start(c, 9000000, null);
            return true;
        }

        public String getMessage() {
            return new StringBuilder().append("@Event - Event").toString();

        }
    }

    /*
     public static class CheckDrop extends OpenNPCCommand {

     public CheckDrop() {
     npc = 4;
     }
     }

     public static class Pokedex extends OpenNPCCommand {

     public Pokedex() {
     npc = 5;
     }
     }

     public static class ClearSlot extends CommandExecute {

     private static MapleInventoryType[] invs = {MapleInventoryType.EQUIP, MapleInventoryType.USE, MapleInventoryType.SETUP, MapleInventoryType.ETC, MapleInventoryType.CASH,};

     @Override
     public boolean execute(MapleClient c, String splitted[]) {
     MapleCharacter player = c.getPlayer();
     if (splitted.length < 2 || player.hasBlockedInventory()) {
     c.getPlayer().dropMessage(5, "@clearslot <eq / use / setup / etc / cash / all >");
     return false;
     } else {
     MapleInventoryType type;
     if (splitted[1].equalsIgnoreCase("eq")) {
     type = MapleInventoryType.EQUIP;
     } else if (splitted[1].equalsIgnoreCase("use")) {
     type = MapleInventoryType.USE;
     } else if (splitted[1].equalsIgnoreCase("setup")) {
     type = MapleInventoryType.SETUP;
     } else if (splitted[1].equalsIgnoreCase("etc")) {
     type = MapleInventoryType.ETC;
     } else if (splitted[1].equalsIgnoreCase("cash")) {
     type = MapleInventoryType.CASH;
     } else if (splitted[1].equalsIgnoreCase("all")) {
     type = null;
     } else {
     c.getPlayer().dropMessage(5, "Invalid. @clearslot <eq / use / setup / etc / cash / all >");
     return false;
     }
     if (type == null) { //All, a bit hacky, but it's okay 
     for (MapleInventoryType t : invs) {
     type = t;
     MapleInventory inv = c.getPlayer().getInventory(type);
     byte start = -1;
     for (byte i = 0; i < inv.getSlotLimit(); i++) {
     if (inv.getItem(i) != null) {
     start = i;
     break;
     }
     }
     if (start == -1) {
     c.getPlayer().dropMessage(5, "There are no items in that inventory.");
     return false;
     }
     int end = 0;
     for (byte i = start; i < inv.getSlotLimit(); i++) {
     if (inv.getItem(i) != null) {
     MapleInventoryManipulator.removeFromSlot(c, type, i, inv.getItem(i).getQuantity(), true);
     } else {
     end = i;
     break;//Break at first empty space. 
     }
     }
     c.getPlayer().dropMessage(5, "Cleared slots " + start + " to " + end + ".");
     }
     } else {
     MapleInventory inv = c.getPlayer().getInventory(type);
     byte start = -1;
     for (byte i = 0; i < inv.getSlotLimit(); i++) {
     if (inv.getItem(i) != null) {
     start = i;
     break;
     }
     }
     if (start == -1) {
     c.getPlayer().dropMessage(5, "There are no items in that inventory.");
     return false;
     }
     byte end = 0;
     for (byte i = start; i < inv.getSlotLimit(); i++) {
     if (inv.getItem(i) != null) {
     MapleInventoryManipulator.removeFromSlot(c, type, i, inv.getItem(i).getQuantity(), true);
     } else {
     end = i;
     break;//Break at first empty space. 
     }
     }
     c.getPlayer().dropMessage(5, "Cleared slots " + start + " to " + end + ".");
     }
     return true;
     }
     }
     }
     * */
 /*public static class FM extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (int i : GameConstants.blockedMaps) {
                if (c.getPlayer().getMapId() == i) {
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                    return false;
                }
            }
            if (c.getPlayer().getLevel() < 10 && c.getPlayer().getJob() != 200) {
                c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
                return false;
            }
            if (c.getPlayer().hasBlockedInventory() || c.getPlayer().getMap().getSquadByMap() != null || c.getPlayer().getEventInstance() != null || c.getPlayer().getMap().getEMByMap() != null || c.getPlayer().getMapId() >= 990000000) {
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return false;
            }

            if ((c.getPlayer().getMapId() >= 680000210 && c.getPlayer().getMapId() <= 680000502) || (c.getPlayer().getMapId() / 1000 == 980000 && c.getPlayer().getMapId() != 980000000) || (c.getPlayer().getMapId() / 100 == 1030008) || (c.getPlayer().getMapId() / 100 == 922010) || (c.getPlayer().getMapId() / 10 == 13003000)) {
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return false;
            }

            c.getPlayer().saveLocation(SavedLocationType.FREE_MARKET, c.getPlayer().getMap().getReturnMap().getId());
            MapleMap map = c.getChannelServer().getMapFactory().getMap(910000000);

            c.getPlayer()
                    .changeMap(map, map.getPortal(0));

            return true;
        }
    }*/
    
    public static class EA extends CommandExecute {

        public boolean execute(MapleClient c, String splitted[]) {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(CWvsContext.enableActions());
            int 人物经验=1;
            if (c.getPlayer().getLevel() < 10) {
                    人物经验 = ServerConfig.expRate;
                } else if (c.getPlayer().getLevel() < 69) {
                    人物经验 =ServerConfig.expRate69;
                } else if (c.getPlayer().getLevel() < 89) {
                    人物经验 =ServerConfig.expRate89;
                } else if (c.getPlayer().getLevel() < 109) {
                    人物经验 =ServerConfig.expRate109;
                } else if (c.getPlayer().getLevel() < 129) {
                    人物经验 = ServerConfig.expRate129;
                } else if (c.getPlayer().getLevel() < 139) {
                    人物经验 = ServerConfig.expRate139;
                } else if (c.getPlayer().getLevel() < 149) {
                    人物经验 = ServerConfig.expRate149;
                } else if (c.getPlayer().getLevel() < 159) {
                    人物经验 = ServerConfig.expRate159;
                } else if (c.getPlayer().getLevel() < 169) {
                   人物经验 =ServerConfig.expRate169;
                } else if (c.getPlayer().getLevel() < 179) {
                    人物经验 = ServerConfig.expRate179;
                } else if (c.getPlayer().getLevel() < 189) {
                    人物经验 = ServerConfig.expRate189;
                } else if (c.getPlayer().getLevel() < 200) {
                    人物经验 =ServerConfig.expRate200;
                } else if (c.getPlayer().getLevel() < 255) {
                    人物经验 = ServerConfig.expRate250;
                }else {
                    人物经验 =ServerConfig.expRate;
                }
            
            
            c.getPlayer().dropMessage(6, "当前时间是" + FileoutputUtil.CurrentReadable_Time() + "");
            c.getPlayer().dropMessage(6, "当前全服怪物经验 " + (Math.round(人物经验)) + "倍,物品爆率 " + (Math.round(ServerConfig.dropRate)) + "倍, 金币爆率 " + (Math.round(ServerConfig.mesoRate)) + "倍,");
            c.getPlayer().dropMessage(6, "当前个人经验倍率 " + (Math.round(c.getPlayer().getEXPMod()) * 100) * Math.round(c.getPlayer().getStat().expBuff /100) + "%, 爆率 " + (Math.round(c.getPlayer().getDropMod()) * 100) * Math.round(c.getPlayer().getStat().dropBuff / 100.0) + "%, 金币倍率 " + Math.round(c.getPlayer().getStat().mesoBuff / 100.0) * 100 + "%");
            c.getPlayer().dropMessage(6, "当前点劵：" + c.getPlayer().getCSPoints(1) + " 点劵|当前抵用：" + c.getPlayer().getCSPoints(2) + " 抵用");
            c.getPlayer().dropMessage(6, "当前延迟 " + c.getPlayer().getClient().getLatency() + " 毫秒");
            
            
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("@EA - 解卡").toString();
            
        }
    }
    

    /*
     public static class TSmega extends CommandExecute {

     public boolean execute(MapleClient c, String splitted[]) {
     c.getPlayer().setSmega();
     return true;
     }
     }

     public static class Ranking extends CommandExecute {

     public boolean execute(MapleClient c, String splitted[]) {
     if (splitted.length < 4) { //job start end
     c.getPlayer().dropMessage(5, "Use @ranking [job] [start number] [end number] where start and end are ranks of the players");
     final StringBuilder builder = new StringBuilder("JOBS: ");
     for (String b : RankingWorker.getJobCommands().keySet()) {
     builder.append(b);
     builder.append(" ");
     }
     c.getPlayer().dropMessage(5, builder.toString());
     } else {
     int start = 1, end = 20;
     try {
     start = Integer.parseInt(splitted[2]);
     end = Integer.parseInt(splitted[3]);
     } catch (NumberFormatException e) {
     c.getPlayer().dropMessage(5, "You didn't specify start and end number correctly, the default values of 1 and 20 will be used.");
     }
     if (end < start || end - start > 20) {
     c.getPlayer().dropMessage(5, "End number must be greater, and end number must be within a range of 20 from the start number.");
     } else {
     final Integer job = RankingWorker.getJobCommand(splitted[1]);
     if (job == null) {
     c.getPlayer().dropMessage(5, "Please use @ranking to check the job names.");
     } else {
     final List<RankingInformation> ranks = RankingWorker.getRankingInfo(job.intValue());
     if (ranks == null || ranks.size() <= 0) {
     c.getPlayer().dropMessage(5, "Please try again later.");
     } else {
     int num = 0;
     for (RankingInformation rank : ranks) {
     if (rank.rank >= start && rank.rank <= end) {
     if (num == 0) {
     c.getPlayer().dropMessage(6, "Rankings for " + splitted[1] + " - from " + start + " to " + end);
     c.getPlayer().dropMessage(6, "--------------------------------------");
     }
     c.getPlayer().dropMessage(6, rank.toString());
     num++;
     }
     }
     if (num == 0) {
     c.getPlayer().dropMessage(5, "No ranking was returned.");
     }
     }
     }
     }
     }
     return true;
     }
     }*/
    public static class Check extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getCSPoints(1) + " Cash, " + c.getPlayer().getEPoints() + " Event Points, " + c.getPlayer().getDPoints() + " Donation Points, " + c.getPlayer().getVPoints() + " voting points and " + c.getPlayer().getIntNoRecord(GameConstants.BOSS_PQ) + " Boss Party Quest points.");
            c.getPlayer().dropMessage(6, "The time is currently " + FileoutputUtil.CurrentReadable_TimeGMT() + " GMT. | EXP " + (Math.round(c.getPlayer().getEXPMod()) * 100) * Math.round(c.getPlayer().getStat().expBuff / 100.0) + "%, Drop " + (Math.round(c.getPlayer().getDropMod()) * 100) * Math.round(c.getPlayer().getStat().dropBuff / 100.0) + "%, Meso " + Math.round(c.getPlayer().getStat().mesoBuff / 100.0) * 100 + "%");
            c.getPlayer().dropMessage(6, "EXP: " + c.getPlayer().getExp() + " / " + c.getPlayer().getNeededExp());
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(CWvsContext.enableActions());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("@Check - Check").toString();
        }
    }

    /*public static class Help extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            StringBuilder sb = new StringBuilder();
            sb.append("\r\n@str, @dex, @int, @luk, @hp, @mp <amount to add or subtract>");
            sb.append("\r\n@mob < Information on the closest monster >");
            sb.append("\r\n@check < Displays various information; also use if you are stuck or unable to talk to NPC >");
            sb.append("\r\n@npc < Universal NPC >");
            sb.append("\r\n@callgm < Send a message to all online GameMasters.");
           
            if (c.canClickNPC()) {
                NPCPacket.getNPCTalk(9010000, (byte) 0, sb.toString(), "00 00", (byte) 0);
            }
            for (String command : sb.toString().split("\r\n")) {
                c.getPlayer().dropMessage(5, command);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("@Help - 指令列表").toString();
        }
    }*/
    //public static class Crescent extends NPC {
    //}

    /*public static class NPC extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (c.getPlayer().isInBlockedMap() || c.getPlayer().hasBlockedInventory()) {
                c.getPlayer().dropMessage(5, "You may not use this command here.");
                return false;
            }
            NPCScriptManager.getInstance().start(c, 9000021, null);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("@NPC - NPC").toString();
        }
    }*/

 /*public static class TradeHelp extends TradeExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().dropMessage(-2, "[System] : <@offerequip, @offeruse, @offersetup, @offeretc, @offercash> <quantity> <name of the item>");
            return true;
        }
    }*/

 /*public abstract static class OfferCommand extends TradeExecute {

        protected int invType = -1;

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(-2, "[Error] : <quantity> <name of item>");
            } else if (c.getPlayer().getLevel() < 70) {
                c.getPlayer().dropMessage(-2, "[Error] : Only level 70+ may use this command");
            } else {
                int quantity = 1;
                try {
                    quantity = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException e) {
                }
                String search = StringUtil.joinStringFrom(splitted, 2).toLowerCase();
                Item found = null;
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                for (Item inv : c.getPlayer().getInventory(MapleInventoryType.getByType((byte) invType))) {
                    if (ii.getName(inv.getItemId()) != null && ii.getName(inv.getItemId()).toLowerCase().contains(search)) {
                        found = inv;
                        break;
                    }
                }
                if (found == null) {
                    c.getPlayer().dropMessage(-2, "[Error] : No such item was found (" + search + ")");
                    return false;
                }
                if (GameConstants.isPet(found.getItemId()) || GameConstants.isRechargable(found.getItemId())) {
                    c.getPlayer().dropMessage(-2, "[Error] : You may not trade this item using this command");
                    return false;
                }
                if (quantity > found.getQuantity() || quantity <= 0 || quantity > ii.getSlotMax(found.getItemId())) {
                    c.getPlayer().dropMessage(-2, "[Error] : Invalid quantity");
                    return false;
                }
                if (!c.getPlayer().getTrade().setItems(c, found, (byte) -1, quantity)) {
                    c.getPlayer().dropMessage(-2, "[Error] : This item could not be placed");
                    return false;
                } else {
                    c.getPlayer().getTrade().chatAuto("[System] : " + c.getPlayer().getName() + " offered " + ii.getName(found.getItemId()) + " x " + quantity);
                }
            }
            return true;
        }
    }

    public static class OfferEquip extends OfferCommand {

        public OfferEquip() {
            invType = 1;
        }
    }

    public static class OfferUse extends OfferCommand {

        public OfferUse() {
            invType = 2;
        }
    }

    public static class OfferSetup extends OfferCommand {

        public OfferSetup() {
            invType = 3;
        }
    }

    public static class OfferEtc extends OfferCommand {

        public OfferEtc() {
            invType = 4;
        }
    }

    public static class OfferCash extends OfferCommand {

        public OfferCash() {
            invType = 5;
        }
    }*/

 /*public static class JoinRace extends CommandExecute {

     @Override
     public boolean execute(MapleClient c, String splitted[]) {
     if (c.getPlayer().getEntryNumber() < 1) {
     if (c.getPlayer().getMapId() == 100000000) {
     if (c.getChannelServer().getWaiting() || c.getPlayer().isGM()) { //TOD: test
     c.getPlayer().setEntryNumber(c.getChannelServer().getCompetitors() + 1);
     c.getChannelServer().setCompetitors(c.getChannelServer().getCompetitors() + 1);
     SkillFactory.getSkill(c.getPlayer().getGender() == 1 ? 80001006 : 80001005).getEffect(1).applyTo(c.getPlayer());
     c.getPlayer().dropMessage(0, "You have successfully joined the race! Your entry number is " + c.getPlayer().getEntryNumber() + ".");
     c.getPlayer().dropMessage(1, "If you cancel the mount buff, you will automatically leave the race.");
     } else {
     c.getPlayer().dropMessage(0, "There is no event currently taking place.");
     return false;
     }
     } else {
     c.getPlayer().dropMessage(0, "You are not at Henesys.");
     return false;
     }
     } else {
     c.getPlayer().dropMessage(0, "You have already joined this race.");
     return false;
     }
     return true;
     }
     }

     public static class Rules extends CommandExecute {

     @Override
     public boolean execute(MapleClient c, String splitted[]) {
     if (c.getChannelServer().getWaiting() || c.getChannelServer().getRace()) {
     c.getPlayer().dropMessage(0, "The Official Rules and Regulations of the Great Victoria Island Race:");
     c.getPlayer().dropMessage(0, "-------------------------------------------------------------------------------------------");
     c.getPlayer().dropMessage(0, "To win you must race from Henesys all the way to Henesys going Eastward.");
     c.getPlayer().dropMessage(0, "Rule #1: No cheating. You can't use any warping commands, or you'll be disqualified.");
     c.getPlayer().dropMessage(0, "Rule #2: You may use any form of transportation. This includes Teleport, Flash Jump and Mounts.");
     c.getPlayer().dropMessage(0, "Rule #3: You are NOT allowed to kill any monsters in your way. They are obstacles.");
     c.getPlayer().dropMessage(0, "Rule #4: You may start from anywhere in Henesys, but moving on to the next map before the start won't work.");
     } else {
     c.getPlayer().dropMessage(0, "There is no event currently taking place.");
     return false;
     }
     return true;
     }
     }*/
 /*public static class JoinEvent extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getChannelServer().warpToEvent(c.getPlayer());
            return true;
        }
    }

    public static class SpawnBomb extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (c.getPlayer().getMapId() != 109010100) {
                c.getPlayer().dropMessage(5, "You may only spawn bomb in the event map.");
                return false;
            }
            if (!c.getChannelServer().bombermanActive()) {
                c.getPlayer().dropMessage(5, "You may not spawn bombs yet.");
                return false;
            }
            c.getPlayer().getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300166), c.getPlayer().getPosition());
            return true;
        }
    }

    public static class CashDrop extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            NPCScriptManager.getInstance().start(c, 9010000, "CashDrop");
            return true;
        }
    }

    public static class CallGM extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                cserv.broadcastGMMessage(tools.packet.CField.multiChat("[GM Help] " + c.getPlayer().getName(), StringUtil.joinStringFrom(splitted, 1), 6));
            }
            c.getPlayer().dropMessage(5, "Your message had been sent successfully.");
            return true;
        }
    }*/
    public static class 爆率 extends CommandExecute {

        public boolean execute(MapleClient c, String[] splitted) {
            /*if (c.getPlayer().isInBlockedMap()) {
                c.getPlayer().dropMessage(5, "你当前状态无法使用该命令。");
                return false;
            }*/

            if (c.getPlayer().hasBlockedInventory()) {
                c.getPlayer().dropMessage(5, "你当前状态无法使用该命令。");
                return false;
            }
            NPCScriptManager.getInstance().start(c, 9010000, "怪物爆率");
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append(ServerConstants.PlayerGMRank.NORMAL.getCommandPrefix()).append("爆率 - 查看怪物掉落").toString();
        }
    }
    
    
    public static class 测试伤害 extends CommandExecute {

        @Override
        public boolean execute(final MapleClient c, String[] splitted) {
            if ((c.getPlayer().getMapId() / 100 == 7010002 && c.getPlayer().getLevel() >= 70) || c.getPlayer().isGM()) {
                if (!c.getPlayer().isTestingDPM()) {
                    c.getPlayer().toggleTestingDPM();
                    c.getPlayer().dropMessage(5, "请持续输出,直到完毕！");
                    final MapleMonster mm = MapleLifeFactory.getMonster(9001007);
                    int distance = ((c.getPlayer().getJob() >= 300 && c.getPlayer().getJob() < 413) || (c.getPlayer().getJob() >= 1300 && c.getPlayer().getJob() < 1500) || (c.getPlayer().getJob() >= 520 && c.getPlayer().getJob() < 600)) ? 125 : 50;
                    Point p = new Point(c.getPlayer().getPosition().x - distance, c.getPlayer().getPosition().y);
                    //mm.setDpmBelongTo(c.getPlayer());
                    final long newhp = Long.MAX_VALUE;
                    OverrideMonsterStats overrideStats = new OverrideMonsterStats();
                    overrideStats.setOHp(newhp);
                    mm.setHp(newhp);
                    mm.getStats().setBoss(true);
                    mm.setOverrideStats(overrideStats);
                    c.getPlayer().getMap().spawnMonsterOnGroundBelow(mm, p);
                    final MapleMap nowMap = c.getPlayer().getMap();
                    Timer.EventTimer.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            long health = mm.getHp();
                            nowMap.killMonsterDpm(mm);
                            long dpm = (newhp - health) / 15;
                            c.getPlayer().角色伤害更新((int) dpm);
                            c.getPlayer().dropMessage(6, "每秒平均输出伤害: " + dpm + "");
                            c.getPlayer().toggleTestingDPM();
                        }
                    }, 15000);
                } else {
                    c.getPlayer().dropMessage(5, "请先把本次测试完毕。");
                    return true;
                }
            } else {
                c.getPlayer().dropMessage(5, "只能在PVP打擂台广场测试DPS(请由拍卖的传送门进入)");
                return true;
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("@DPM - 測試傷害").toString();
        }
    }

    private PlayerCommand() {
    }
    
}
