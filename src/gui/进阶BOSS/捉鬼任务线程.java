package gui.进阶BOSS;

import client.MapleCharacter;
import client.MapleDisease;
import client.MapleStat;
import constants.ServerConfig;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import java.awt.Point;
import java.util.concurrent.ScheduledFuture;
import server.MapleItemInformationProvider;
import server.ServerProperties;
import server.Timer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;

public class 捉鬼任务线程 {

    /**
     浪子杰 私人订制捉鬼任务线程
     */
    public static ScheduledFuture<?> 进阶BOSS线程 = null;
    public static ScheduledFuture<?> 进阶BOSS线程伤害 = null;
    public static ScheduledFuture<?> 全图掉HP = null;
    public static ScheduledFuture<?> 全图掉MP = null;
    public static ScheduledFuture<?> 全图封锁 = null;
    public static int 强化BOSS = 9500337;
    public static int 飞鱼 = 2230107;
    public static int 地图 = Integer.parseInt(ServerConfig.捉鬼任务所在地图);
    //public static int 频道 = Integer.parseInt(ServerProperties.getProperty("Lzj.捉鬼任务所在频道"));
    public static Point 坐标 = new Point(232, 185);
    
    private static boolean 全图掉血开关 = ServerConfig.捉鬼任务全图掉血开关;
    private static boolean 全图掉蓝开关 =ServerConfig.捉鬼任务全图掉蓝开关;
    private static boolean 减少血量开关 =ServerConfig.捉鬼任务减少血量开关;
    private static boolean 全图封锁开关 =ServerConfig.捉鬼任务全图封锁开关;
    private static boolean 全图黑暗开关 =ServerConfig.捉鬼任务全图黑暗开关;
    private static boolean 全图虚弱开关 =ServerConfig.捉鬼任务全图虚弱开关;
    private static boolean 全图诅咒开关 =ServerConfig.捉鬼任务全图诅咒开关;
    private static boolean 全图诱导开关 =ServerConfig.捉鬼任务全图诱导开关;
    private static boolean 直接死亡开关 =ServerConfig.捉鬼任务直接死亡开关;
    private static boolean 直接驱散开关 =ServerConfig.捉鬼任务直接驱散开关;
    private static boolean 减少蓝量开关 =ServerConfig.捉鬼任务减少蓝量开关;
    
    private static boolean 互相残杀开关 =ServerConfig.互相残杀开关;
    
    private static int 伤害间隔时间=ServerConfig.捉鬼任务伤害间隔时间;
    private static int 技能间隔时间=ServerConfig.捉鬼任务技能间隔时间;
    
    private static int 最大伤害扣血数值=ServerConfig.捉鬼任务伤害最大扣血数值;
    private static int 最大伤害扣蓝数值=ServerConfig.捉鬼任务伤害最大扣蓝数值;
    private static int 最小伤害扣血数值=ServerConfig.捉鬼任务伤害最小扣血数值;
    private static int 最小伤害扣蓝数值=ServerConfig.捉鬼任务伤害最小扣蓝数值;
    
    
    private static int 战士职业回避率=ServerConfig.捉鬼任务战士职业伤害回避率;
    private static int 法师职业回避率=ServerConfig.捉鬼任务法师职业伤害回避率;
    private static int 射手职业回避率=ServerConfig.捉鬼任务射手职业伤害回避率;
    private static int 飞侠职业回避率=ServerConfig.捉鬼任务飞侠职业伤害回避率;
    private static int 海盗职业回避率=ServerConfig.捉鬼任务海盗职业伤害回避率;
    
    private static int 战士职业技能回避率=ServerConfig.捉鬼任务战士职业技能回避率;
    private static int 法师职业技能回避率=ServerConfig.捉鬼任务法师职业技能回避率;
    private static int 射手职业技能回避率=ServerConfig.捉鬼任务射手职业技能回避率;
    private static int 飞侠职业技能回避率=ServerConfig.捉鬼任务飞侠职业技能回避率;
    private static int 海盗职业技能回避率=ServerConfig.捉鬼任务海盗职业技能回避率;
    

    public static void 开启进阶BOSS线程() {
        //召唤怪物();
        //System.out.println("开启扎昆线程");
        开启进阶BOSS线程伤害();
        if (进阶BOSS线程 == null) {
            进阶BOSS线程 = Timer.BuffTimer.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    double 随机 = Math.ceil(Math.random() * 20);
                    if (随机 <= 0) {
                        if(全图掉血开关){
                        全图掉HP();
                        }
                    } else if (随机 == 1) {
                        if(全图掉蓝开关){
                        全图掉MP();
                        }
                    } else if (随机 == 2) {
                        if(全图封锁开关){
                        全图封锁();
                        }
                    } else if (随机 == 3) {
                        if(全图黑暗开关){
                        全图黑暗();
                        }
                    } else if (随机 == 4) {
                        if(全图虚弱开关){
                        全图虚弱();
                        }
                    } else if (随机 == 5) {
                        if(全图诅咒开关){
                        全图诅咒();
                        }
                    } else if (随机 == 6) {
                        if(全图诱导开关){
                        全图诱导();
                         }
                    } else if (随机 == 7) {
                         if(全图诱导开关){
                        全图诱导();
                         }
                    } else if (随机 == 8) {
                         if(全图诱导开关){
                        全图诱导();
                         }
                         if(全图掉血开关){
                         全图掉HP();
                         }
                    } else if (随机 == 9) {
                         if(全图掉蓝开关){
                        全图掉HP();
                         }
                        if(全图诱导开关){
                        全图诱导();
                         }
                    } else if (随机 == 10 || 随机 == 11 || 随机 == 12 || 随机 == 13) {
                        if(直接死亡开关){
                        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                                if (chr == null) {
                                    continue;
                                }
                                if (chr.getMapId() == 地图 ) {
                                    chr.startMapEffect("【大型boss】 : boss使用出黑暗魔法，3秒后将吞噬在场所有人。", 5120027);
                                }
                            }
                        }
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000 * 3);
                                    直接死亡();
                                } catch (InterruptedException e) {
                                }
                            }
                        }.start();
                        }
                    } else if (随机 == 14 || 随机 == 15 || 随机 == 16 || 随机 == 17) {
                        if(直接驱散开关){
                        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                                if (chr == null) {
                                    continue;
                                }
                                if (chr.getMapId() == 地图) {
                                    chr.startMapEffect("【大型boss】 : boss使用出黑暗魔法，3秒后将驱散在场所有人。", 5120027);
                                }
                            }
                        }
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000 * 3);
                                    直接驱散();
                                } catch (InterruptedException e) {
                                }
                            }
                        }.start();
                        }
                    } else {
                        if(互相残杀开关){
                        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                                if (chr == null) {
                                    continue;
                                }
                                if (chr.getMapId() == 地图) {
                                   // chr.startMapEffect("【大型boss】 : boss使用出黑暗魔法，诱导玩家可以互相攻击。", 5120027);
                                }
                            }
                        }
                       // MapleParty.互相伤害 += 1;
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000 * 10);
                                   // MapleParty.互相伤害 = 0;
                                } catch (InterruptedException e) {
                                }
                            }
                        }.start();
                        }     
                    }
                }
            }, 1000 * 技能间隔时间);
        }
    }

    public static void 关闭进阶BOSS线程() {
        if (进阶BOSS线程 != null) {
            关闭进阶BOSS线程伤害();
            进阶BOSS线程.cancel(false);
            进阶BOSS线程 = null;
        }
    }

    public static void 开启进阶BOSS线程伤害() {
        if (进阶BOSS线程伤害 == null) {
            进阶BOSS线程伤害 = Timer.BuffTimer.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    int 随机 = (int) Math.ceil(Math.random() * 2);
                    switch (随机) {
                        case 0:
                            if(减少血量开关){
                            减少血量();
                            }
                            break;
                        case 1:
                            if(减少蓝量开关){
                            减少蓝量();
                            }
                            break;
                        default:
                            if(减少血量开关){
                            减少血量();
                            }
                            if(减少蓝量开关){
                            减少蓝量();
                            }
                            break;
                    }
                }
            }, 1000 * 伤害间隔时间);
        }
    }

    public static void 关闭进阶BOSS线程伤害() {
        //System.out.println("关闭扎昆线程");
        if (进阶BOSS线程伤害 != null) {
            进阶BOSS线程伤害.cancel(false);
            进阶BOSS线程伤害 = null;
        }
    }

    public static void 直接驱散() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                    int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                        MapleItemInformationProvider.getInstance().getItemEffect(2030000).applyReturnScroll(chr);
                        chr.dropMessage(6, "直接驱散");
                    } else {
                        chr.dropMessage(6, "躲避了直接驱散");
                    }
                }
            }
        }
    }

    public static void 直接死亡() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                    int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                        chr.addHP(-30000);
                        chr.dropMessage(6, "直接死亡 HP - 999999999");
                    } else {
                        chr.dropMessage(6, "躲避了直接死亡");
                    }
                }
            }
        }
    }

    public static void 减少血量() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                    int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业回避率;
                       break;
                    }
                    if (总概率>成功率) {
                        int 血量 = (int) Math.ceil(Math.random() * (最大伤害扣血数值-最小伤害扣血数值)) +最小伤害扣血数值;
                        chr.setHp(chr.getHp() - 血量);
                        chr.updateSingleStat(MapleStat.HP, chr.getHp());
                        chr.dropMessage(6, "HP - " + 血量);
                    } else {
                          chr.dropMessage(6, "躲避了扣血伤害");
                    }
                }
            }
        }
    }

    public static void 减少蓝量() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                   int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业回避率;
                       break;
                    }
                    if (总概率>成功率) {
                        int 蓝量 = (int) Math.ceil(Math.random() * (最大伤害扣蓝数值-最小伤害扣蓝数值)) +最小伤害扣蓝数值;
                        chr.setMp(chr.getMp() - 蓝量);
                        chr.updateSingleStat(MapleStat.MP, chr.getMp());
                        chr.dropMessage(6, "MP - " + 蓝量);
                    } else {
                        chr.dropMessage(6, "躲避了直接扣蓝");
                    }
                }
            }
        }
    }

    public static void 全图诱导() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                     int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                    if (chr.getJob() != 230 || chr.getJob() != 231 || chr.getJob() != 232) {
                        MobSkill mobSkill = MobSkillFactory.getMobSkill(128, 1);
                        MapleDisease disease = null;
                        disease = MapleDisease.getByMobSkill(128);
                        chr.giveDebuff(disease, mobSkill);
                        chr.dropMessage(6, "被诱导");
                    } else {
                        chr.dropMessage(6, "主教职业群，免疫被诱导");
                    }
                }else {
                    chr.dropMessage(6, "躲避了诱导技能");
                    }
                }
            }
        }
    }

    public static void 全图诅咒() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                    int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                    MobSkill mobSkill = MobSkillFactory.getMobSkill(124, 1);
                    MapleDisease disease = null;
                    disease = MapleDisease.getByMobSkill(124);
                    chr.giveDebuff(disease, mobSkill);
                    chr.dropMessage(6, "被诅咒");
                }else{
                  chr.dropMessage(6, "躲避了诅咒技能");      
                }
                }
            }
        }
    }

    public static void 全图虚弱() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                    int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                    MobSkill mobSkill = MobSkillFactory.getMobSkill(122, 1);
                    MapleDisease disease = null;
                    disease = MapleDisease.getByMobSkill(122);
                    chr.giveDebuff(disease, mobSkill);
                    chr.dropMessage(6, "被虚弱");
                }else{
                    chr.dropMessage(6, "躲避了虚弱技能");    
                }
                }
            }
        }
    }

    public static void 全图黑暗() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                    int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                    MobSkill mobSkill = MobSkillFactory.getMobSkill(121, 1);
                    MapleDisease disease = null;
                    disease = MapleDisease.getByMobSkill(121);
                    chr.giveDebuff(disease, mobSkill);
                    chr.dropMessage(6, "被黑暗");
                }else{
                     chr.dropMessage(6, "躲避了黑暗技能");
                    
                }
                }    
            }
        }
    }

    public static void 全图封锁() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                     int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                    MobSkill mobSkill = MobSkillFactory.getMobSkill(120, 1);
                    MapleDisease disease = null;
                    disease = MapleDisease.getByMobSkill(120);
                    chr.giveDebuff(disease, mobSkill);
                    chr.dropMessage(6, "被封锁");
                }else{
                    chr.dropMessage(6, "躲避了封锁技能");
                }
                }
            }
        }

    }

    public static void 全图掉HP() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图  && !chr.isGM()) {
                     int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                    chr.setHp(1);
                    chr.updateSingleStat(MapleStat.HP, 1);
                }else{
                    chr.dropMessage(6, "躲避了致命一击");
                }
                }
            }
        }
    }

    public static void 全图掉MP() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                if (chr == null) {
                    continue;
                }
                if (chr.getMapId() == 地图 && !chr.isGM()) {
                    int 总概率 = (int) Math.ceil(Math.random() * 100);
                    int 成功率 = 0;
                    
                    switch(chr.getJob()){
                        case 100:
                        case 110:
                        case 111:
                        case 112:
                        case 120: 
                        case 121:
                        case 122:
                        case 130:
                        case 131:
                        case 132: 
                        case 1100:
                        case 1110: 
                        case 1111:  
                        case 2100:
                        case 2110:
                        case 2111:
                       成功率 = 战士职业技能回避率;
                       break;
                       case 200:
                        case 210:
                        case 211:
                        case 212:
                        case 220: 
                        case 221:
                        case 222:
                        case 230:
                        case 231:
                        case 232: 
                        case 1200:
                        case 1210: 
                        case 1211:     
                       成功率 = 法师职业技能回避率;
                       break;
                        case 300:
                        case 310:
                        case 311:
                        case 312:
                        case 320: 
                        case 321:
                        case 322:
                        case 1300:
                        case 1310: 
                        case 1311:     
                       成功率 = 射手职业技能回避率;
                       break;
                        case 400:
                        case 410:
                        case 411:
                        case 412:
                        case 420: 
                        case 421:
                        case 422:
                        case 1400:
                        case 1410: 
                        case 1411:     
                       成功率 = 飞侠职业技能回避率;
                       break;
                        case 500:
                        case 510:
                        case 511:
                        case 512:
                        case 520: 
                        case 521:
                        case 522:    
                       成功率 = 海盗职业技能回避率;
                       break;
                    }
                    if (总概率>成功率) {
                    chr.setMp(1);
                    chr.updateSingleStat(MapleStat.MP, 1);
                }else{
                    chr.dropMessage(6, "躲避了致命一击");
                    }
                }
            }
        }
    }

    /*public static void 召唤怪物() {
        ChannelServer channelServer = ChannelServer.getInstance(频道);
        MapleMonster mapleMonster = MapleLifeFactory.getMonster(强化BOSS);
        MapleMap mapleMap = channelServer.getMapFactory().getMap(地图);
        //设置怪物坐标
        mapleMonster.setPosition(坐标);
        mapleMap.spawnMonster(mapleMonster, -2);
    }*/

    private 捉鬼任务线程() {
    }
}
