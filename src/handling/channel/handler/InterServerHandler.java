package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.EventConstants;
import constants.GameConstants;
import constants.ServerConfig;
import gui.ZeroMS_UI;
import handling.cashshop.CashShopServer;
import handling.cashshop.handler.CashShopOperation;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.*;
import handling.world.exped.MapleExpedition;
import handling.world.guild.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import scripting.EventInstanceManager;
import scripting.EventManager;
import scripting.NPCScriptManager;
import server.*;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import tools.FileoutputUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CSPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.BuddylistPacket;
import tools.packet.CWvsContext.FamilyPacket;
import tools.packet.CWvsContext.GuildPacket;
import tools.packet.JobPacket.AvengerPacket;

public class InterServerHandler {

    public static void EnterCS(final MapleClient c, final MapleCharacter chr) {
        if (chr.hasBlockedInventory() || chr.getMap() == null || chr.getEventInstance() != null || c.getChannelServer() == null) {
            c.getSession().write(CField.serverBlocked(2));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getPlayer().getChair() > 0) {
            chr.cancelFishingTask();
            chr.setChair(0);
            c.getSession().write(CField.cancelChair(-1));
            chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.MONSTER_RIDING});
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "服务器繁忙，请稍后再试。");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        ChannelServer ch = ChannelServer.getInstance(c.getChannel());
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
        if (ZeroMS_UI.getInstance() != null) {
            ZeroMS_UI.getInstance().updatePlayerList(chr, false);
        }
        c.updateLoginState(3, c.getSessionIPAddress());
        chr.saveToDB(false, false);
        chr.getMap().removePlayer(chr);
        String ipAddr = ChannelServer.getInstance(c.getChannel()).getIP().split(":")[0];
        c.getSession().write(CField.getChannelChange(c, ipAddr, Integer.parseInt(CashShopServer.getIP().split(":")[1])));
        c.setPlayer(null);
        c.setReceiving(false);
    }

    public static void Loggedin(final int playerid, final MapleClient c) {
        try {
            MapleCharacter player;
            CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(playerid);
            if (transfer != null) {

                CashShopOperation.EnterCS(transfer, c);
                return;
            }

            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                transfer = cserv.getPlayerStorage().getPendingCharacter(playerid);
                if (transfer != null) {
                    c.setChannel(cserv.getChannel());
                    break;
                }
            }
            if (transfer == null) { // Player isn't in storage, probably isn't CC
                Triple<String, String, Integer> ip = LoginServer.getLoginAuth(playerid);
                String s = c.getSessionIPAddress();
                if (ip == null || !s.substring(s.indexOf('/') + 1, s.length()).equals(ip.left)) {
                    if (ip != null) {
                        LoginServer.putLoginAuth(playerid, ip.left, ip.mid, ip.right);
                    }

                    //c.getSession().close();
                    //return;
                }
                c.setTempIP(ip.mid);
                c.setChannel(ip.right);
                player = MapleCharacter.loadCharFromDB(playerid, c, true);
                //System.out.println("Load char from DB");

            } else {
                player = MapleCharacter.ReconstructChr(transfer, c, true);

            }
            final ChannelServer channelServer = c.getChannelServer();
            if (!LoginServer.CanLoginKey(player.getLoginKey(), player.getAccountID())) {
                FileoutputUtil.logToFile("logs/Data/客户端登入key异常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().getRemoteAddress().toString().split(":")[0] + " 账号: " + c.getAccountName() + " 客户端key：" + LoginServer.getLoginKey(player.getAccountID()) + " 服务端key：" + player.getLoginKey() + " 进入游戏1");
                World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] 非法登入 账号 " + c.getAccountName()));
                c.getSession().close();
                return;
            }
            c.setPlayer(player);
            c.setAccID(player.getAccountID());
            if (!c.CheckIPAddress()) { // Remote hack
                //c.getSession().close();

                //return;
            }
            final int state = c.getLoginState();
            //System.out.println("State = " + c.getLoginState());
            boolean allowLogin = false;
            if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL || state == MapleClient.LOGIN_NOTLOGGEDIN) {
                allowLogin = !World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()));
            }
            if (!allowLogin) {
                c.setPlayer(null);
                c.getSession().close();
                return;
            }
            c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
            channelServer.addPlayer(player);
            if (ZeroMS_UI.getInstance() != null) {
            ZeroMS_UI.getInstance().updatePlayerList(player, true);
            }
            player.giveCoolDowns(PlayerBuffStorage.getCooldownsFromStorage(player.getId()));// BUFF技能
            player.silentGiveBuffs(PlayerBuffStorage.getBuffsFromStorage(player.getId()));// 冷卻時間
            player.giveSilentDebuff(PlayerBuffStorage.getDiseaseFromStorage(player.getId()));// 疾病狀態

                if (LoginServer.getChrPos() != null) {
                    if (LoginServer.getChrPos().containsKey(player.getId())) {
                        LoginServer.RemoveChrPos(player.getId());
                    }
                
            }

            //c.getSession().write(CWvsContext.updateCrowns(new int[]{-1, -1, -1, -1, -1}));
            c.getSession().write(CField.getCharInfo(player));
            c.getSession().write(CWvsContext.temporaryStats_Reset()); // .
            if (!LoginServer.CanLoginKey(player.getLoginKey(), player.getAccountID())) {
                FileoutputUtil.logToFile("logs/Data/客户端登入key异常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().getRemoteAddress().toString().split(":")[0] + " 账号: " + c.getAccountName() + " 客户端key：" + LoginServer.getLoginKey(player.getAccountID()) + " 服务端key：" + player.getLoginKey() + " 进入游戏2");
                World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] 非法登入 账号 " + c.getAccountName()));
                c.getSession().close();
                return;
            }
            //PlayersHandler.calcHyperSkillPointCount(c);
            c.getSession().write(CSPacket.enableCSUse());

            c.getSession().write(CWvsContext.updateSkills(c.getPlayer().getSkills(), false));//skill to 0 "fix"

            //管理員上線預設隱藏
            if (ServerConfig.Managestealth){//管理隐身
            if (player.isGM()) {
                SkillFactory.getSkill(9001004).getEffect(1).applyTo(player);
                if (ServerConfig.Managementacceleration){//管理加速
                if (GameConstants.isKOC(player.getJob())) {
                    SkillFactory.getSkill(10001010).getEffect(1).applyTo(player, 2100000000);
                } else if (GameConstants.isAran(player.getJob())) {
                    SkillFactory.getSkill(20001010).getEffect(1).applyTo(player, 2100000000);
                } else {
                    SkillFactory.getSkill(1010).getEffect(1).applyTo(player, 2100000000);
            }
        }        }        }  
            player.getMap().addPlayer(player);
            try {
                // Start of buddylist
                final int buddyIds[] = player.getBuddylist().getBuddyIds();
                World.Buddy.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
                if (player.getParty() != null) {
                    final MapleParty party = player.getParty();
                    World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));

                    if (party != null && party.getExpeditionId() > 0) {
                        final MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                        if (me != null) {
                            c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(me, false, true));
                        }
                    }
                }
                final CharacterIdChannelPair[] onlineBuddies = World.Find.multiBuddyFind(player.getId(), buddyIds);
                for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                    player.getBuddylist().get(onlineBuddy.getCharacterId()).setChannel(onlineBuddy.getChannel());
                }
                c.getSession().write(BuddylistPacket.updateBuddylist(player.getBuddylist().getBuddies()));

                // Start of Messenger
                final MapleMessenger messenger = player.getMessenger();
                if (messenger != null) {
                    World.Messenger.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()));
                    World.Messenger.updateMessenger(messenger.getId(), c.getPlayer().getName(), c.getChannel());
                }

                // Start of Guild and alliance
                if (player.getGuildId() > 0) {
                    World.Guild.setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                    c.getSession().write(GuildPacket.showGuildInfo(player));
                    final MapleGuild gs = World.Guild.getGuild(player.getGuildId());
                    if (gs != null) {
                        final List<byte[]> packetList = World.Alliance.getAllianceInfo(gs.getAllianceId(), true);
                        if (packetList != null) {
                            for (byte[] pack : packetList) {
                                if (pack != null) {
                                    c.getSession().write(pack);
                                }
                            }
                        }
                    } else { //guild not found, change guild id
                        player.setGuildId(0);
                        player.setGuildRank((byte) 5);
                        player.setAllianceRank((byte) 5);
                        player.saveGuildStatus();
                    }
                }
                if (player.getFamilyId() > 0) {
                    World.Family.setFamilyMemberOnline(player.getMFC(), true, c.getChannel());
                }
                c.getSession().write(FamilyPacket.getFamilyData());
                c.getSession().write(FamilyPacket.getFamilyInfo(player));
            } catch (Exception e) {
                FileoutputUtil.outputFileError(FileoutputUtil.Login_Error, e);
            }
            player.getClient().getSession().write(CWvsContext.broadcastMsg(channelServer.getServerMessage()));
            player.sendMacros();
            player.showNote();
            player.sendImp();
            player.updatePartyMemberHP();
            player.startFairySchedule(false);
            player.baseSkills(); //fix people who've lost skills.

            c.getSession().write(CField.getKeymap(player.getKeyLayout()));
            player.updatePetAuto();      
            if (ServerConfig.Loginhelp) {
            if (player.getGMLevel() > 0 && player.getBossLog("管理上线提示") == 0) {
                player.dropMessage(5, "指令: [!help] 查看管理员指令");
                player.dropMessage(5, "指令: [@帮助] 查看玩家指令");
            } else if (player.getGMLevel() <= 0 && player.getBossLog("玩家上线提示") == 0) {
                player.dropMessage(5, "指令: [@帮助] 查看玩家指令");
            }
        }
                    
        if (ServerConfig.Offlinehangup && player.getMapId() == ServerConfig.offlinemap) {
			long nowTimestamp = System.currentTimeMillis();
			long 奖励时间 = nowTimestamp - player.getLastOfflineTime();
			if(奖励时间 >= 60000){
				int 离线时间 = (int) 奖励时间 / 60000;
				if(离线时间 >= 1440){
					离线时间 = 1440;
					c.getPlayer().dropMessage(5, "您的离线时间超过24小时,离线奖励按照一天算。");
				}
                                final int 点卷数量 = ServerConfig.Offlinecouponcounting* 离线时间;//离线点券
                                final int 抵用数量 = ServerConfig.Offlineoffset * 离线时间;//离线抵用
                                final int 豆豆数量 = ServerConfig.OfflineDoudou * 离线时间;//离线泡点豆豆
                                final int 金币数量 = ServerConfig.Offlinegoldcoins * 离线时间;//离线泡点金币
                                final int 经验数量 = ServerConfig.Offlineexperience * 离线时间;//离线泡点经验
                                player.gainExp(经验数量, false, false, false);//给固定经验
				player.modifyCSPoints(2, 抵用数量);
				player.modifyCSPoints(1, 点卷数量);
				player.gainBeans(豆豆数量);
                                player.gainMeso((金币数量), true);//给金币
				c.getPlayer().dropMessage(5, "您的离线时间"+离线时间+"分钟,离线获得[" + 经验数量 + "] 经验 ["+金币数量+"] 金币 ["+抵用数量+"] 抵用卷 ["+点卷数量+"] 点卷 ["+豆豆数量+"] 豆豆 !");
				player.updateOfflineTime1();
			}
	}
            player.expirationTask(true, transfer == null);
            c.getSession().write(CWvsContext.updateMaplePoint(player));
            if (player.getJob() == 132) { // DARKKNIGHT
                player.checkBerserk();
            }
            player.spawnClones();
            player.spawnSavedPets();
            if (player.getStat().equippedSummon > 0) {
                SkillFactory.getSkill(player.getStat().equippedSummon + (GameConstants.getBeginnerJob(player.getJob()) * 1000)).getEffect(1).applyTo(player);
            }

            boolean changeChannel = true;
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {

                Map<String, String> isSquadPlayerID = new HashMap<>();
                isSquadPlayerID.put("ZakumBattle", "ZakumBattle");
                isSquadPlayerID.put("ScarTarBattle", "ScarTarBattle");
                isSquadPlayerID.put("HorntailBattle", "HorntailBattle");
                isSquadPlayerID.put("PinkBeanBattle", "Pinkbean");
                if (isSquadPlayerID.size() > 0) {
                    for (final Map.Entry<String, String> emeim : isSquadPlayerID.entrySet()) {
                        String em = emeim.getKey();
                        String eim = emeim.getValue();
                        EventManager bossem = cserv.getEventSM().getEventManager(em);
                        if (bossem != null && bossem.getInstance(eim) != null) {
                            EventInstanceManager bosseim = bossem.getInstance(eim);
                            if (bosseim != null && (bosseim.getProperty("isSquadPlayerID_" + player.getId()) != null)) {
                                String propsa = bosseim.getProperty("isSquadPlayerID_" + player.getId());
                                if (propsa != null && propsa.equals("1")) {
                                    if (c.getChannel() != cserv.getChannel()) {
                                        c.getPlayer().changeChannel(cserv.getChannel());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (changeChannel) {
                Map<String, String> isSquadPlayerID = new HashMap<>();
                isSquadPlayerID.put("ZakumBattle", "ZakumBattle");
                isSquadPlayerID.put("ScarTarBattle", "ScarTarBattle");
                isSquadPlayerID.put("HorntailBattle", "HorntailBattle");
                isSquadPlayerID.put("PinkBeanBattle", "Pinkbean");
                if (isSquadPlayerID.size() > 0) {
                    for (final Map.Entry<String, String> emeim : isSquadPlayerID.entrySet()) {
                        String em = emeim.getKey();
                        String eim = emeim.getValue();
                        final EventManager bossem = player.getClient().getChannelServer().getEventSM().getEventManager(em);
                        if (bossem != null && bossem.getInstance(eim) != null) {
                            EventInstanceManager bosseim = bossem.getInstance(eim);
                            if ((bosseim != null) && (bosseim.getProperty("isSquadPlayerID_" + player.getId()) != null)) {
                                String propsa = bosseim.getProperty("isSquadPlayerID_" + player.getId());
                                if (propsa != null && propsa.equals("1")) {
                                    bosseim.registerPlayer(player);
                                }
                            }
                        }
                    }
                }
            }

            player.getClient().getSession().write(CWvsContext.broadcastMsg(channelServer.getServerMessage()));
        if (c.getPlayer().hasEquipped(1122017)) {
            player.dropMessage(5, "您装备了精灵吊坠！打猎时可以额外获得10%的道具佩戴经验奖励！");
        }
           if (ServerConfig.Onlineannouncement) {
           if (player.getGender() == 0) {
                   World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(0x0C, c.getChannel(), "[登录公告] 【帅哥】" + player.getName() + "（" + c.getPlayer().getJobName(c.getPlayer().getJob())+ "） "  + " : " +"进入游戏，大家热烈欢迎他吧！！！"));
                } else {
                   World.Broadcast.broadcastSmega(CWvsContext.broadcastMsg(0x0C, c.getChannel(), "[登录公告] 【美女】" + player.getName() + "（" + c.getPlayer().getJobName(c.getPlayer().getJob())+ "） "  + " : " +"进入游戏，大家热烈欢迎她吧！！！"));
                }                }
            
            boolean Doubleburstchannel = ServerConfig.Doubleburstchannel;
            int Doubleexplosionchannel = ServerConfig.Doubleexplosionchannel;
            int Channelnumber = ServerConfig.Channelnumber;
            if (c.getChannel() == Channelnumber && !c.getPlayer().haveItem(Doubleexplosionchannel, 1, false, false)) { //双爆频道票 5220002
                c.getPlayer().dropMessage(1, "对不起，你缺少特殊物品，无法进入 " + Channelnumber + " 频道, 该频道为双倍爆率频道！");
                c.getPlayer().changeChannel(1);
            }
   //     }
            
            if (EventConstants.DoubleExpTime) {
                player.getClient().getSession().write(CWvsContext.broadcastMsg(11, player.getClient().getChannel(), " [全服贡献]" + " : " + "目前是全服贡献双倍经验时间。"));
            }
        } catch (Exception e) {
        }
    }

    public static final void ChangeChannel(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr, final boolean room) {
        if (chr == null || chr.hasBlockedInventory() || chr.getEventInstance() != null || chr.getMap() == null || chr.isInBlockedMap() || FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getPlayer().getChair() > 0) {
            chr.cancelFishingTask();
            chr.setChair(0);
            c.getSession().write(CField.cancelChair(-1));
            chr.cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.MONSTER_RIDING});
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "服务器正忙着。请在不到一分钟的时间再试一次.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final int chc = slea.readByte() + 1;
        int mapid = 0;
        if (room) {
            mapid = slea.readInt();
        }
        chr.updateTick(slea.readInt());
        if (!World.isChannelAvailable(chc, chr.getWorld())) {
            chr.dropMessage(1, "请求被拒绝由于未知的错误.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (room && (mapid < 910000001 || mapid > 910000022)) {
            chr.dropMessage(1, "请求被拒绝由于未知的错误.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (room) {
            if (chr.getMapId() == mapid) {
                if (c.getChannel() == chc) {
                    chr.dropMessage(1, "你已经在 " + chr.getMap().getMapName());
                    c.getSession().write(CWvsContext.enableActions());
                } else { // diff channel
                    chr.changeChannel(chc);
                }
            } else { // diff map
                if (c.getChannel() != chc) {
                    chr.changeChannel(chc);
                }
                final MapleMap warpz = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
                if (warpz != null) {
                    chr.changeMap(warpz, warpz.getPortal("out00"));
                } else {
                    chr.dropMessage(1, "请求被拒绝由于未知的错误.");
                    c.getSession().write(CWvsContext.enableActions());
                }
            }
        } else {
            chr.changeChannel(chc);
        }
    }

    private InterServerHandler() {
    }
}
