package gui.进阶BOSS;

import constants.ServerConfig;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.World;
import java.awt.Point;
import server.ServerProperties;
import server.maps.MapleMap;
import static tools.FileoutputUtil.CurrentReadable_Time;
import tools.packet.CWvsContext;
//import tools.MaplePacketCreator;
//import MXDJR.RobotSocket;
public class 活动捉鬼任务 {

    /**
     * <捉鬼任务代码>
     */
    //捉鬼任务代码
    public static int 捉鬼任务 = Integer.parseInt(ServerConfig.捉鬼任务NPC);
    //商人出现后存在的时间/分
    public static int 刷新时间 = Integer.parseInt(ServerConfig.捉鬼任务刷新时间);

    /**
     * <启动服务端的时候启动线程>
     */
    public static void 启动捉鬼任务() {
        随机商人出现条件();
       // System.out.println("[服务端]" + CurrentReadable_Time() + " : 1" );
    }

    /**
     * <随机出捉鬼任务出现的时间，地图，坐标>
     */
    public static void 随机商人出现条件() {
        //随机频道
        int pind = (int) Math.ceil(Math.random() * Integer.parseInt(ServerProperties.getProperty("channelCount")));
        //如果随机的频道为0，则加1
        if (pind == 0) {
            pind += 1;
        }
        //随机时间
       /* int huor = (int) Math.ceil(Math.random() * 23);
        if (MapleParty.捉鬼任务时间 == huor) {
            if (huor == 23) {
                huor -= 1;
            } else {
                huor += 1;
            }
        }*/
       
        //随机地图坐标
        int[][] 坐标 = {
            {106010000, 488, 215},
            {240010000, 905, -298},
            {240010100, 2098, -508},
            {240010200, 2920, -688},
            {240010200, 409, -688},
            {110000000, -64, 151},
            {110000000, 379, -143},
            {110010000, -1594, -113},
            {110010000, 1204, -473},
            {104040000, 1059, -687},
            {104040000, 48, -685},
            {104020000, 1418, -1345},
            {104010000, 2329, -115},
            {104010002, -1401, -25},
            {100000002, -16, -475},
            {100000002, 214, -475},
            {100030000, -3652, -205},
            {100040000, 349, 1752},
            {105050000, 2282, 1619},
            {105090301, 928, -923},
            {105040305, 1245, 2295},
            {100030000, -2977, -1465},
            {100000006, -705, 215},
            {600000000, 5682, -632},
            {200070000, -132, -715},
            {222010201, 120, -1047},
            {100040104, 66, 812},
            {260010400, 199, -85},
            {103010000, -1088, 232},
            {230010201, -50, -17},
            {240020100, -889, -508},
            {221020000, 11, 2162},
            {220070201, 811, 1695},
            {541010040, 1075, -1695},
            {261000001, -47, 64}
        };
         //随机坐标组
        int rand = (int) Math.floor(Math.random() * 坐标.length);
        //给予随机的值
        //MapleParty.捉鬼任务时间 = huor;
        MapleParty.捉鬼任务频道 = pind;
        MapleParty.捉鬼任务地图 = 坐标[rand][0];
        MapleParty.捉鬼任务坐标X = 坐标[rand][1];
        MapleParty.捉鬼任务坐标Y = 坐标[rand][2];
        //召唤的频道
        ChannelServer channelServer = ChannelServer.getInstance(MapleParty.捉鬼任务频道);
        //召唤的地图
        MapleMap mapleMap = channelServer.getMapFactory().getMap(MapleParty.捉鬼任务地图);
        //通知信息
        //String 信息 = "[捉鬼任务] : 一个神秘的商人 " + MapleParty.捉鬼任务时间 + " 时将出现在 " + MapleParty.捉鬼任务频道 + " 频道的某个地方。";
       // World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 信息));
        //System.err.println("[服务端]" + CurrentReadable_Time() + " : " + 信息);
       // System.err.println("[服务端]" + CurrentReadable_Time() + " : 出现在地图: " + 坐标[rand][0] + "( " + mapleMap.getMapName() + " ) 坐标: " + 坐标[rand][1] + "/" + 坐标[rand][2]);
        召唤捉鬼任务();
        
    }

    /**
     * <满足条件后召唤捉鬼任务出现>
     */
    public static void 召唤捉鬼任务() {
        //召唤的频道
        ChannelServer channelServer = ChannelServer.getInstance(MapleParty.捉鬼任务频道);
        //召唤的地图
        MapleMap mapleMap = channelServer.getMapFactory().getMap(MapleParty.捉鬼任务地图);
        //召唤商人
        if(mapleMap!=null){
        mapleMap.spawnNpc(捉鬼任务, new Point(MapleParty.捉鬼任务坐标X, MapleParty.捉鬼任务坐标Y));
        }else{
        mapleMap =channelServer.getMapFactory().getMap(106010000);  
        mapleMap.spawnNpc(捉鬼任务, new Point(488, 215));
        }
        //延时执行
        String 信息 = "[捉鬼任务] : 一个神秘的鬼差出现在 " + MapleParty.捉鬼任务频道 + " 频道" + mapleMap.getMapName() + "。";
        World.Broadcast.broadcastMessage(CWvsContext.broadcastMsg(6, 信息));
        //RobotSocket.robbatSpeak(信息);
       // RobotSocket.robbatSpeak(信息);
       // RobotSocket.robbatSpeak(信息);
        System.out.println("[服务端]" + CurrentReadable_Time() + " : " + 信息);
        String 信息1 = "[捉鬼任务] : 下一次出现的时间约为"+刷新时间+"分钟之后";
        //RobotSocket.robbatSpeak(信息1);
        World.Broadcast.broadcastMessage(CWvsContext.broadcastMsg(6, 信息1));
        System.out.println("[服务端]" + CurrentReadable_Time() + " : " + 信息1);
        捉鬼任务线程.开启进阶BOSS线程();
        new Thread() {
            @Override
            public void run() {
                try {
                    //设置到时后清理商人
                    Thread.sleep(1000 * 60 * 刷新时间);
                    if(ChannelServer.getInstance(MapleParty.捉鬼任务频道)!=null){
                    删除捉鬼任务();
                    }
                    启动捉鬼任务();
                } catch (InterruptedException e) {
                }
            }
        }.start();
        
        
        
        
        
        
        
    }

    /**
     * <删除捉鬼任务并且重置线程，并且随机下一次出现的时间地图和坐标>
     */
    public static void 删除捉鬼任务() {
        //召唤的频道
        ChannelServer channelServer = ChannelServer.getInstance(MapleParty.捉鬼任务频道);
        //召唤的地图
        MapleMap mapleMap = channelServer.getMapFactory().getMap(MapleParty.捉鬼任务地图)==null?channelServer.getMapFactory().getMap(10000):channelServer.getMapFactory().getMap(MapleParty.捉鬼任务地图);
        //删除商人
        mapleMap.removeNpc(捉鬼任务);
        //重置商人线程
        MapleParty.捉鬼任务 = 0;
        //重新随机商人数据
        //随机商人出现条件();
        
    }

    private 活动捉鬼任务() {
    }
}
