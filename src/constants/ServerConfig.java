package constants;

import server.ServerProperties;
import server.maps.GWTJ;

public class ServerConfig {

    public static String 
            SQL_IP = "127.0.0.1",
            SQL_PORT = "3306",
            SQL_DATABASE = "v079",
            SQL_USER = "zeroms",
            SQL_PASSWORD = "sa!@#456"; //8df97d5582af803
    public static boolean Danji_Guanggao = false;//单机广告
    public static boolean autoLieDetector = false;
    public static boolean adminOnly = false;
    public static boolean logPackets = false;//OK
    public static int expRate = 1, mesoRate = 1, dropRate = 1, bossRate = 1, petRate = 1;
    public static int Weakengold = 1;//削弱金币
    public static int expRate69 = 1, expRate89 = 1, expRate109 = 1, expRate129 = 1, expRate139 = 1, expRate149 = 1, expRate159 = 1, expRate169 = 1, expRate179 = 1, expRate189 = 1, expRate200 = 1, expRate250 = 1;
    public static int personalPvpMap = 999999999, partyPvpMap = 999999999, guildPvpMap = 999999999;
    public static short createMobInterval = 1000;//地图怪物重生时间毫秒级 1000=1秒
    public static int maxlevel = 250;
    public static int kocmaxlevel = 200;
    public static final int flags = 3;
    public static String serverName = "冒险岛";
    public static String eventMessage = "Welcome to CMS_v079.1";
    public static String version = "079 补丁版本:5 更新日期:2022.1.31【典藏版】";
    public static int flag = 3;
    public static int monsterSpawn =  2;//几倍怪物
    public static int maxCharacters = 15;
    public static String ServerMessage = "Welcome to CMS_v079.1";
    public static int userLimit = 1500;
    public static String IP = "127.0.0.1";
    public static String wzpath = "wz";
    public static boolean Openmultiplayermap = true;/*开启多倍地图*/
    public static boolean adventurer = true;//冒险家
    public static boolean GodofWar = true;//战神
    public static boolean Knights = true;//骑士团
    public static boolean 初始化 = false;
    public static String Custommultiplayermap = "104040000|104040001|100020000|240040510|104010001|100040001|100040002|100040003|100040004|105050000|105050100|105070001|200010000|200020000|200040000|701010000|261020500|300010100|300010200|300020100|300020200|103010001|541010000|541010010|551030100|550000100|550000200|100000003";
    public static double Marriageexperience = 0.5,Employmentexperience = 0.5;//结婚经验 雇佣经验
    public static String key = "caonima";
    public static int Stackquantity = 3000;//最大叠加数量
    public static int channelCount = 2;
    public static boolean Use_Localhost = false;//单机模式
    public static boolean logs_chat = true;//聊天日志
    public static boolean logs_mrechant = true;//雇佣商人日志
    public static boolean logs_trade = true;//玩家交易日志
    public static boolean logs_storage = true;//仓库日志
    public static boolean logs_csbuy = true;//商城购买日志
    public static boolean logs_npcshop_buy = true;//商店日志
    public static boolean logs_DAMAGE = false;//伤害日志
    public static boolean logs_PACKETS = false;//封包日志
    public static boolean AUTO_REGISTER = true;//自动注册
    public static int CashPort = 8600;
    public static int LoginPort = 8484;
    public static int ChannelPort = 7575;
    public static int RSGS = 0;//人物灌水百分比
    public static boolean banallskill = false;//禁用buff
    public static boolean bangainexp = false;//禁止获得经验
    public static boolean bandropitem = false;//禁止爆物
    public static boolean enablepointsbuy = true;//是否开启商城道具可用抵用券购买
    public static boolean Forgingsystem = true;//锻造系统
    public static boolean CollegeSystem = true;//学院系统
    public static boolean Petsarenothungry = false;//宠物不饿
    public static boolean wirelessbuff = false;//开启无限BUFF
    public static int statLimit = 32767; //最大属性
    public static byte defaultInventorySlot = 48; //默认角色栏位
    
    
    public static boolean 捉鬼任务启动 = false;//#此处时间乃是重启端之后多久召唤第一个npc
    public static String 捉鬼任务初始召唤时间 = "1";//此处时间乃是多久召唤一次新的npc
    public static String 捉鬼任务刷新时间 = "100";//此处时间乃是多久召唤一次新的npc
    public static String 捉鬼任务NPC = "9900001";//捉鬼任务NPC
    
    public static String 捉鬼任务所在地图 = "261040000";//捉鬼任务NPC
    public static boolean 捉鬼任务全图掉血开关 = true;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务全图掉蓝开关 = true;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务减少血量开关 = true;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务减少蓝量开关 = true;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务全图封锁开关 = true;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务全图黑暗开关 = true;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务全图虚弱开关 = true;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务全图诅咒开关 = true;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务全图诱导开关 = false;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务全图死亡开关 = false;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 捉鬼任务全图驱散开关 = false;//#此处时间乃是重启端之后多久召唤第一个npc
    public static boolean 互相残杀开关 = false;//#此处时间乃是重启端之后多久召唤第一个npc
    
    public static boolean 捉鬼任务直接死亡开关= false;
    public static boolean 捉鬼任务直接驱散开关= false;
    
    public static int 捉鬼任务伤害间隔时间 = 8;//捉鬼任务NPC
    public static int 捉鬼任务技能间隔时间 = 40;//捉鬼任务NPC
    public static int 捉鬼任务战士职业伤害回避率 = 5;//捉鬼任务NPC
    public static int 捉鬼任务法师职业伤害回避率 = 5;//捉鬼任务NPC
    public static int 捉鬼任务射手职业伤害回避率 = 5;//捉鬼任务NPC
    public static int 捉鬼任务飞侠职业伤害回避率 = 7;//捉鬼任务NPC
    public static int 捉鬼任务海盗职业伤害回避率 = 5;//捉鬼任务NPC
    public static int 捉鬼任务战士职业技能回避率 = 5;//捉鬼任务NPC
    public static int 捉鬼任务法师职业技能回避率 = 5;//捉鬼任务NPC
    public static int 捉鬼任务射手职业技能回避率 = 5;//捉鬼任务NPC
    public static int 捉鬼任务飞侠职业技能回避率 = 7;//捉鬼任务NPC
    public static int 捉鬼任务海盗职业技能回避率 = 5;//捉鬼任务NPC
    public static int 捉鬼任务伤害最大扣血数值 = 10000;//捉鬼任务NPC
    public static int 捉鬼任务伤害最大扣蓝数值 = 22000;//捉鬼任务NPC
    public static int 捉鬼任务伤害最小扣血数值 = 3000;//捉鬼任务NPC
    public static int 捉鬼任务伤害最小扣蓝数值 = 12000;//捉鬼任务NPC
    
    public static int petsuction = 4001239; //全屏宠吸现需要的道具
    public static boolean Droptheitem = false;//物落脚下开关
    public static boolean Fullscreenpetsuction = false;//全屏宠吸开关
    public static boolean Fullscreengoldcoin = false;//全屏吸金币开关
    public static boolean Fullscreensuction = false;//全屏吸物品开关
    
    
    public static int Doubleexplosionchannel = 5220002; //双爆频道道具
    public static boolean Doubleburstchannel = true;//双爆频道开关
    public static int Channelnumber = 2;//双爆频道
    
    public static boolean Strangeexplosionresistance = true;//打怪爆抵用开关
    public static boolean Strangeexplosivepointvolume = true;//打怪爆点卷开关
    public static int Monsterdroproll = 2; //怪物掉落点卷数量
    public static int Monsterdropoffset = 3; //怪物掉落抵用卷数量
    public static boolean marketUseBoard = true;//市场说话黑板
    private static String MapLoginType = "MapLogin";
    private static String loginWorldSelect = "MapLogin1";
    public static int bankHandlingFee = 10000; //金币存取手续费
    public static int takeOutHandlingFee = 10000; //寄存取物服务费
    public static int BEGINNER_SPAWN_MAP = 0;//新手出生地图
    public static boolean Freelydisableskills = true;//废弃
    public static int CreateGuildCost = 10000000;//创建家族费用
    public static int createEmblemMoney = 500000;//家族徽章费用
    public static int Fishinggoldcoinsseveral = 1;//钓鱼金币几倍
    public static int Fishingexperienceratio = 1;//钓鱼经验几倍
    public static int pdexp = 10;//获得经验等级X倍率
    public static int dyjy = 0;//默认经验
    public static int dyjb = 0;//默认金币
    public static boolean pdkg = true;//泡点开关
    public static int pdmap = 910000000;//泡点地图
    public static int pdzsjb = 1;//获得金币最少量
    public static int pdzdjb = 2;//获得金币最大量
    public static int pdzsdy = 1;//获得抵用券最少量
    public static int pdzddy = 2;//获得抵用券最大量
    public static int pdzsdj = 1;//获得点券最少量
    public static int pdzddj = 2;//获得点券最大量
    public static int pdzsdd = 1;//获得豆豆最少量
    public static int pdzddd = 2;//获得豆豆最大量
    
    public static boolean Upgradeconsultation = true;//升级公告
    public static boolean Injurytips = true;//受伤提示
    public static boolean debugMode = false;//封包提示
    public static boolean Playerchat = true;//玩家聊天
    public static boolean Throwoutgoldcoins = true;//丢出金币
    public static boolean Throwoutitems = true;//丢出物品
    public static boolean Gameinstructions = true;//游戏指令
    public static boolean Gamehorn = true;//游戏喇叭开关
    public static boolean Managestealth = true;//管理隐身
    public static boolean Managementacceleration = true;//管理加速
    public static boolean Playertransaction = true;//玩家交易
    public static boolean Hiredbusinessman = true;//雇佣商人开关
    public static boolean Loginhelp = true;//游戏帮助
    public static boolean Monsterstate = true;//怪物状态
    public static boolean Mapname = true;//显示地图名字
    public static boolean Onlineannouncement = true;//上线公告
    public static boolean Overdrawingarchiving = true;//过图存档
    public static boolean Auctionswitch = true;//拍卖行开关
    public static int Automaticcleaning = 300;//清理地板 满300个自动清理
    
    public static boolean Offlinehangup = true;//离线挂机
    public static int offlinemap = 910000000;//离线地图
    public static int Offlinecouponcounting = 1;//离线泡点点券
    public static int Offlineoffset = 1;//离线泡点抵用
    public static int OfflineDoudou = 1;//离线泡点豆豆
    public static int Offlinegoldcoins = 1;//离线泡点金币
    public static int Offlineexperience = 20;//离线泡点经验
   
    public static boolean applAttackRange = true;//技能范围检测
    public static boolean applAttackNumber = true;//技能段数检测
    public static boolean applAttackmobcount = true;//攻击数量检测
    public static boolean applAttackMP = true;//蓝耗检测
    
    public static String Servermaintenanceprompt = "游戏正在维护中.";//服务器维护提示语
    public static String Turnoffserverprompt = "游戏服务器将关闭维护，请玩家安全下线...";//关闭服务器提示语
    public static String Accountnumberprohibitionprompt = "你的账号已经被封禁.";//登陆时账号封禁提示语
  //ServerConfig
    
    public static String events = ""/* + "AutomatedEvent,"*/ + "PinkZakumEntrance,PVP,CygnusBattle,ScarTarBattle,BossBalrog_EASY,BossBalrog_NORMAL,HorntailBattle,Nibergen,PinkBeanBattle,ZakumBattle,NamelessMagicMonster,Dunas,Dunas2,2095_tokyo,ZakumPQ,LudiPQ,KerningPQ,ProtectTylus,WitchTower_EASY,WitchTower_Med,WitchTower_Hard,Vergamot,ChaosHorntail,ChaosZakum,CoreBlaze,BossQuestEASY,BossQuestMed,BossQuestHARD,BossQuestHELL,BossQuestCHAOS,Ravana_EASY,Ravana_HARD,Ravana_MED,GuildQuest,Aufhaven,Dragonica,Rex,MonsterPark,KentaPQ,ArkariumBattle,AswanOffSeason,HillaBattle,The Dragon Shout,VonLeonBattle,Ghost,OrbisPQ,Romeo,Juliet,Pirate,Amoria,Ellin,CWKPQ,DollHouse,Kenta,Prison,Azwan,HenesysPQ,jett2ndjob,Boats,Trains,Flight,Geenie,aran3rd2,aran3rd,aran4th,ProtectPig";
    public static GWTJ 通缉怪物;
    /*Anti-Sniff*/
    public static boolean USE_FIXED_IV;
    public static final byte[] Static_LocalIV = new byte[]{71, 113, 26, 44};
    public static final byte[] Static_RemoteIV = new byte[]{70, 112, 25, 43};

    public static enum Events {

        EVENT1("PinkZakumEntrance"),
        EVENT2("PVP"),
        EVENT3("CygnusBattle"),
        EVENT4("ScarTarBattle"),
        EVENT5("BossBalrog_EASY"),
        EVENT6("BossBalrog_NORMAL"),
        EVENT7("HorntailBattle"),
        EVENT8("Nibergen"),
        EVENT9("PinkBeanBattle"),
        EVENT10("ZakumBattle"),
        EVENT11("NamelessMagicMonster"),
        EVENT12("Dunas"),
        EVENT13("Dunas2"),
        EVENT14("2095_tokyo"),
        EVENT15("ZakumPQ"),
        EVENT16("LudiPQ"),
        EVENT17("KerningPQ"),
        EVENT18("ProtectTylus"),
        EVENT19("WitchTower_EASY"),
        EVENT20("WitchTower_Med"),
        EVENT21("WitchTower_Hard"),
        EVENT22("Vergamot"),
        EVENT23("ChaosHorntail"),
        EVENT24("ChaosZakum"),
        EVENT25("CoreBlaze"),
        EVENT26("BossQuestEASY"),
        EVENT27("BossQuestMed"),
        EVENT28("BossQuestHARD"),
        EVENT29("BossQuestHELL"),
        EVENT30("Ravana_EASY"),
        EVENT31("Ravana_HARD"),
        EVENT32("Ravana_MED"),
        EVENT33("GuildQuest"),
        EVENT34("Aufhaven"),
        EVENT35("Dragonica"),
        EVENT36("Rex"),
        EVENT37("MonsterPark"),
        EVENT38("KentaPQ"),
        EVENT39("ArkariumBattle"),
        EVENT40("AswanOffSeason"),
        EVENT41("HillaBattle"),
        EVENT42("The Dragon Shout"),
        EVENT43("VonLeonBattle"),
        EVENT44("Ghost"),
        EVENT45("OrbisPQ"),
        EVENT46("Romeo"),
        EVENT47("Juliet"),
        EVENT48("Pirate"),
        EVENT49("Amoria"),
        EVENT50("Ellin"),
        EVENT51("CWKPQ"),
        EVENT52("DollHouse"),
        EVENT53("Kenta"),
        EVENT54("Prison"),
        EVENT55("Azwan");
        private final String name;

        Events(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static String getMapLoginType() {
        return MapLoginType;
    }
    public static String getLoginWorldSelect() {
        return loginWorldSelect;
    }
    public static void setMapLoginType(String s) {
        MapLoginType = s;
    }
    
    public static void setLoginWorldSelect(String s) {
        loginWorldSelect = s;
    }
    
    public static String[] getEvents() {
        String[] eventlist = new String[Events.values().length];
        int arrayLocation = 0;
        for (Events event : Events.values()) {
            eventlist[arrayLocation] += event.getName();
            arrayLocation++;
        }
        return eventlist;
    }

    public static String getEventList() {
        String eventlist = new String();
        for (Events event : Events.values()) {
            eventlist += event.getName();
            eventlist += ", ";
        }
        eventlist += "@";
        eventlist = eventlist.replaceAll(", @", "");
        return eventlist;
    }
    
    public static void loadSetting() {

            SQL_IP = ServerProperties.getProperty("SQL_IP", SQL_IP);////ok
            SQL_PORT = ServerProperties.getProperty("SQL_PORT", SQL_PORT);////ok
            SQL_DATABASE = ServerProperties.getProperty("SQL_DATABASE", SQL_DATABASE);////ok
            SQL_USER = ServerProperties.getProperty("SQL_USER", SQL_USER);////ok
            SQL_PASSWORD = ServerProperties.getProperty("SQL_PASSWORD", SQL_PASSWORD);////ok
            AUTO_REGISTER = ServerProperties.getProperty("AUTO_REGISTER", AUTO_REGISTER);//自动注册
            adventurer = ServerProperties.getProperty("adventurer", adventurer);//ok
            Knights = ServerProperties.getProperty("Knights", Knights);//ok
            GodofWar = ServerProperties.getProperty("GodofWar", GodofWar);//ok
            Marriageexperience = ServerProperties.getProperty("Marriageexperience", Marriageexperience); //ok
            Employmentexperience = ServerProperties.getProperty("Employmentexperience", Employmentexperience);// ok
            Stackquantity = ServerProperties.getProperty("Stackquantity", Stackquantity);// ok
            Use_Localhost = ServerProperties.getProperty("Localhost", Use_Localhost);//单机模式 OK
            logs_chat = ServerProperties.getProperty("logs_chat", logs_chat);//聊天日志
            logs_mrechant = ServerProperties.getProperty("logs_mrechant", logs_mrechant);//雇佣商人日志
            logs_trade = ServerProperties.getProperty("logs_trade", logs_trade);//玩家交易日志
            logs_storage = ServerProperties.getProperty("logs_storage", logs_storage);//仓库日志
            logs_csbuy = ServerProperties.getProperty("logs_csbuy", logs_csbuy);//商城购买日志
            logs_npcshop_buy = ServerProperties.getProperty("logs_npcshop_buy", logs_npcshop_buy);//商店日志
            logs_DAMAGE = ServerProperties.getProperty("logs_DAMAGE", logs_npcshop_buy);//伤害日志
            logs_PACKETS = ServerProperties.getProperty("logs_PACKETS", logs_PACKETS);//封包日志
            RSGS = ServerProperties.getProperty("RSGS", RSGS);//人物灌水百分比
            banallskill = ServerProperties.getProperty("banallskill", banallskill);//禁用buff
            bangainexp = ServerProperties.getProperty("bangainexp", bangainexp);//禁止获得经验
            bandropitem = ServerProperties.getProperty("bandropitem", bandropitem);//禁止爆物
            enablepointsbuy = ServerProperties.getProperty("enablepointsbuy", enablepointsbuy);//抵用券
            CollegeSystem = ServerProperties.getProperty("CollegeSystem", CollegeSystem);//学院系统
            Forgingsystem = ServerProperties.getProperty("Forgingsystem", Forgingsystem);//锻造系统
            Petsarenothungry = ServerProperties.getProperty("Petsarenothungry", Petsarenothungry);//宠物不饿
            wirelessbuff = ServerProperties.getProperty("wirelessbuff", wirelessbuff);//无限BUFF
            Openmultiplayermap = ServerProperties.getProperty("Openmultiplayermap", Openmultiplayermap);//ok
            Custommultiplayermap = ServerProperties.getProperty("Custommultiplayermap", Custommultiplayermap);//自定义地图
            monsterSpawn = ServerProperties.getProperty("monsterSpawn", monsterSpawn);//# 地图怪物倍数 //ok
            IP = ServerProperties.getProperty("IP", IP);//服务器IP //ok
            wzpath = ServerProperties.getProperty("wzpath", wzpath);//服务器IP //ok
            logPackets = ServerProperties.getProperty("logPackets", logPackets);//# 调试封包 ok
            adminOnly = ServerProperties.getProperty("adminOnly", adminOnly);//管理员模式 OK
            expRate = ServerProperties.getProperty("expRate", expRate);//经验倍率 OK
            dropRate = ServerProperties.getProperty("dropRate", dropRate);//爆率倍率 OK
            mesoRate = ServerProperties.getProperty("mesoRate", mesoRate);//金币倍率OK
            bossRate = ServerProperties.getProperty("bossRate", bossRate);
            petRate = ServerProperties.getProperty("petRate", petRate);
            Weakengold = ServerProperties.getProperty("Weakengold", Weakengold);//削弱金币
            maxlevel = ServerProperties.getProperty("maxlevel", maxlevel);//最大等级 ok
            kocmaxlevel = ServerProperties.getProperty("kocmaxlevel", kocmaxlevel);// 骑士团最大等级 ok
            expRate69 = ServerProperties.getProperty("expRate69", expRate69);//ok
            expRate89 = ServerProperties.getProperty("expRate89", expRate89);//ok
            expRate109 = ServerProperties.getProperty("expRate109", expRate109);//ok
            expRate129 = ServerProperties.getProperty("expRate129", expRate129);//ok
            expRate139 = ServerProperties.getProperty("expRate139", expRate139);//ok
            expRate149 = ServerProperties.getProperty("expRate149", expRate149);//ok
            expRate159 = ServerProperties.getProperty("expRate159", expRate159);//ok
            expRate169 = ServerProperties.getProperty("expRate169", expRate169);//ok
            expRate179 = ServerProperties.getProperty("expRate179", expRate179);//ok
            expRate189 = ServerProperties.getProperty("expRate189", expRate189);//ok
            expRate200 = ServerProperties.getProperty("expRate200", expRate200);//ok
            expRate250 = ServerProperties.getProperty("expRate250", expRate250);//ok
            personalPvpMap = ServerProperties.getProperty("personalPvpMap", personalPvpMap);//# 个人PVP地图 OK
            partyPvpMap = ServerProperties.getProperty("partyPvpMap", partyPvpMap);//# 团队PVP地图 OK
            guildPvpMap = ServerProperties.getProperty("guildPvpMap", guildPvpMap);//# 家族PVP地图 OK
            flag = ServerProperties.getProperty("flag", flag);//服务器标识  0: 无 1: 活动 2: 新 3: 火爆  OK
            events = ServerProperties.getProperty("events", events);//事件列表 ok
            serverName = ServerProperties.getProperty("serverName", serverName);//服务器名称 ok
            eventMessage = ServerProperties.getProperty("eventMessage", eventMessage);//频道公告 ok
            ServerMessage = ServerProperties.getProperty("ServerMessage", ServerMessage);//服务器公告  OK
            maxCharacters = ServerProperties.getProperty("maxCharacters", maxCharacters);//服务器初始角色数量 OK
            channelCount = ServerProperties.getProperty("channelCount", channelCount);//服务器频道数 OK
            CashPort = ServerProperties.getProperty("CashPort", CashPort);//服务器名称 ok
            LoginPort = ServerProperties.getProperty("LoginPort", LoginPort);//服务器名称 ok
            ChannelPort = ServerProperties.getProperty("ChannelPort", ChannelPort);//服务器名称 ok
            USE_FIXED_IV = ServerProperties.getProperty("antiSniff", USE_FIXED_IV);//封包加密 OK
            autoLieDetector = ServerProperties.getProperty("autoLieDetector", autoLieDetector);//自动测谎 OK
            createMobInterval = ServerProperties.getProperty("createMobInterval", createMobInterval);//地图怪物重生时间毫秒级 1000=1秒 OK
            statLimit = ServerProperties.getProperty("statLimit", statLimit);//最大属性
            defaultInventorySlot = ServerProperties.getProperty("defaultInventorySlot", defaultInventorySlot);//默认角色栏位
            petsuction = ServerProperties.getProperty("petsuction", petsuction); //全屏宠吸现需要的道具
            Droptheitem = ServerProperties.getProperty("Droptheitem", Droptheitem); //物落脚下开关
            Fullscreenpetsuction = ServerProperties.getProperty("Fullscreenpetsuction", Fullscreenpetsuction);//全屏宠吸开关
            Fullscreengoldcoin = ServerProperties.getProperty("Fullscreengoldcoin", Fullscreengoldcoin);//全屏吸金币开关
            Fullscreensuction = ServerProperties.getProperty("Fullscreensuction", Fullscreensuction);//全屏吸物品开关
            Doubleexplosionchannel = ServerProperties.getProperty("Doubleexplosionchannel", Doubleexplosionchannel); //双爆频道道具
            Doubleburstchannel = ServerProperties.getProperty("Doubleburstchannel", Doubleburstchannel);//双爆频道开关
            Channelnumber = ServerProperties.getProperty("Channelnumber", Channelnumber);//双爆频道
            Strangeexplosionresistance = ServerProperties.getProperty("Strangeexplosionresistance", Strangeexplosionresistance);//打怪爆抵用开关
            Strangeexplosivepointvolume = ServerProperties.getProperty("Strangeexplosivepointvolume", Strangeexplosivepointvolume);//打怪爆点卷开关
            Monsterdroproll = ServerProperties.getProperty("Monsterdroproll", Monsterdroproll); //怪物掉落点卷数量
            Monsterdropoffset = ServerProperties.getProperty("Monsterdropoffset", Monsterdropoffset); //怪物掉落抵用卷数量
            marketUseBoard = ServerProperties.getProperty("marketUseBoard", marketUseBoard);//自由黑板
            bankHandlingFee = ServerProperties.getProperty("bankHandlingFee", bankHandlingFee); //金币存取手续费
            takeOutHandlingFee = ServerProperties.getProperty("takeOutHandlingFee", takeOutHandlingFee); //寄存取物服务费
            CreateGuildCost = ServerProperties.getProperty("CreateGuildCost", CreateGuildCost);//创建家族费用
            createEmblemMoney = ServerProperties.getProperty("createEmblemMoney", createEmblemMoney);//家族徽章费用
            Fishinggoldcoinsseveral = ServerProperties.getProperty("Fishinggoldcoinsseveral", Fishinggoldcoinsseveral);//钓鱼金币几倍
            Fishingexperienceratio = ServerProperties.getProperty("Fishingexperienceratio", Fishingexperienceratio);//钓鱼经验几倍
            pdexp = ServerProperties.getProperty("pdexp", pdexp);//获得经验等级X倍率
            dyjy = ServerProperties.getProperty("dyjy", dyjy);//默认经验
            dyjb = ServerProperties.getProperty("dyjb", dyjb);//默认金币
            pdkg = ServerProperties.getProperty("pdkg", pdkg);//泡点开关
            pdmap = ServerProperties.getProperty("pdmap", pdmap);//泡点地图
            pdzsjb = ServerProperties.getProperty("pdzsjb", pdzsjb);//获得金币最少量
            pdzdjb = ServerProperties.getProperty("pdzdjb", pdzdjb);//获得金币最大量
            pdzsdy = ServerProperties.getProperty("pdzsdy", pdzsdy);//获得抵用券最少量
            pdzddy = ServerProperties.getProperty("pdzddy", pdzddy);//获得抵用券最大量
            pdzsdj = ServerProperties.getProperty("pdzsdj", pdzsdj);//获得点券最少量
            pdzddj = ServerProperties.getProperty("pdzddj", pdzddj);//获得点券最大量
            pdzsdd = ServerProperties.getProperty("pdzsdd", pdzsdd);//获得豆豆最少量
            pdzddd = ServerProperties.getProperty("pdzddd", pdzddd);//获得豆豆最大量
            Upgradeconsultation = ServerProperties.getProperty("Upgradeconsultation", Upgradeconsultation);//升级公告
            Injurytips = ServerProperties.getProperty("Injurytips", Injurytips);//受伤提示
            debugMode = ServerProperties.getProperty("debugMode", debugMode);//封包提示
            Playerchat = ServerProperties.getProperty("Playerchat", Playerchat);//玩家聊天
            Throwoutgoldcoins = ServerProperties.getProperty("Throwoutgoldcoins", Throwoutgoldcoins);//丢出金币
            Throwoutitems = ServerProperties.getProperty("Throwoutitems", Throwoutitems);//丢出物品
            Gameinstructions = ServerProperties.getProperty("Gameinstructions", Gameinstructions);//游戏指令
            Gamehorn = ServerProperties.getProperty("Gamehorn", Gamehorn);//游戏喇叭开关
            Managestealth = ServerProperties.getProperty("Managestealth", Managestealth);//管理隐身
            Managementacceleration = ServerProperties.getProperty("Managementacceleration", Managementacceleration);//管理加速
            Playertransaction = ServerProperties.getProperty("Playertransaction", Playertransaction);//玩家交易
            Hiredbusinessman = ServerProperties.getProperty("Hiredbusinessman", Hiredbusinessman);//雇佣商人开关
            Loginhelp = ServerProperties.getProperty("Loginhelp", Loginhelp);//游戏帮助
            Monsterstate = ServerProperties.getProperty("Monsterstate", Monsterstate);//怪物状态
            Mapname = ServerProperties.getProperty("Mapname", Mapname);//显示地图名字
            Onlineannouncement = ServerProperties.getProperty("Onlineannouncement", Onlineannouncement);//上线公告
            Overdrawingarchiving = ServerProperties.getProperty("Overdrawingarchiving", Overdrawingarchiving);//过图存档
            Auctionswitch = ServerProperties.getProperty("Auctionswitch", Auctionswitch);//拍卖行开关
            Automaticcleaning = ServerProperties.getProperty("Automaticcleaning", Automaticcleaning);//清理地板 满300个自动清理
            BEGINNER_SPAWN_MAP = ServerProperties.getProperty("BEGINNER_SPAWN_MAP", BEGINNER_SPAWN_MAP);//出生地图
            Offlinehangup = ServerProperties.getProperty("Offlinehangup", Offlinehangup);//离线挂机
            offlinemap = ServerProperties.getProperty("offlinemap", offlinemap);//离线地图
            Offlinecouponcounting = ServerProperties.getProperty("Offlinecouponcounting", Offlinecouponcounting);//离线泡点点券
            Offlineoffset = ServerProperties.getProperty("Offlineoffset", Offlineoffset);//离线泡点抵用
            OfflineDoudou = ServerProperties.getProperty("OfflineDoudou", OfflineDoudou);//离线泡点豆豆
            Offlinegoldcoins = ServerProperties.getProperty("Offlinegoldcoins", Offlinegoldcoins);//离线泡点金币
            Offlineexperience = ServerProperties.getProperty("Offlineexperience", Offlineexperience);//离线泡点经验
            applAttackRange = ServerProperties.getProperty("applAttackRange", applAttackRange);//技能范围检测
            applAttackNumber = ServerProperties.getProperty("applAttackNumber", applAttackNumber);//技能段数检测
            applAttackmobcount = ServerProperties.getProperty("applAttackmobcount", applAttackmobcount);//攻击数量检测
            applAttackMP = ServerProperties.getProperty("applAttackMP", applAttackMP);//蓝耗检测
            Servermaintenanceprompt = ServerProperties.getProperty("游戏正在维护中.", Servermaintenanceprompt);//服务器维护提示语
            Turnoffserverprompt = ServerProperties.getProperty("游戏服务器将关闭维护，请玩家安全下线...", Turnoffserverprompt);//关闭服务器提示语
            Accountnumberprohibitionprompt = ServerProperties.getProperty("你的账号已经被封禁.", Accountnumberprohibitionprompt);//登陆时账号封禁提示语
        
        }

    private ServerConfig() {
    }
         }
            

