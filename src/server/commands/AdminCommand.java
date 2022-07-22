package server.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import com.mysql.jdbc.Connection;
import client.LoginCrypto;
import com.mysql.jdbc.PreparedStatement;
import constants.GameConstants;
import constants.ServerConstants.PlayerGMRank;
import database.DBConPool;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.handler.AutoRegister;
import handling.world.CharacterTransfer;
import handling.world.MapleMessengerCharacter;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.Randomizer;
import server.ShutdownServer;
import server.Timer.EventTimer;
import server.Timer.WorldTimer;
import server.events.MapleEvent;
import server.events.MapleEventType;
import tools.FileoutputUtil;
import tools.HexTool;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CField.NPCPacket;
import tools.packet.CWvsContext;
import tools.packet.PetPacket;

public class AdminCommand {

    private static final Logger logger = LogManager.getLogger(AdminCommand.class);

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.ADMIN;
    }

    public static class UpdatePet extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MaplePet pet = c.getPlayer().getPet(0);
            if (pet == null) {
                return false;
            }
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.petColor(c.getPlayer().getId(), (byte) 0, Color.yellow.getAlpha()), true);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!UpdatePet  - 刷新宠物").toString();
        }
    }

    public static class DamageBuff extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            SkillFactory.getSkill(9101003).getEffect(1).applyTo(c.getPlayer());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!DamageBuff  - DamageBuff").toString();
        }
    }

    public static class MagicWheel extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            List<Integer> items = new LinkedList();
            for (int i = 1; i <= 10; i++) {
                try {
                    items.add(Integer.parseInt(splitted[i]));
                } catch (NumberFormatException ex) {
                    items.add(GameConstants.eventRareReward[GameConstants.eventRareReward.length]);
                }
            }
            int end = Randomizer.nextInt(10);
            String data = "Magic Wheel";
            c.getPlayer().setWheelItem(items.get(end));
            c.getSession().write(CWvsContext.magicWheel((byte) 3, items, data, end));
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!MagicWheel  - MagicWheel").toString();
        }
    }

    public static class UnsealItem extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            short slot = Short.parseShort(splitted[1]);
            Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
            if (item == null) {
                return false;
            }
            final int itemId = item.getItemId();
            Integer[] itemArray = {1002140, 1302000, 1302001,
                1302002, 1302003, 1302004, 1302005, 1302006,
                1302007};
            final List<Integer> items = Arrays.asList(itemArray);
            c.getSession().write(CField.sendSealedBox(slot, 2028162, items)); //sealed box
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            WorldTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemId), itemId, 1, false, false);
                    Item item = ii.getEquipById(items.get(Randomizer.nextInt(items.size())));
                    MapleInventoryManipulator.addbyItem(c, item);
                    c.getSession().write(CField.unsealBox(item.getItemId()));
                    c.getSession().write(CField.EffectPacket.showRewardItemAnimation(2028162, "")); //sealed box
                }
            }, 10000);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!UnsealItem  - UnsealItem").toString();
        }
    }

    public static class CutScene extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getSession().write(NPCPacket.getCutSceneSkip());
            return true;
        }

        public String getMessage() {
            return new StringBuilder().append("!CutScene  - CutScene").toString();
        }
    }

    public static class DemonJob extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getSession().write(NPCPacket.getDemonSelection());
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!DemonJob  - DemonJob").toString();
        }
    }

    public static class NearestPortal extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MaplePortal portal = c.getPlayer().getMap().findClosestPortal(c.getPlayer().getTruePosition());
            c.getPlayer().dropMessage(6, portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());

            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!NearestPortal  - 显示附近的传送点").toString();
        }
    }

    public static class Uptime extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().dropMessage(6, "服务器已运行 " + StringUtil.getReadableMillis(ChannelServer.serverStartTime, System.currentTimeMillis()));
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Uptime  - 显示服务器运行了多久").toString();
        }
    }

    public static class Reward extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            chr.addReward(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]), Integer.parseInt(splitted[5]), Integer.parseInt(splitted[6]), StringUtil.joinStringFrom(splitted, 7));
            chr.updateReward();
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Reward  - Reward").toString();
        }
    }

    public static class DropMessage extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            String type = splitted[1];
            String text = splitted[2];
            if (type == null) {
                c.getPlayer().dropMessage(6, "Syntax error: !dropmessage type text");
                return false;
            }
            if (type.length() > 1) {
                c.getPlayer().dropMessage(6, "Type must be just with one word");
                return false;
            }
            if (text == null && text.length() < 1) {
                c.getPlayer().dropMessage(6, "Text must be 1 letter or more!!");
                return false;
            }
            c.getPlayer().dropMessage(Integer.parseInt(type), text);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!DropMessage  - DropMessage").toString();
        }
    }

    public static class DropMsg extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            String type = splitted[1];
            String text = splitted[2];
            if (type == null) {
                c.getPlayer().dropMessage(6, "Syntax error: !dropmessage type text");
                return false;
            }
            if (type.length() > 1) {
                c.getPlayer().dropMessage(6, "Type must be just with one word");
                return false;
            }
            if (text == null && text.length() < 1) {
                c.getPlayer().dropMessage(6, "Text must be 1 letter or more!!");
                return false;
            }
            //c.getPlayer().dropMsg(Integer.parseInt(type), text);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!DropMsg  - DropMsg").toString();
        }
    }
    

    public static class GMPerson extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]).setGM(Byte.parseByte(splitted[2]));
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!GMPerson  - GMPerson").toString();
        }
    }

    public static class WarpCashShop extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            MapleClient client = chr.getClient();
            final ChannelServer ch = ChannelServer.getInstance(client.getChannel());

            chr.changeRemoval();

            if (chr.getMessenger() != null) {
                MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
                World.Messenger.leaveMessenger(chr.getMessenger().getId(), messengerplayer);
            }
            PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
            PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
            PlayerBuffStorage.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
            World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), -10);
            ch.removePlayer(chr);
            client.updateLoginState(MapleClient.CHANGE_CHANNEL, client.getSessionIPAddress());
            chr.saveToDB(false, false);
            chr.getMap().removePlayer(chr);
            String ipAddr = ChannelServer.getInstance(client.getChannel()).getIP().split(":")[0];
            client.getSession().write(CField.getChannelChange(client, ipAddr, Integer.parseInt(CashShopServer.getIP().split(":")[1])));
            client.setPlayer(null);
            client.setReceiving(false);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!WarpCashShop  - 进入商城").toString();
        }
    }
    

    public static class TestDirection extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getSession().write(CField.UIPacket.getDirectionInfo(StringUtil.joinStringFrom(splitted, 5), Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]), Integer.parseInt(splitted[4]), Integer.parseInt(splitted[5])));
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!TestDirection  - TestDirection").toString();
        }
    }

    public static class ToggleAutoRegister extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            AutoRegister.autoRegister = !AutoRegister.autoRegister;
            c.getPlayer().dropMessage(0, "登入自动注册 " + (AutoRegister.autoRegister ? "开启" : "关闭") + ".");
            System.out.println("登入自动注册 " + (AutoRegister.autoRegister ? "开启" : "关闭") + ".");
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!ToggleAutoRegister  - 是否开启自动注册").toString();
        }
    }

    public static class 注册 extends register {
    }

   public static class register extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String[] splitted) {
            String acc = null;
            String password = null;
            try {
                acc = splitted[1];
                password = splitted[2];
            } catch (Exception ex) {
            }
            if (acc == null || password == null) {
                c.getPlayer().dropMessage(6,"账号或密码异常");
                return false;
            }
            boolean ACCexist = AutoRegister.getAccountExists(acc);
            if (ACCexist) {
                c.getPlayer().dropMessage(6,"帐号已被使用");
                return false;
            }
            if (acc.length() >= 12) {
                c.getPlayer().dropMessage(6,"密码长度过长");
                return false;
            }
            Connection con;
            try {
                 con =  (Connection) DBConPool.getInstance().getDataSource().getConnection();
                //con = (java.sql.Connection) DatabaseConnection.getConnection();
            } catch (Exception ex) {
                System.out.println(ex);
                return false;
            }
            try {
                try (PreparedStatement ps = (PreparedStatement) con.prepareStatement("INSERT INTO accounts (name, password) VALUES (?, ?)")) {
                    ps.setString(1, acc);
                    ps.setString(2, LoginCrypto.hexSha1(password));
                    ps.executeUpdate();
                    ps.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex);
                return false;
            }
            c.getPlayer().dropMessage(1,"[注册完成]账号: " + acc + " 密码: " + password);
            return true;
        }
         @Override
        public String getMessage() {
            return new StringBuilder().append("!注册 [账号] <密码> - 注册账号").toString();
        }
        }
    
    
    
    
    
    
    
    public static class Packet extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getSession().write(HexTool.getByteArrayFromHexString(StringUtil.joinStringFrom(splitted, 1)));
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Packet  - Packet").toString();
        }
    }

    public static class StripEveryone extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            ChannelServer cs = c.getChannelServer();
            for (MapleCharacter mchr : cs.getPlayerStorage().getAllCharacters()) {
                if (c.getPlayer().isGM()) {
                    continue;
                }
                MapleInventory equipped = mchr.getInventory(MapleInventoryType.EQUIPPED);
                MapleInventory equip = mchr.getInventory(MapleInventoryType.EQUIP);
                List<Short> ids = new ArrayList<>();
                for (Item item : equipped.newList()) {
                    ids.add(item.getPosition());
                }
                for (short id : ids) {
                    MapleInventoryManipulator.unequip(mchr.getClient(), id, equip.getNextFreeSlot());
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!StripEveryone  - StripEveryone").toString();
        }
    }

    public static class Strip extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            MapleInventory equipped = victim.getInventory(MapleInventoryType.EQUIPPED);
            MapleInventory equip = victim.getInventory(MapleInventoryType.EQUIP);
            List<Short> ids = new ArrayList<>();
            for (Item item : equipped.newList()) {
                ids.add(item.getPosition());
            }
            for (short id : ids) {
                MapleInventoryManipulator.unequip(victim.getClient(), id, equip.getNextFreeSlot());
            }
            boolean notice = false;
            if (splitted.length > 1) {
                notice = true;
            }
            if (notice) {
                World.Broadcast.broadcastMessage(CWvsContext.broadcastMsg(0, victim.getName() + " has been stripped by " + c.getPlayer().getName()));
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Strip  - Strip").toString();
        }
    }

    public static class MesoEveryone extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainMeso(Integer.parseInt(splitted[1]), true);
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!MesoEveryone  - 给所有人金币").toString();
        }
    }

    public static class ScheduleHotTime extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 1) {
                c.getPlayer().dropMessage(0, "!ScheduleHotTime <Item Id>");
                return false;
            }
            if (!MapleItemInformationProvider.getInstance().itemExists(Integer.parseInt(splitted[1]))) {
                c.getPlayer().dropMessage(0, "Item does not exists.");
                return false;
            }
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (c.canClickNPC()) {
                        mch.gainItem(Integer.parseInt(splitted[1]), 1);
                        mch.getClient().getSession().write(CField.NPCPacket.getNPCTalk(9010010, (byte) 0, "You got the #t" + Integer.parseInt(splitted[1]) + "#, right? Click it to see what's inside. Go ahead and check your item inventory now, if you're curious.", "00 00", (byte) 1, 9010010));
                    }
                }
            }
            System.out.println("Hot Time had been scheduled successfully.");
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!ScheduleHotTime  - ScheduleHotTime").toString();
        }
    }

    public static class WarpAllHere extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() != c.getPlayer().getMapId()) {
                    mch.changeMap(c.getPlayer().getMap(), c.getPlayer().getPosition());
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!WarpAllHere  - 把所有玩家传送到这里").toString();
        }
    }

    public static class DCAll extends CommandExecute {

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
                range = 1;
            }
            if (range == 0) {
                c.getPlayer().getMap().disconnectAll();
            } else if (range == 1) {
                c.getChannelServer().getPlayerStorage().disconnectAll(true);
            } else if (range == 2) {
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.getPlayerStorage().disconnectAll(true);
                }
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!DCAll  - [m|c|w] - 所有玩家断线").toString();
        }
    }

    public static class Shutdown extends CommandExecute {

        protected static Thread t = null;

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            c.getPlayer().dropMessage(6, "正在关闭服务器。");
            if (t == null || !t.isAlive()) {
                t = new Thread(ShutdownServer.getInstance());
                ShutdownServer.getInstance().shutdown();
                t.start();
            } else {
                c.getPlayer().dropMessage(6, "关闭线程已经在进行中或关闭尚未完成。请稍等。");
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Shutdown  - 关闭服务器").toString();
        }
    }

    public static class ShutdownTime extends Shutdown {

        private static ScheduledFuture<?> ts = null;
        private int minutesLeft = 0;

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            minutesLeft = Integer.parseInt(splitted[1]);
            c.getPlayer().dropMessage(6, "服务器将在" + minutesLeft + "分钟后关闭");
            if (ts == null && (t == null || !t.isAlive())) {
                t = new Thread(ShutdownServer.getInstance());
                ts = EventTimer.getInstance().register(new Runnable() {
                    @Override
                    public void run() {
                        if (minutesLeft == 0) {
                            ShutdownServer.getInstance().shutdown();
                            t.start();
                            ts.cancel(false);
                            return;
                        }
                        World.Broadcast.broadcastMessage(CWvsContext.broadcastMsg(0, "服务器将在" + minutesLeft + "分钟后进行停机维护, 请及时安全下线, 以免造成不必要的损失。"));
                        minutesLeft--;
                    }
                }, 60000);
            } else {
                c.getPlayer().dropMessage(6, "关闭线程已经在进行中或关闭尚未完成。请稍等。");
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!ShutdownTime  - <分钟数> - 关闭服务器").toString();
        }
    }

    public static class Sql extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            try (java.sql.Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
                PreparedStatement ps = (PreparedStatement) con.prepareStatement(StringUtil.joinStringFrom(splitted, 1));
                ps.executeUpdate();
            } catch (SQLException ex) {
                FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
                System.err.println(ex);
                return false;
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!Sql - Sql").toString();
        }
    }

    public static class hair extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setHair(id);
            player.updateSingleStat(MapleStat.HAIR, id);
            player.dropMessage(6, "当前发型ID被更改为: " + id);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!hair - 更改发型").toString();
        }

    }

    public static class face extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setFace(id);
            player.updateSingleStat(MapleStat.FACE, id);
            player.dropMessage(5, "当前脸型ID被更改为: " + id);
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!face - 更改脸型").toString();
        }
    }

    public static class 选择活动 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String splitted[]) {
            if (splitted.length < 2) {
                return false;
            }
            final MapleEventType type = MapleEventType.getByString(splitted[1]);
            if (type == null) {
                final StringBuilder sb = new StringBuilder("目前开放的活动有: ");
                for (MapleEventType t : MapleEventType.values()) {
                    sb.append(t.name()).append(",");
                }
                c.getPlayer().dropMessage(5, sb.toString().substring(0, sb.toString().length() - 1));
            }
            final String msg = MapleEvent.scheduleEvent(type, c.getChannelServer());
            if (msg.length() > 0) {
                c.getPlayer().dropMessage(5, msg);
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!选择活动 - 选择活动").toString();
        }
    }

    public static class 活动开始 extends CommandExecute {

        private static ScheduledFuture<?> ts = null;
        private int min = 1, sec = 0;

        @Override
        public boolean execute(final MapleClient c, String splitted[]) {
            if (c.getChannelServer().getEvent() == c.getPlayer().getMapId()) {
                MapleEvent.setEvent(c.getChannelServer(), false);
                if (c.getPlayer().getMapId() == 109020001) {
                    sec = 10;
                    c.getPlayer().dropMessage(5, "已经关闭活动入口，10秒后开始活动。");
                    World.Broadcast.broadcastMessage(CWvsContext.broadcastMsg(6, "频道:" + c.getChannel() + "活动目前已经关闭大门口，10秒后开始活动。"));
                    c.getPlayer().getMap().broadcastMessage(CField.getClock(sec));
                } else {
                    sec = 60;
                    c.getPlayer().dropMessage(5, "已经关闭活动入口，60秒后开始活动。");
                    World.Broadcast.broadcastMessage(CWvsContext.broadcastMsg(6, "频道:" + c.getChannel() + "活动目前已经关闭大门口，60秒后开始活动。"));
                    c.getPlayer().getMap().broadcastMessage(CField.getClock(sec));
                }
                ts = EventTimer.getInstance().register(new Runnable() {

                    @Override
                    public void run() {
                        if (min == 0) {
                            MapleEvent.onStartEvent(c.getPlayer());
                            ts.cancel(false);
                            return;
                        }
                        min--;
                    }
                }, sec * 1000);
                return true;
            } else {
                c.getPlayer().dropMessage(5, "您必须先使用 !选择活动 设定当前频道的活动，并在当前频道活动地图裡使用。");
                return true;
            }
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!活动开始 - 活动开始").toString();
        }
    }

    public static class 倍率设置 extends CommandExecute {

        public boolean execute(MapleClient c, String splitted[]) {
            MapleCharacter player = c.getPlayer();
            MapleCharacter mc = player;
            if (splitted.length > 2) {
                int arg = Integer.parseInt(splitted[2]);
                int seconds = Integer.parseInt(splitted[3]);
                int mins = Integer.parseInt(splitted[4]);
                int hours = Integer.parseInt(splitted[5]);
                int time = seconds + (mins * 60) + (hours * 60 * 60);
                boolean bOk = true;
                if (splitted[1].equals("经验")) {
                    if (arg <= 50) {
                        for (ChannelServer cservs : ChannelServer.getAllInstances()) {
                            cservs.setExpRate(arg);
                            cservs.broadcastPacket(CWvsContext.broadcastMsg(6, "经验倍率已经成功修改为 " + arg + "倍。祝大家游戏开心.经验倍率将在时间到后自动更正！"));
                        }
                    } else {
                        mc.dropMessage(6, "操作已被系统限制。");
                    }
                } else if (splitted[1].equals("爆率")) {//drop
                    if (arg <= 5) {
                        for (ChannelServer cservs : ChannelServer.getAllInstances()) {
                            cservs.setDropRate(arg);
                            cservs.broadcastPacket(CWvsContext.broadcastMsg(6, "爆率倍率已经成功修改为 " + arg + "倍。祝大家游戏开心.经验倍率将在时间到后自动更正！！"));
                        }
                    } else {
                        mc.dropMessage(6,"操作已被系统限制。");
                    }
                } else if (splitted[1].equals("金币")) {
                    if (arg <= 5) {
                        for (ChannelServer cservs : ChannelServer.getAllInstances()) {
                            cservs.setMesoRate(arg);
                            cservs.broadcastPacket(CWvsContext.broadcastMsg(6, "金币倍率已经成功修改为 " + arg + "倍。祝大家游戏开心.经验倍率将在时间到后自动更正！！"));
                        }
                    } else {
                        mc.dropMessage(6,"操作已被系统限制。");
                    }
                } else {
                    bOk = false;
                }
                if (bOk) {
                    final String rate = splitted[1];
                    World.scheduleRateDelay(rate, time);
                } else {
                    mc.dropMessage(6,"使用方法: !倍率设置 <exp经验|drop爆率|meso金币|bossboss爆率|pet> <类> <秒> <分> <时>");

                }
            } else {
                mc.dropMessage(6,"使用方法: !倍率设置 <exp经验|drop爆率|meso金币|bossboss爆率|pet> <类> <秒> <分> <时>");
            }
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!活动开始 - 活动开始").toString();
        }
    }

    private AdminCommand() {
    }
}
