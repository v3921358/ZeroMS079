package handling.login.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.PartTimeJob;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.JobConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import constants.WorldConstants;
import constants.WorldConstants.TespiaWorldOption;
import constants.WorldConstants.WorldOption;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginInformationProvider.JobType;
import handling.login.LoginServer;
import handling.login.LoginWorker;
import handling.world.World;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.MapleItemInformationProvider;
import server.ServerProperties;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import static tools.FileoutputUtil.CurrentReadable_Time;
import tools.HexTool;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.LoginPacket;
import tools.packet.PacketHelper;

public class CharLoginHandler {

    private static boolean loginFailCount(final MapleClient c) {
        c.loginAttempt++;
        return c.loginAttempt > 4;
    }

    public static void handleAuthRequest(final LittleEndianAccessor slea, final MapleClient c) {
        //System.out.println("Sending response to client.");
        int request = slea.readInt();
        int response;

        response = ((request >> 5) << 5) + (((((request & 0x1F) >> 3) ^ 2) << 3) + (7 - (request & 7)));
        response |= ((request >> 7) << 7);
        response -= 1; //-1 again on v143

        c.getSession().write(LoginPacket.sendAuthResponse(response));
    }

    public static final void login(final LittleEndianAccessor slea, final MapleClient c) {
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();
        slea.read(6);//网卡mac AC 67 5D 3B 73 82 
        byte[] bytes = new byte[6];
        for (int i = 0; i < 4; i++) {//88 EE F3 54
            bytes[i] = slea.readByte();
        }
        slea.readInt();//00 00 00 00
        for (int i = 4; i < 6; i++) {//8C 9A 
            bytes[i] = slea.readByte();
        }
        slea.read(4);//00 00 00 00
        StringBuilder sps = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sps.append(HexTool.toString(bytes[i]));
            sps.append("-");
        }
        String macData = sps.toString();
        macData = macData.substring(0, macData.length() - 1);

        int loginok = c.login(login, pwd, false);
        final Calendar tempbannedTill = c.getTempBanCalendar();
        c.updateMacs(macData);
        final boolean ipBan = c.hasBannedIP();
        final boolean macBan = c.hasBannedMac();

        /*if (!"/127.0.0.1".equals(c.getSessionIPAddress())) {
            c.getSession().write(CWvsContext.broadcastMsg(1, " 防挂提示：\r\n 禁止登陆：错误代码：0\r\n登录器参数不正确\r\n检测到你未使用本服专用登录器\r\n请勿使用万能登录器登录！"));
            return;
        }*/
        
        
        if (loginok == 0 && (/*ipBan ||*/macBan) && !c.isGm()) {
            loginok = 3;
            if (macBan) {
                // this is only an ipban o.O" - maybe we should refactor this a bit so it's more readable
                //MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Enforcing account ban, account " + login, false, 4, false);
                c.getSession().write(LoginPacket.getTempBan(-1, c.getBanReason()));
                return;
            }
        }
        if (loginok == 5) {
            if (AutoRegister.autoRegister) {
                if (AutoRegister.createAccount(login, pwd, c.getSession().getRemoteAddress().toString(), macData)) {
                    c.getSession().write(CWvsContext.broadcastMsg(1, "<" + ServerConfig.serverName + ">\r\n恭喜你注册成功~\r\n账号:" + login + "密码:" + pwd + "\r\n请重新登陆。"));
                    c.getSession().write(LoginPacket.getLoginFailed(1));
                    return;
                } else {
                    c.getSession().write(CWvsContext.broadcastMsg(1, "<" + ServerConfig.serverName + ">\r\n很遗憾~\r\n账号注册失败."));
                    c.getSession().write(LoginPacket.getLoginFailed(1));
                    return;
                }
            }
        }
        if (loginok != 5) {
            try {
                for (MapleCharacter chr : CashShopServer.getPlayerStorage().getAllCharacters()) {
                    if (chr != null) {
                        if (chr.getClient().getAccID() == c.getAccID()) {
                            if (!chr.getClient().equals(c)) {
                                CashShopServer.getPlayerStorage().deregisterPlayer(chr);
                                MapleClient hc = chr.getClient();
                                hc.disconnect(false, true);
                                hc.getSession().close();
                                loginok = 7;
                            }
                        }
                    }
                }
                for (ChannelServer cs : ChannelServer.getAllInstances()) {
                    for (MapleCharacter chr : cs.getPlayerStorage().getAllCharacters()) {
                        if (chr.getClient().getAccID() == c.getAccID()) {
                            if (!chr.getClient().equals(c)) {
                                MapleClient hc = chr.getClient();
                                hc.disconnect(true, false);
                                hc.getSession().close();
                                loginok = 7;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        if (loginok == 0 && c.getGender() == 10) {
            c.getSession().write(LoginPacket.getGenderNeeded(c));
            return;
        }
        if (loginok == 3) {
            c.getSession().write(CWvsContext.broadcastMsg(1, ServerConfig.Accountnumberprohibitionprompt));
            c.getSession().write(LoginPacket.getTempBan(-1, c.getBanReason()));
            return;
        }
        if (loginok != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getLoginFailed(loginok));
            } else {
                c.getSession().close();
            }
        } else if (tempbannedTill.getTimeInMillis() != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getTempBan(PacketHelper.getTime(tempbannedTill.getTimeInMillis()), c.getBanReason()));
            } else {
                c.getSession().close();
            }
        } else {
            c.loginAttempt = 0;
            int loginkey = (int) ((Math.random() * 9 + 1) * 100000);
            c.setLoginKey(loginkey);
            c.updateLoginKey(loginkey, c.getAccID());
            LoginServer.addLoginKey(loginkey, c.getAccID());
            FileoutputUtil.logToFile("logs/data/登入账号.txt", "\r\n 时间　[" + FileoutputUtil.NowTime() + "]" + " IP 地址 : " + c.getSessionIPAddress() + " MAC: " + macData + " 账号：　" + login + " 密码：" + pwd);
            LoginWorker.registerClient(c);
        }
    }

    public static void LicenseRequest(LittleEndianAccessor slea, MapleClient c) {
        if (slea.readByte() == 1) {
            c.getSession().write(LoginPacket.licenseResult());
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, c.getSessionIPAddress());
        } else {
            c.getSession().close();
        }
    }

    public static final void SetGenderRequest(final LittleEndianAccessor slea, final MapleClient c) {
        byte gender = slea.readByte();
        String username = slea.readMapleAsciiString();

        if (gender != 0 && gender != 1) {
            c.getSession().close();
            return;
        }
        if (c.getAccountName().equals(username)) {
            c.setGender(gender);
            c.updateGender();
            c.getSession().write(LoginPacket.getGenderChanged(c));
            c.getSession().write(LoginPacket.getLoginFailed(22));
            c.updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, c.getSessionIPAddress());
        } else {
            c.getSession().close();
        }
    }

    public static void ServerListRequest(final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        for (WorldOption servers : WorldOption.values()) {
            if (WorldOption.getById(servers.getWorld()).show() && servers != null) {
                c.getSession().write(LoginPacket.getServerList(servers.getWorld(), LoginServer.getLoad()));

            }
        }
        c.getSession().write(LoginPacket.getEndOfServerList());
    }

    public static void ServerStatusRequest(final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        // 0 = Select world normally
        // 1 = "Since there are many users, you may encounter some..."
        // 2 = "The concurrent users in this world have reached the max"
        final int numPlayer = LoginServer.getUsersOn();
        final int userLimit = LoginServer.getUserLimit();
        if (numPlayer >= userLimit) {
            c.getSession().write(LoginPacket.getServerStatus(2));
        } else if (numPlayer * 2 >= userLimit) {
            c.getSession().write(LoginPacket.getServerStatus(1));
        } else {
            c.getSession().write(LoginPacket.getServerStatus(0));
        }
    }

    public static void CharlistRequest(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }

        final int server = slea.readByte();
        final int channel = slea.readByte() + 1;
        if (!World.isChannelAvailable(channel, server) || !WorldOption.isExists(server)) {
            c.getSession().write(LoginPacket.getLoginFailed(10)); //cannot process so many
            return;
        }

        if (!WorldOption.getById(server).isAvailable() && !(c.isGm() && server == WorldConstants.gmserver)) {
            c.getSession().write(CWvsContext.broadcastMsg(1, "目前世界 " + WorldConstants.getNameById(server) + " 不开放. \r\n请选择其他世界."));
            c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, but it is used to unstuck
            return;
        }

        //System.out.println("Client " + c.getSession().getRemoteAddress().toString().split(":")[0] + " is connecting to server " + server + " channel " + channel + "");
        final List<MapleCharacter> chars = c.loadCharacters(server);
        if (chars != null && ChannelServer.getInstance(channel) != null) {
            c.setWorld(server);
            System.out.println("【" + CurrentReadable_Time() + "】【账号登录】账号" + c.getAccountName() + " 连接到世界服务器: " + ServerProperties.getProperty("serverName") + " 频道: " + channel);
            c.setChannel(channel);
            //c.getSession().write(LoginPacket.getSecondAuthSuccess(c));
            //c.getSession().write(LoginPacket.getChannelSelected());
            c.getSession().write(LoginPacket.getCharList(c.getSecondPassword(), chars, c.getCharacterSlots()));
        } else {
            c.getSession().close();
        }
    }

    public static void updateCCards(final LittleEndianAccessor slea, final MapleClient c) {
        if (slea.available() != 36 || !c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        final Map<Integer, Integer> cids = new LinkedHashMap<>();
        for (int i = 1; i <= 9; i++) { // 6 chars
            final int charId = slea.readInt();
            if ((!c.login_Auth(charId) && charId != 0) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
                c.getSession().close();
                return;
            }
            cids.put(i, charId);
        }
        c.updateCharacterCards(cids);
    }

    public static void CheckCharName(final String name, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        LoginInformationProvider li = LoginInformationProvider.getInstance();
        boolean nameUsed = true;
        if (MapleCharacterUtil.canCreateChar(name, c.isGm())) {
            nameUsed = false;
        }
        if (li.isForbiddenName(name) && !c.isGm()) {
            nameUsed = false;
        }
        c.getSession().write(LoginPacket.charNameResponse(name, nameUsed));
    }

    public static void CreateChar(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        String name;
        byte gender, skin;
        short subcategory;
        int face, hair, top, bottom, shoes, weapon;
        JobType job;
        name = slea.readMapleAsciiString();
        if (!MapleCharacterUtil.canCreateChar(name, false)) {
            System.out.println("char name hack: " + name);
            return;
        }
        int job_type = slea.readInt();
        job = JobType.getByType(job_type);
        if (job == null) {
            System.out.println("New job type found: " + job_type);
            return;
        } else if(job.id == 0){//冒险家
            if(!ServerConfig.adventurer){
                c.getSession().write(CWvsContext.broadcastMsg(1, "冒险家被禁止创建"));
                c.getSession().write(LoginPacket.getLoginFailed(1));
                return;
            }
        } else if(job.id == 1000){//骑士团
            if(!ServerConfig.Knights){
                c.getSession().write(CWvsContext.broadcastMsg(1, "骑士团被禁止创建"));
                c.getSession().write(LoginPacket.getLoginFailed(1));
                return;
            }
        } else if(job.id == 2000){//战神
            if(!ServerConfig.GodofWar){
                c.getSession().write(CWvsContext.broadcastMsg(1, "战神被禁止创建"));
                c.getSession().write(LoginPacket.getLoginFailed(1));
                return;
            }
        }
        for (JobConstants.LoginJob j : JobConstants.LoginJob.values()) {
            if (j.getJobType() == job_type) {
                if (j.getFlag() != JobConstants.LoginJob.JobFlag.ENABLED.getFlag()) {
                    System.out.println("job was tried to be created while not enabled");
                    return;
                }
            }
        }
        subcategory = 0;
        gender = c.getGender();
        skin = 0;
        face = slea.readInt();
        hair = slea.readInt();

        top = slea.readInt();

        bottom = slea.readInt();

        shoes = slea.readInt();
        weapon = slea.readInt();
        
        int index = 0;
        int[] items = new int[]{face, hair, top, bottom, shoes, weapon};
        for (int k : items) {
            if (k > 1000) {
                if (!LoginInformationProvider.getInstance().isEligibleItem(gender, index, /*job == JobType.影武者 ? 1 : */ job.id, k)) {
                    System.out.println(gender + " | " + index + " | " + job.type + " | " + k);
                    return;
                }
                index++;
            }
        }

        MapleCharacter newchar = MapleCharacter.getDefault(c, job);
        newchar.setWorld((byte) c.getWorld());
        newchar.setFace(face);
        newchar.setSecondFace(face);

        newchar.setHair(hair);
        newchar.setSecondHair(hair);
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor(skin);

        final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();
        final MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
        Item item;
        //-1 Hat | -2 Face | -3 Eye acc | -4 Ear acc | -5 Topwear 
        //-6 Bottom | -7 Shoes | -9 Cape | -10 Shield | -11 Weapon
        //todo check zero's beta weapon slot
        int[][] equips = new int[][]{{top, -5}, {bottom, -6}, {shoes, -7}, {weapon, -11}};
        for (int[] i : equips) {
            if (i[0] > 0) {
                item = li.getEquipById(i[0]);
                item.setPosition((byte) i[1]);
                item.setGMLog("Character Creation");
                equip.addFromDB(item);
            }
        }
        // Additional skills for all first job classes. Some skills are not added by default,
        // so adding the skill ID here between the {}, will give the skills you entered to the desired job.
        int[][] skills = new int[][]{
            {},//Resistance
            {},//Explorer
            {},//Cygnus
            {},//Aran
            {20010022, 20010194},//Evan
            {20020109, 20021110, 20020111, 20020112}, //Mercedes
            {},//Demon
            {},//Phantom
            {},//Dualblade
            {50001214},//Mihile
            {20040216, 20040217, 20040218, 20040219, 20040220, 20040221, 20041222},//Luminous
            {},//Kaiser
            {60011216, 60010217, 60011218, 60011219, 60011220, 60011221, 60011222},//AngelicBuster
            {},//Cannoneer
            {30020232, 30020233, 30020234, 30020240, 30021238},//Xenon
            {100000279, 100000282, 100001262, 100001263, 100001264, 100001265, 100001266, 100001268},//Zero
            {228, 80001151},//Jett
            {},//Hayato
            {40020000, 40020001, 40020002, 40021023, 40020109},//Kanna
            {80001152, 110001251}//BeastTamer
        };
        if (skills[job.type].length > 0) {
            final Map<Skill, SkillEntry> ss = new HashMap<>();
            Skill s;
            for (int i : skills[job.type]) {
                s = SkillFactory.getSkill(i);
                int maxLevel = s.getMaxLevel();
                if (maxLevel < 1) {
                    maxLevel = s.getMasterLevel();
                }
                ss.put(s, new SkillEntry((byte) 1, (byte) maxLevel, -1));
            }
            if (job == JobType.Zero) {
                ss.put(SkillFactory.getSkill(101000103), new SkillEntry((byte) 8, (byte) 10, -1));
                ss.put(SkillFactory.getSkill(101000203), new SkillEntry((byte) 8, (byte) 10, -1));
            }
            if (job == JobType.BeastTamer) {
                ss.put(SkillFactory.getSkill(110001511), new SkillEntry((byte) 0, (byte) 30, -1));
                ss.put(SkillFactory.getSkill(110001512), new SkillEntry((byte) 0, (byte) 5, -1));
                ss.put(SkillFactory.getSkill(110000513), new SkillEntry((byte) 0, (byte) 30, -1));
                ss.put(SkillFactory.getSkill(110000515), new SkillEntry((byte) 0, (byte) 10, -1));
            }
            newchar.changeSkillLevel_Skip(ss, false);
        }
        int[][] guidebooks = new int[][]{{4161001, 0}, {4161047, 1}, {4161048, 2000}, {4161052, 2001}, {4161054, 3}, {4161079, 2002}};
        int guidebook = 0;
        for (int[] i : guidebooks) {
            if (newchar.getJob() == i[1]) {
                guidebook = i[0];
            } else if (newchar.getJob() / 1000 == i[1]) {
                guidebook = i[0];
            }
        }
        if (guidebook > 0) {
            newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(guidebook, (byte) 0, (short) 1, (byte) 0));
        }
        if (job == JobType.Cygnus) {
            newchar.setQuestAdd(MapleQuest.getInstance(20022), (byte) 1, "1");
            newchar.setQuestAdd(MapleQuest.getInstance(20010), (byte) 1, null); //>_>_>_> ugh
            newchar.setQuestAdd(MapleQuest.getInstance(20000), (byte) 1, null); //>_>_>_> ugh
            newchar.setQuestAdd(MapleQuest.getInstance(20015), (byte) 1, null); //>_>_>_> ugh
            newchar.setQuestAdd(MapleQuest.getInstance(20020), (byte) 1, null); //>_>_>_> ugh
        }

        if (job == JobType.Phantom) {
            newchar.getStat().maxhp = 850;
            newchar.getStat().hp = 140;
            newchar.getStat().maxmp = 805;
            newchar.getStat().mp = 38;
        }

        if (job == JobType.Zero) {
            newchar.setLevel((short) 100);
            newchar.getStat().str = 518;
            newchar.getStat().maxhp = 6910;
            newchar.getStat().hp = 6910;
            newchar.getStat().maxmp = 100;
            newchar.getStat().mp = 100;
            newchar.setRemainingSp(3, 0); //alpha
            newchar.setRemainingSp(3, 1); //beta
        }

        if (job == JobType.BeastTamer) {
            newchar.setLevel((short) 10);
            newchar.getStat().maxhp = 567;
            newchar.getStat().hp = 551;
            newchar.getStat().maxmp = 270;
            newchar.getStat().mp = 263;
            newchar.setRemainingAp(45);
            newchar.setRemainingSp(3, 0);
        }

        if (MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm()) && (c.isGm() || c.canMakeCharacter(c.getWorld()))) {
            MapleCharacter.saveNewCharToDB(newchar, job, subcategory);
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
            c.createdChar(newchar.getId());
            //newchar.newCharRewards();
        } else {
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
        }
    }

    public static void CreateUltimate(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.getPlayer().isGM() && (!c.isLoggedIn() || c.getPlayer() == null || c.getPlayer().getLevel() < 120 || c.getPlayer().getMapId() != 130000000 || c.getPlayer().getQuestStatus(20734) != 0 || c.getPlayer().getQuestStatus(20616) != 2 || !GameConstants.isKOC(c.getPlayer().getJob()) || !c.canMakeCharacter(c.getPlayer().getWorld()))) {
            c.getSession().write(CField.createUltimate(2));
            //Character slots are full. Please purchase another slot from the Cash Shop.
            return;
        }
        //System.out.println(slea.toString());
        final String name = slea.readMapleAsciiString();
        final int job = slea.readInt(); //job ID

        final int face = slea.readInt();
        final int hair = slea.readInt();

        //No idea what are these used for:
        final int hat = slea.readInt();
        final int top = slea.readInt();
        final int glove = slea.readInt();
        final int shoes = slea.readInt();
        final int weapon = slea.readInt();

        final byte gender = c.getPlayer().getGender();

        //JobType errorCheck = JobType.Adventurer;
        //if (!LoginInformationProvider.getInstance().isEligibleItem(gender, 0, errorCheck.type, face)) {
        //    c.getSession().write(CWvsContext.enableActions());
        //    return;
        //}
        JobType jobType = JobType.UltimateAdventurer;

        MapleCharacter newchar = MapleCharacter.getDefault(c, jobType);
        newchar.setJob(job);
        newchar.setWorld(c.getPlayer().getWorld());
        newchar.setFace(face);
        newchar.setHair(hair);
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor((byte) 3); //troll
        newchar.setLevel((short) 50);
        newchar.getStat().str = (short) 4;
        newchar.getStat().dex = (short) 4;
        newchar.getStat().int_ = (short) 4;
        newchar.getStat().luk = (short) 4;
        newchar.setRemainingAp((short) 254); //49*5 + 25 - 16
        newchar.setRemainingSp(job / 100 == 2 ? 128 : 122); //2 from job advancements. 120 from leveling. (mages get +6)
        newchar.getStat().maxhp += 150; //Beginner 10 levels
        newchar.getStat().maxmp += 125;
        switch (job) {
            case 110:
            case 120:
            case 130:
                newchar.getStat().maxhp += 600; //Job Advancement
                newchar.getStat().maxhp += 2000; //Levelup 40 times
                newchar.getStat().maxmp += 200;
                break;
            case 210:
            case 220:
            case 230:
                newchar.getStat().maxmp += 600;
                newchar.getStat().maxhp += 500; //Levelup 40 times
                newchar.getStat().maxmp += 2000;
                break;
            case 310:
            case 320:
            case 410:
            case 420:
            case 520:
                newchar.getStat().maxhp += 500;
                newchar.getStat().maxmp += 250;
                newchar.getStat().maxhp += 900; //Levelup 40 times
                newchar.getStat().maxmp += 600;
                break;
            case 510:
                newchar.getStat().maxhp += 500;
                newchar.getStat().maxmp += 250;
                newchar.getStat().maxhp += 450; //Levelup 20 times
                newchar.getStat().maxmp += 300;
                newchar.getStat().maxhp += 800; //Levelup 20 times
                newchar.getStat().maxmp += 400;
                break;
            default:
                return;
        }

        final Map<Skill, SkillEntry> ss = new HashMap<>();
        ss.put(SkillFactory.getSkill(1074 + (job / 100)), new SkillEntry((byte) 5, (byte) 5, -1));
        ss.put(SkillFactory.getSkill(80), new SkillEntry((byte) 1, (byte) 1, -1));
        newchar.changeSkillLevel_Skip(ss, false);
        final MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();

        //TODO: Make this GMS - Like
        int[] items = new int[]{1142257, hat, top, shoes, glove, weapon, hat + 1, top + 1, shoes + 1, glove + 1, weapon + 1}; //brilliant = fine+1
        for (byte i = 0; i < items.length; i++) {
            Item item = li.getEquipById(items[i]);
            item.setPosition((byte) (i + 1));
            newchar.getInventory(MapleInventoryType.EQUIP).addFromDB(item);
        }

        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000004, (byte) 0, (short) 200, (byte) 0));
        if (MapleCharacterUtil.canCreateChar(name, c.isGm()) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm())) {
            MapleCharacter.saveNewCharToDB(newchar, jobType, (short) 0);
            MapleQuest.getInstance(20734).forceComplete(c.getPlayer(), 1101000);
            c.getSession().write(CField.createUltimate(0));
        } else if (!LoginInformationProvider.getInstance().isForbiddenName(name) || c.isGm()) {
            c.getSession().write(CField.createUltimate(3)); //"You cannot use this name."
        } else {
            c.getSession().write(CField.createUltimate(1));
        }
    }

    public static void DeleteChar(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        String Secondpw_Client = GameConstants.GMS ? slea.readMapleAsciiString() : null;
        if (Secondpw_Client == null) {
            if (slea.readByte() > 0) { // Specific if user have second password or not
                Secondpw_Client = slea.readMapleAsciiString();
            }
            slea.readMapleAsciiString();
        }

        final int Character_ID = slea.readInt();

        if (!c.login_Auth(Character_ID) || !c.isLoggedIn() || loginFailCount(c)) {
            c.getSession().close();
            return; // Attempting to delete other character
        }
        byte state = 0;

        if (c.getSecondPassword() != null) { // On the server, there's a second password
            if (Secondpw_Client == null) { // Client's hacking
                c.getSession().close();
                return;
            } else if (!c.CheckSecondPassword(Secondpw_Client)) { // Wrong Password
                state = 20;
            }
        }

        if (state == 0) {
            state = (byte) c.deleteCharacter(Character_ID);
        }
        c.getSession().write(LoginPacket.deleteCharResponse(Character_ID, state));
    }

    public static void Character_WithoutSecondPassword(final LittleEndianAccessor slea, final MapleClient c, final boolean haspic, final boolean view) {

        if (!LoginServer.CanLoginKey(c.getLoginKey(), c.getAccID())) {
            FileoutputUtil.logToFile("logs/Data/客户端登入key异常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().getRemoteAddress().toString().split(":")[0] + " 账号: " + c.getAccountName() + " 客户端key：" + LoginServer.getLoginKey(c.getAccID()) + " 服务端key：" + c.getLoginKey() + " 角色列表");
            return;
        }
        final int charId = slea.readInt();

        if (!c.isLoggedIn() || loginFailCount(c) || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
            c.getSession().close();
            return;
        }

        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        final String s = c.getSessionIPAddress();
        LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
        int port = Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]);
        String ipAddr = ChannelServer.getInstance(c.getChannel()).getIP().split(":")[0];
        c.getSession().write(CField.getServerIP(c, ipAddr, port, charId));
        
    }

    public static void Character_WithSecondPassword(final LittleEndianAccessor slea, final MapleClient c, final boolean view) {
        final String password = slea.readMapleAsciiString();
        final int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readInt());
        }
        if (!c.isLoggedIn() || loginFailCount(c) || c.getSecondPassword() == null || !c.login_Auth(charId) || ChannelServer.getInstance(c.getChannel()) == null || !WorldOption.isExists(c.getWorld())) {
            c.getSession().close();
            return;
        }
        c.updateMacs(slea.readMapleAsciiString());

        if (c.CheckSecondPassword(password) && password.length() >= 6 && password.length() <= 16 || c.isGm()) {

            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }

            final String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP(), c.getChannel());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            String ipAddr = ChannelServer.getInstance(c.getChannel()).getIP().split(":")[0];
            c.getSession().write(CField.getServerIP(c, ipAddr, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
        } else {
            c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
        }
    }

    public static void partTimeJob(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        System.out.println("[Part Time Job] data: " + slea);
        byte mode = slea.readByte(); //1 = start 2 = end
        int cid = slea.readInt(); //character id
        byte job = slea.readByte(); //part time job
        if (mode == 0) {
            LoginPacket.partTimeJob(cid, (byte) 0, System.currentTimeMillis());
        } else if (mode == 1) {
            LoginPacket.partTimeJob(cid, job, System.currentTimeMillis());
        }
    }

    public static void PartJob(LittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer() != null || !c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        final byte mode = slea.readByte();
        final int cid = slea.readInt();
        if (mode == 1) {
            final PartTimeJob partTime = MapleCharacter.getPartTime(cid);
            final byte job = slea.readByte();
            if (/*chr.getLevel() < 30 || */job < 1 || job > 5 || partTime.getReward() > 0 || (partTime.getJob() > 0 && partTime.getJob() <= 5)) {
                c.getSession().close();
                return;
            }
            partTime.setTime(System.currentTimeMillis());
            partTime.setJob(job);
            c.getSession().write(LoginPacket.updatePartTimeJob(partTime));
            MapleCharacter.removePartTime(cid);
            MapleCharacter.addPartTime(partTime);
        } else if (mode == 2) {
            final PartTimeJob partTime = MapleCharacter.getPartTime(cid);
            if (/*chr.getLevel() < 30 || */partTime.getReward() > 0
                    || partTime.getJob() < 1 || partTime.getJob() > 5) {
                c.getSession().close();
                return;
            }
            final long distance = (System.currentTimeMillis() - partTime.getTime()) / (60 * 60 * 1000L);
            if (distance > 1) {
                partTime.setReward((int) (((partTime.getJob() + 1) * 1000L) + distance));
            } else {
                partTime.setJob((byte) 0);
                partTime.setReward(0);
            }
            partTime.setTime(System.currentTimeMillis());
            MapleCharacter.removePartTime(cid);
            MapleCharacter.addPartTime(partTime);
            c.getSession().write(LoginPacket.updatePartTimeJob(partTime));
        }
    }

    private CharLoginHandler() {
    }
}
