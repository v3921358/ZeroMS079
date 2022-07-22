package server;

import client.MapleCharacter;
import client.ZeroMSEvent;
import client.SkillFactory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.OnlyID;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import constants.GameConstants;
import constants.ServerConfig;
import constants.ServerConstants;
import database.DBConPool;
import gui.ZeroMS_UI;
import tools.CustomPlayerRankings;
import handling.MapleServerHandler;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.MapleDojoRanking;
import handling.channel.MapleGuildRanking;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.World;
import handling.world.family.MapleFamily;
import handling.world.guild.MapleGuild;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import server.Timer.BuffTimer;
import server.Timer.CloneTimer;
import server.Timer.EtcTimer;
import server.Timer.EventTimer;
import server.Timer.MapTimer;
import server.Timer.PingTimer;
import server.Timer.WorldTimer;
import server.events.MapleOxQuizFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.maps.GWTJ;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.CWvsContext;
import server.拍卖.拍卖行_MYSQL;
import static tools.FileoutputUtil.CurrentReadable_Time;
import tools.ZeroMS;

public class Start {
    private static ServerSocket srvSocket = null; //服务线程，用以控制服务器只启动一个实例
    private static int srvPort = 6350;     //控制启动唯一实例的端口号，这个端口如果保存在配置文件中会更灵活
    public static long startTime = System.currentTimeMillis();
    public static final Start instance = new Start();
    public static AtomicInteger CompletedLoadingThreads = new AtomicInteger(0);

    public final static void 启动游戏(final String args[]) {
        checkSingleInstance();//多开检测
        long startTime = System.currentTimeMillis();
       /*  System.out.println("【正在读取授权码请稍后...】");
        if (!ZeroMS.getAuthorizedFeedBack()) {
        JOptionPane.showMessageDialog(null, "此服务器未授权，请联系作者授权！");
          System.exit(0);
        }*/
        System.out.println("【" + CurrentReadable_Time() + "】【信息】盛大游戏版本：" + ServerConstants.MAPLE_VERSION + "." + ServerConstants.MAPLE_PATCH);
        System.out.println("【" + CurrentReadable_Time() + "】【信息】世界服务器：" + ServerConfig.serverName);
        服务器信息();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】服务端配置文件");
        if (ServerConfig.adminOnly || ServerConfig.Use_Localhost) {
            System.out.println("【" + CurrentReadable_Time() + "】【信息】管理员模式: 开启");
        } else {
            System.out.println("【" + CurrentReadable_Time() + "】【信息】管理员模式: 关闭");
        }
        if (ServerConfig.AUTO_REGISTER) {
            System.out.println("【" + CurrentReadable_Time() + "】【信息】自动注册模式: 开启");
        } else {
            System.out.println("【" + CurrentReadable_Time() + "】【信息】自动注册模式: 开启");
        }
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = 0")) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("运行时错误: 无法连接到MySQL数据库服务器 - " + ex);
        }
        World.init();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】世界服务器线程");
        /* 加载计时器 */
        WorldTimer.getInstance().start();
        EtcTimer.getInstance().start();
        MapTimer.getInstance().start();
        CloneTimer.getInstance().start();
        EventTimer.getInstance().start();
        BuffTimer.getInstance().start();
        PingTimer.getInstance().start();
        ZeroMSEvent.start();//广告线程
        //GameConstants.LoadEXP();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏排行榜");
        MapleDojoRanking.getInstance().load();
        MapleGuildRanking.getInstance().load();
        MapleGuild.loadAll();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏家族系统");
        MapleFamily.loadAll();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏学院系统");
        MapleLifeFactory.loadQuestCounts();
        MapleQuest.initQuests();
        /* 加载道具信息 */
        MapleItemInformationProvider.getInstance().runEtc();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏学院系统1");
        MapleMonsterInformationProvider.getInstance().load();
        MapleItemInformationProvider.getInstance().runItems();
        /* 读取WZ內禁止使用的名称 */
        LoginInformationProvider.getInstance();
        /* 加载随机奖励 */
        RandomRewards.load();
        /* 读取钓鱼 */
        FishingRewardFactory.getInstance();
        /* 加载任务*/
        MapleOxQuizFactory.getInstance();
        /* 加载技能信息 */
        MapleCarnivalFactory.getInstance();
        SkillFactory.load();
        MobSkillFactory.getInstance();
        /* 处理怪物重生、CD、宠物、坐骑 */
        SpeedRunner.loadSpeedRuns();
        MapleInventoryIdentifier.getInstance();
        /* 加载自定义NPC、怪物*/
        MapleMapFactory.loadCustomLife();

        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM `moonlightachievements` where achievementid > 0;");
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        // 现金道具
        CashItemFactory.getInstance().initialize();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏已上架商店物品数量: " + 服务器游戏商品() + " 个");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏已上架商城商品数量: " + 服务器商城商品() + " 个");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏注册玩家账号数量: " + 服务器账号() + " 个");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏建立玩家角色数量: " + 服务器角色() + " 个");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏玩家拥有道具数量: " + 服务器道具() + " 个");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】游戏玩家拥有技能数量: " + 服务器技能() + " 个");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】自动存档线程");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】角色福利泡点线程");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】启动记录在线时长线程");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】启动服务端内存回收线程");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】启动服务端地图回收线程");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】处理怪物重生、CD、宠物、坐骑");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】玩家NPC");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】检测游戏复制道具系统");
        刷新通缉怪物(120);
        System.out.println("【" + CurrentReadable_Time() + "】【信息】初始化通缉怪物");
        加载等级上限();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】初始化等级上线");
        拍卖行_MYSQL.初始化();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】初始化游戏拍卖行");
        
        // 服务器协议响应回调 初始化
        MapleServerHandler.initiate();
        /* 加载登录服务器 */
        LoginServer.run_startup_configurations();
        /* 加载频道服务器*/
        ChannelServer.startChannel_Main();
        /* 加载商城服务器*/
        CashShopServer.run_startup_configurations();
        // 服务器shutdown回调。
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
        // 怪物重生。刷新每个地图的怪物。
        World.registerRespawn();
        // 关闭服务器的相关操作初始化。
        ShutdownServer.registerMBean();
       /* 加载玩家NPC */
        PlayerNPC.loadAll();
        MapleMonsterInformationProvider.getInstance().addExtra();
        LoginServer.setOn();
        RankingWorker.run();
        World.resetEveryDay(1);
        World.oneMinuteThread(1);
        World.fiveMinuteThread(5);
        World.offLineOnHook(1);
        回收内存(360);
        回收地图(480);
        自动存档(5);
        OnlyID.getInstance();//防复制道具
     //   System.out.println("Event Script List: " + ServerConfig.getEventList());
        if (ServerConfig.logPackets) {
            System.out.println("【" + CurrentReadable_Time() + "】【信息】数据包记录已开启.");
        }
        if (ServerConfig.USE_FIXED_IV) {
            System.out.println("【" + CurrentReadable_Time() + "】【信息】已启用封包加密模式.");
        }
        CustomPlayerRankings.getInstance().load();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】所有游戏数据加载完毕");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】服务端已启动完毕，耗时 " + ((System.currentTimeMillis() - startTime) / 1000) + " 秒");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】运行中请勿直接关闭本控制台，使用下方关闭服务器按钮来关闭服务端，否则回档自负.");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】本程序源码均来自互联网,仅作交流学习使用,不得用于任何商业用途");
        System.out.println("【" + CurrentReadable_Time() + "】【信息】请安装后在24小时内删除,如由此引起的相关法律法规责任,与作者无关");
    }
    protected static void checkSingleInstance() {
        try {
            srvSocket = new ServerSocket(srvPort); //启动一个ServerSocket，用以控制只启动一个实例
        } catch (IOException ex) {
            if (ex.getMessage().contains("在一台服务器主机上同时只能启动一个服务端进程.")) {
                System.err.println("在一台服务器主机上同时只能启动一个服务端进程.");
            }
            System.exit(0);
        }
    }
    
         public static void 自动存档(final int time) {
        WorldTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                int ppl = 0;
                try {
                    for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                        for (final MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            ++ppl;
                            chr.saveToDB(false, false);
                        }
                    }
                }
                catch (Exception e) {
                    System.err.println("自动存档出错：" + e);
                }
                System.out.println("【" + CurrentReadable_Time() + "】【信息】已经将 " + ppl + " 个玩家保存到数据中.");
            }
        }, 60000 * time);
    }
    
        public static void 服务器信息() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress().toString(); //獲取本機ip
            String hostName = addr.getHostName().toString(); //獲取本機計算機名稱
            System.out.println("【" + CurrentReadable_Time() + "】【信息】检测服务端运行环境");
            System.out.println("【" + CurrentReadable_Time() + "】【信息】服务器名: " + hostName);
            Properties 設定檔 = System.getProperties();
            System.out.println("【" + CurrentReadable_Time() + "】【信息】操作系统：" + 設定檔.getProperty("os.name"));
            System.out.println("【" + CurrentReadable_Time() + "】【信息】系统框架：" + 設定檔.getProperty("os.arch"));
            System.out.println("【" + CurrentReadable_Time() + "】【信息】系统版本：" + 設定檔.getProperty("os.version"));
            System.out.println("【" + CurrentReadable_Time() + "】【信息】服务端目录：" + 設定檔.getProperty("user.dir"));
            System.out.println("【" + CurrentReadable_Time() + "】【信息】服务端环境检测完成");
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
      
        public static int 服务器角色() {
        int p = 0;
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM characters WHERE id >=0");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    p += 1;
                }
            }
            ps.close();
        } catch (SQLException Ex) {
            System.err.println("服务器角色？");
        }
        return p;
    }

    public static int 服务器账号() {
        int p = 0;
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM accounts WHERE id >=0");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    p += 1;
                }
            }
            ps.close();
        } catch (SQLException Ex) {
            System.err.println("服务器账号？");
        }
        return p;
    }

    public static int 服务器技能() {
        int p = 0;
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM skills ");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    p += 1;
                }
            }
            ps.close();
        } catch (SQLException Ex) {
            System.err.println("服务器技能？");
        }
        return p;
    }

    public static int 服务器道具() {
        int p = 0;
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT inventoryitemid as DATA FROM inventoryitems WHERE inventoryitemid >=0");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    p += 1;
                }
            }
            ps.close();
        } catch (SQLException Ex) {
            System.err.println("服务器道具？");
        }
        return p;
    }

    public static int 服务器商城商品() {
        int p = 0;
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT serial as DATA FROM cashshop_modified_items WHERE serial >=0");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    p += 1;
                }
            }
            ps.close();
        } catch (SQLException Ex) {
            System.err.println("服务器商城商品？");
        }
        return p;
    }  
   
        public static int 服务器游戏商品() {
        int p = 0;
        try {
            Connection con = DBConPool.getInstance().getDataSource().getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT shopitemid as DATA FROM shopitems WHERE shopitemid >=0");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    p += 1;
                }
            }
            ps.close();
        } catch (SQLException Ex) {
            System.err.println("服务器道具游戏商品？");
        }
        return p;
    }
    
    public static String returnSerialNumber() {
        String cpu = getCPUSerial();
        String disk = getHardDiskSerialNumber("C");

        int newdisk = Integer.parseInt(disk);

        String s = cpu + newdisk;
        String newStr = s.substring(8, s.length());
        return newStr;
    }
    
    public static String getCPUSerial() {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\nSet colItems = objWMIService.ExecQuery _ \n   (\"Select * from Win32_Processor\") \nFor Each objItem in colItems \n    Wscript.Echo objItem.ProcessorId \n    exit for  ' do the first cpu only! \nNext \n";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = input.readLine()) != null) {
                result = result + line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        if ((result.trim().length() < 1) || (result == null)) {
            result = "无机器码被读取";
        }
        return result.trim();
    }
    
    public static String getHardDiskSerialNumber(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\nSet colDrives = objFSO.Drives\nSet objDrive = colDrives.item(\"" + drive + "\")\n" + "Wscript.Echo objDrive.SerialNumber";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result = result + line;
            }
            input.close();
        } catch (Exception e) {
        }
        return result.trim();
    }
    
    public static class Shutdown implements Runnable {

        @Override
        public void run() {
            ShutdownServer.getInstance().run();
            ShutdownServer.getInstance().run();
        }
    }

    public static void main(final String args[]) throws InterruptedException, IOException {
     //   instance.run();
/*try {
    //FlatLightLaf.setup();
    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");//系统默认
    ZeroMS_UI.main(args);
} catch( Exception ex ) {
    System.err.println( "Failed to initialize LaF" );
}*/
    }

        /**
     * * <30分钟强制回收一次内存>
     */
    private static int 回收内存 = 0;

    public static void 回收内存(final int time) {
        Timer.WorldTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (回收内存 > 0) {
                    System.gc();
                    System.err.println("【" + CurrentReadable_Time() + "】【内存回收】回收服务端内存 √");
                } else {
                    回收内存++;
                }
            }
        }, 60 * 1000 * time);
       }
     /**
     * * <30分钟强制回收一次地图>
     */
    
        public static void 回收地图(int time) {
        Timer.WorldTimer.getInstance().register(new Runnable() {

            public void run() {
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                        for (int i = 0; i < 6; i++) {
                            int mapidA = 100000000 + (i + 1000000 - 2000000);
                            MapleCharacter player = chr;
                            if (i == 6) {
                                mapidA = 910000000;
                            }
                            int mapid = mapidA;
                            MapleMap map = player.getClient().getChannelServer().getMapFactory().getMap(mapid);
                            if (player.getClient().getChannelServer().getMapFactory().destroyMap(mapid)) {
                                MapleMap newMap = player.getClient().getChannelServer().getMapFactory().getMap(mapid);
                                MaplePortal newPor = newMap.getPortal(0);
                                LinkedHashSet<MapleCharacter> mcs = new LinkedHashSet<MapleCharacter>(map.getCharacters()); // do NOT remove, fixing ConcurrentModificationEx.
                                outerLoop:
                                for (MapleCharacter m : mcs) {
                                    for (int x = 0; x < 5; x++) {
                                        try {
                                            m.changeMap(newMap, newPor);
                                            continue outerLoop;
                                        } catch (Throwable t) {
                                        System.err.println("【" + CurrentReadable_Time() + "】【地图回收】系统正在回收地图 √");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, 60000 * time);
    }
    
    public static void 加载等级上限() {
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 等级上限");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                GameConstants.等级上限 = rs.getInt("等级");
                GameConstants.提交数量 = rs.getInt("提交数量");
            }
            ps.close();
            con.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }
    
    public static void 刷新通缉怪物(int min) {
        final List<Pair<Integer, String>> itemIds = new ArrayList<>(); // empty list.
        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM 通缉怪物");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemIds.add(new Pair<>(rs.getInt("怪物ID"), rs.getString("地图名")));
            }
            ps.close();
            con.close();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
        if(itemIds.size() > 0){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            
            ServerConfig.通缉怪物 = new GWTJ(df.format(new Date()),itemIds.get(0).left,itemIds.get(0).right,"",false);
            Timer.EventTimer.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    int 随机 = (int)(Math.random() * itemIds.size());
                    int 怪物ID = itemIds.get(随机).left;
                    ServerConfig.通缉怪物 =  new GWTJ(df.format(new Date()),怪物ID,itemIds.get(随机).right,"",false);
                    for (ChannelServer cs : ChannelServer.getAllInstances()) {
                        cs.broadcastPacket(CWvsContext.broadcastMsg(6, " 刷新通缉怪物!!!請在通緝NPC中查詢被通緝怪物.通缉怪物所在地图(" + ServerConfig.通缉怪物.怪物所在地图 + ")"));
                    }
                }
            }, min * 60 * 1000, min * 60 * 1000);
            
        } else {
            System.out.println("【" + CurrentReadable_Time() + "】【信息】通缉怪物数据为空");
        }
    }
}
