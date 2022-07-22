package handling.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.anticheat.CheatTracker;
import client.anticheat.CheatingOffense;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.ServerConfig;
import handling.world.World;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.pvp.MaplePvp;
import tools.AttackPair;
import tools.FileoutputUtil;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.JobPacket;
import scripting.NPCConversationManager;

public class DamageParse {
    /**
     * @param attack 攻击信息
     * @param theSkill 技能信息
     * @param player 角色信息
     * @param attackCount 攻击次数
     * @param effect 技能效果
     * @param maxDamagePerMonster 每个怪物的最大伤害
     * @param visProjectile 可见的子弹、箭矢、飞镖等...
     */
    @SuppressWarnings("empty-statement")
    
    
   public static boolean applAttackRange(AttackInfo attack, MapleCharacter player, MapleStatEffect effect, MapleMonster mob)
   {
     int xs = distance(player.getPosition().x, player.getPosition().y, mob.getPosition().x, mob.getPosition().y);
     
     if (attack.skill == 0) {
       if (xs > 500) {
         player.dropMessage(-1, "攻击无效 - 攻击距离过远");
         return true;
       }
       return false;
     }
     if ((attack.skill == 0) || (effect.getrb() == null) || (effect.getlt() == null)) {
       return false;
     }
     double bd = 1.3D;
     int ys = (int)(distance(effect.getrb().x, effect.getrb().y, effect.getlt().x, effect.getlt().y) * bd);
     
     if ((ServerConfig.applAttackRange) && (
       (player.isGM()) || (ServerConfig.logPackets))) {
       player.dropMessage(5, "技能 " + SkillFactory.getSkill(attack.skill).getName() + " 范围 (收到封包):" + xs + " (服务端对比(" + bd + "));" + ys);
     }
     
     if (xs > ys) {
     //  System.out.println("[" + FileoutputUtil.CurrentReadable_Time() + "] - [系统警告]:" + player.getName() + " 监测出使用非法软件，攻击范围异常。");
       player.dropMessage(-1, "攻击无效 - 伤害范围过远");
       return true;
     }
     return false;
   }
   
   public static int distance(int x, int y, int x1, int y1) {
     Point p1 = new Point(x, y);
     Point p2 = new Point(x1, y1);
     return (int)Math.sqrt(Math.abs((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY())));
   }

   public static boolean applAttackNumber(AttackInfo attack, MapleCharacter player, MapleStatEffect effect)
   {
     int Number = attack.hits;
     
     int xmlmobcountNumber = effect.getBulletCount();
     if (effect.getAttackCount() > effect.getBulletCount()) {
       xmlmobcountNumber = effect.getAttackCount();
     }
     
     if (player.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
       xmlmobcountNumber *= 2;
     }
     
     if ((ServerConfig.applAttackNumber) && (
       (player.isGM()) || (ServerConfig.logPackets))) {
       player.dropMessage(5, "技能:" + SkillFactory.getSkill(attack.skill).getName());
       player.dropMessage(5, "技能段数(收到封包):" + Number);
       player.dropMessage(5, "技能段数(服务端对比):" + xmlmobcountNumber);
     }
     
     if (Number > xmlmobcountNumber) {
    //   System.out.println("[" + FileoutputUtil.CurrentReadable_Time() + "] - [系统警告]:" + player.getName() + " 监测出使用非法软件，攻击段数异常。");
       player.dropMessage(-1, "攻击无效 - 伤害段数异常");
       return true;
     }
     return false;
   }
 
 
   public static boolean applAttackmobcount(AttackInfo attack, MapleCharacter player, MapleStatEffect effect)
   {
     int mobcount = attack.targets;
     
     int xmlmobcount = effect.getMobCount();
     
     if ((ServerConfig.applAttackmobcount) && (
       (player.isGM()) || (ServerConfig.logPackets))) {
       player.dropMessage(5, "技能:" + SkillFactory.getSkill(attack.skill).getName());
       player.dropMessage(5, "技能目标(收到封包):" + mobcount);
       player.dropMessage(5, "技能目标(服务端对比):" + xmlmobcount);
     }
   
 
     if (mobcount > xmlmobcount) {
     //  System.out.println("[" + FileoutputUtil.CurrentReadable_Time() + "] - [系统警告]:" + player.getName() + " 监测出使用非法软件，攻击目标异常。");
       player.dropMessage(-1, "攻击无效 - 攻击数量异常");
       return true;
     }
     return false;
   }


   public static boolean applAttackMP(AttackInfo attack, MapleCharacter player, MapleStatEffect effect)
   {
     int skillmp = effect.getMpCon();
     
     int xmlmp = SkillFactory.getSkill(attack.skill).getEffect(effect.getLevel()).getMpCon();
     
     int playermp = player.getStat().getMp();
     
     int playermph = playermp - skillmp;
     
     if ((skillmp > 0) || (xmlmp > 0))
     {
       if ((playermp <= 0) || (skillmp != xmlmp) || (playermp - skillmp != playermp - xmlmp) || (playermph <= 0)) {
         System.out.println("[" + FileoutputUtil.CurrentReadable_Time() + "] - [系统警告]:" + player.getName() + " 监测出使用非法软件，技能无蓝耗/锁蓝等等。");
         player.dropMessage(-1, "攻击无效 - 监测出使用非法软件,技能无蓝耗/锁蓝等");
         return true;
       }
       
       if ((ServerConfig.applAttackMP) && (
         (player.isGM()) || (ServerConfig.logPackets))) {
         player.dropMessage(5, "技能:" + SkillFactory.getSkill(attack.skill).getName());
         player.dropMessage(5, "技能消耗emp(收到封包):" + skillmp);
         player.dropMessage(5, "技能消耗xmp(服务端对比):" + xmlmp);
         player.dropMessage(5, "玩家状态mp(使用前状态):" + playermp);
         player.dropMessage(5, "玩家状态mp(使用后状态):" + (playermp - skillmp));
       }
     }
     
     return false;
   }
   
    
    public static void applyAttack(AttackInfo attack, Skill theSkill, MapleCharacter player, int attackCount, double maxDamagePerMonster, MapleStatEffect effect, AttackType attack_type) {
        if (!player.isAlive()) {//如果玩家死亡
            player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            return;
        }
        if ((attack.real) && (GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)) { //大于100的就检测攻击时间
            player.getCheatTracker().checkAttack(attack.skill, attack.lastAttackTickCount);
        }
        if (attack.skill != 0) {//当攻击技能不等于空
            if (effect == null) {
                player.getClient().getSession().write(CWvsContext.enableActions());
                return;
            }
            
       if ((ServerConfig.applAttackNumber) && 
         (applAttackNumber(attack, player, effect))) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
       
 
       if ((ServerConfig.applAttackmobcount) && 
         (applAttackmobcount(attack, player, effect))) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
       
 
       if ((ServerConfig.applAttackMP) && 
         (applAttackMP(attack, player, effect))) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
            
            if (GameConstants.isMulungSkill(attack.skill)) {
                if (player.getMapId() / 10000 != 92502) {
                    return;
                }
                if (player.getMulungEnergy() < 10000) {
                    return;
                }
                player.mulung_EnergyModify(false);
            } else if (GameConstants.isPyramidSkill(attack.skill)) {
                if (player.getMapId() / 1000000 != 926) {
                    return;
                }

                if ((player.getPyramidSubway() != null) && (player.getPyramidSubway().onSkillUse(player)));
            } else if (GameConstants.isInflationSkill(attack.skill)) {
                if (player.getBuffedValue(MapleBuffStat.GIANT_POTION) != null);
            } else if ((attack.targets > effect.getMobCount()) && (attack.skill != 1211002) && (attack.skill != 1220010)) {
                player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT);
                return;
            }
        }
        if (player.getClient().getChannelServer().isAdminOnly()) {
            player.dropMessage(-1, new StringBuilder().append("Animation: ").append(Integer.toHexString((attack.display & 0x8000) != 0 ? attack.display - 32768 : attack.display)).toString());
        }
         /* 检测技能是否为正常的攻击次数 ： 在此处定义的技能将不会被检测 */
        boolean useAttackCount = (attack.skill != 4211006) && (attack.skill != 3221007) && (attack.skill != 23121003) && ((attack.skill != 1311001) || (player.getJob() != 132)) && (attack.skill != 3211006);

        if ((attack.hits > 0) && (attack.targets > 0)) {
            if (!player.getStat().checkEquipDurabilitys(player, -1)) {
                player.dropMessage(5, "一个道具的耐久度已经归零，但背包空位不足.");
                return;
            }
        }
        int totDamage = 0;
        MapleMap map = player.getMap();
        if (map.isPvpMap()) {
            MaplePvp.doPvP(player, map, attack, effect);
        } else if (map.isPartyPvpMap()) {
            MaplePvp.doPartyPvP(player, map, attack, effect);
        } else if (map.isGuildPvpMap()) {
            MaplePvp.doGuildPvP(player, map, attack, effect);
        }
        if (attack.skill == 4211006) {
            for (AttackPair oned : attack.allDamage) {
                if (oned.attack == null) {
                    MapleMapObject mapobject = map.getMapObject(oned.objectid, MapleMapObjectType.ITEM);

                    if (mapobject != null) {
                        MapleMapItem mapitem = (MapleMapItem) mapobject;
                        mapitem.getLock().lock();
                        try {
                            if (mapitem.getMeso() > 0) {
                                if (mapitem.isPickedUp()) {
                                    return;
                                }
                                map.removeMapObject(mapitem);
                                map.broadcastMessage(CField.explodeDrop(mapitem.getObjectId()));
                                mapitem.setPickedUp(true);
                            } else {
                                player.getCheatTracker().registerOffense(CheatingOffense.ETC_EXPLOSION);
                                return;
                            }
                        } finally {
                            mapitem.getLock().unlock();
                        }
                    } else {
                        player.getCheatTracker().registerOffense(CheatingOffense.EXPLODING_NONEXISTANT);
                        return;
                    }
                }
            }
        }
        int totDamageToOneMonster = 0;
        long hpMob = 0L;
        PlayerStats stats = player.getStat();

        int CriticalDamage = stats.passive_sharpeye_percent();
        int ShdowPartnerAttackPercentage = 0;
        if ((attack_type == AttackType.RANGED_WITH_SHADOWPARTNER) || (attack_type == AttackType.NON_RANGED_WITH_MIRROR)) {
            MapleStatEffect shadowPartnerEffect = player.getStatForBuff(MapleBuffStat.SHADOWPARTNER);
            if (shadowPartnerEffect != null) {
                ShdowPartnerAttackPercentage += shadowPartnerEffect.getX();
            }
            attackCount /= 2;
        }
        ShdowPartnerAttackPercentage *= (CriticalDamage + 100) / 100;
        if (attack.skill == 4221001) {
            ShdowPartnerAttackPercentage *= 10;
        }

        double maxDamagePerHit = 0.0D;
        //double maxDamagePerHit2 = 0.0D;
        for (AttackPair oned : attack.allDamage) {
            MapleMonster monster = map.getMonsterByOid(oned.objectid);
            
       if ((ServerConfig.applAttackRange) && 
         (applAttackRange(attack, player, effect, monster))) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
       
            if ((monster != null) && (monster.getLinkCID() <= 0)) {
                totDamageToOneMonster = 0;
                hpMob = monster.getMobMaxHp();
                MapleMonsterStats monsterstats = monster.getStats();
                int fixeddmg = monsterstats.getFixedDamage();
                boolean Tempest = (monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006) || (attack.skill == 21120006) || (attack.skill == 1221011);

                if ((!Tempest) /*&& (!player.isGM())*/) {
                    if (((player.getJob() >= 3200) && (player.getJob() <= 3212) && (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) || (attack.skill == 3221007) || (attack.skill == 23121003) || (((player.getJob() < 3200) || (player.getJob() > 3212)) && (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)))) {
                        maxDamagePerHit = CalculateMaxWeaponDamagePerHit(player, monster, attack, theSkill, effect, maxDamagePerMonster, CriticalDamage);
                    } else {
                        maxDamagePerHit = 1.0D;
                    }
                }
                byte overallAttackCount = 0;
                //maxDamagePerHit2 = maxDamagePerHit * 1.5;
                int criticals = 0;
                for (Pair eachde : oned.attack) {
                    Integer eachd = (Integer) eachde.left;
                    overallAttackCount = (byte) (overallAttackCount + 1);
                    if (((Boolean) eachde.right)) {
                        criticals++;
                    }
                    //if ((useAttackCount) && (overallAttackCount - 1 == attackCount)) {
                    //    maxDamagePerHit = maxDamagePerHit / 100.0D * (ShdowPartnerAttackPercentage == 0 ? 1 : ShdowPartnerAttackPercentage * (monsterstats.isBoss() ? stats.bossdam_r : stats.dam_r) / 100.0D);
                    //} 

                    if (fixeddmg != -1) {
                        if (monsterstats.getOnlyNoramlAttack()) {
                            eachd = attack.skill != 0 ? 0 : fixeddmg;
                        } else {
                            eachd = fixeddmg;
                        }
                    } else if (monsterstats.getOnlyNoramlAttack()) {
                        eachd = attack.skill != 0 ? 0 : Math.min(eachd, (int) maxDamagePerHit);
                    } else /*if (!player.isGM())*/ if (Tempest) {
                        if (eachd > monster.getMobMaxHp()) {
                            eachd = (int) Math.min(monster.getMobMaxHp(), 2147483647L);
                            player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE);
                        }
                    } else if (((player.getJob() >= 3200) && (player.getJob() <= 3212) && (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) || (attack.skill == 23121003) || (((player.getJob() < 3200) || (player.getJob() > 3212)) && (!monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)))) {
                        if (eachd > maxDamagePerHit) {

                            eachd = (int) maxDamagePerHit;

                        }

                    }
                    totDamageToOneMonster += eachd;

                    if (((eachd == 0) || (monster.getId() == 9700021)) && (player.getPyramidSubway() != null)) {
                        player.getPyramidSubway().onMiss(player);
                    }
                }
                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);

                if ((GameConstants.getAttackDelay(attack.skill, theSkill) >= 100) && (!GameConstants.isNoDelaySkill(attack.skill)) && (attack.skill != 3101005) && (!monster.getStats().isBoss()) && (player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants.getAttackRange(effect, player.getStat().defRange))) {
                    player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, new StringBuilder().append("[Distance: ").append(player.getTruePosition().distanceSq(monster.getTruePosition())).append(", Expected Distance: ").append(GameConstants.getAttackRange(effect, player.getStat().defRange)).append(" Job: ").append(player.getJob()).append("]").toString());
                }
                double range = player.getPosition().distanceSq(monster.getPosition());
                double SkillRange = GameConstants.getAttackRange(player, effect, attack);

                if (range > SkillRange && !GameConstants.inBossMap(player.getMapId())) { // 815^2 <-- the most ranged attack in the game is Flame Wheel at 815 range
                    World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + player.getName() + " (等级 " + player.getLevel() + ") " + " 攻击范围异常,技能:" + attack.skill + "　怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 计算范围:" + (int) range));
                    FileoutputUtil.logToFile("logs/Hack/攻击范围异常.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家<" + player.getLevel() + ">: " + player.getName() + " 攻击范围异常,技能:" + attack.skill + "　怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 计算范围:" + (int) range);
                    if (range > SkillRange * 2) {
                        World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + player.getName() + " (等级 " + player.getLevel() + ") " + " 超大攻击范围,技能:" + attack.skill + "　怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 计算范围:" + (int) range));
                        FileoutputUtil.logToFile("logs/Hack/超大攻击范围异常.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家<" + player.getLevel() + ">: " + player.getName() + " 超大攻击范围,技能:" + attack.skill + "　怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 计算范围:" + (int) range);
                    }
                }

                if (player.getSkillLevel(36110005) > 0) {
                    Skill skill = SkillFactory.getSkill(36110005);
                    MapleStatEffect eff = skill.getEffect(player.getSkillLevel(skill));
                    if (player.getLastCombo() + 5000 < System.currentTimeMillis()) {
                        monster.setTriangulation(0);
                        //player.clearDamageMeters();
                    }
                    if (eff.makeChanceResult()) {
                        player.setLastCombo(System.currentTimeMillis());
                        if (monster.getTriangulation() < 3) {
                            monster.setTriangulation(monster.getTriangulation() + 1);
                        }
                        monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.DARKNESS, eff.getX(), eff.getSourceId(), null, false), false, eff.getY() * 1000, true, eff);
                        monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.TRIANGULATION, monster.getTriangulation(), eff.getSourceId(), null, false), false, eff.getY() * 1000, true, eff);
                    }
                }

                if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null) {
                    switch (attack.skill) {
                        case 0:
                        case 4001334:
                        case 4201005:
                        case 4211002:
                        case 4211004:
                        case 4221003:
                        case 4221007:
                            handlePickPocket(player, monster, oned);
                    }

                }

                if ((totDamageToOneMonster > 0) || (attack.skill == 1221011) || (attack.skill == 21120006)) {
                    if (GameConstants.isDemonSlayer(player.getJob())) {
                        player.handleForceGain(monster.getObjectId(), attack.skill);
                    }
                    if ((GameConstants.isPhantom(player.getJob())) && (attack.skill != 24120002) && (attack.skill != 24100003)) {
                        player.handleCardStack();
                    }
                    if (GameConstants.isKaiser(player.getJob())) {
                        player.handleKaiserCombo();
                    }
                    if (attack.skill != 1221011) {
                        monster.damage(player, totDamageToOneMonster, true, attack.skill);
                        if(monster.getId()==怪物代码){
                        记录打怪伤害(player.getId(), totDamageToOneMonster);
                        }
                    } else {
                        monster.damage(player, (int) (monster.getStats().isBoss() ? 500000 : monster.getHp() > Integer.MAX_VALUE ? Integer.MAX_VALUE : monster.getHp() - 1), true, attack.skill);
                    }

                    if (monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)) {
                        player.addHP(-(7000 + Randomizer.nextInt(8000)));
                    }
                    player.onAttack(monster.getMobMaxHp(), monster.getMobMaxMp(), attack.skill, monster.getObjectId(), totDamage, 0);
                    switch (attack.skill) {
                        case 4001002:
                        case 4001334:
                        case 4001344:
                        case 4111005:
                        case 4121007:
                        case 4201005:
                        case 4211002:
                        case 4221001:
                        case 4221007:
                        case 4301001:
                        case 4311002:
                        case 4311003:
                        case 4331000:
                        case 4331004:
                        case 4331005:
                        case 4331006:
                        case 4341002:
                        case 4341004:
                        case 4341005:
                        case 4341009:
                        case 14001004:
                        case 14111002:
                        case 14111005:
                            int[] skills = {4120005, 4220005, 4340001, 14110004};
                            for (int i : skills) {
                                Skill skill = SkillFactory.getSkill(i);
                                if (player.getTotalSkillLevel(skill) > 0) {
                                    MapleStatEffect venomEffect = skill.getEffect(player.getTotalSkillLevel(skill));
                                    if (!venomEffect.makeChanceResult()) {
                                        break;
                                    }
                                    monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.POISON, 1, i, null, false), true, venomEffect.getDuration(), true, venomEffect);
                                    break;
                                }

                            }

                            break;
                        case 4201004:
                            monster.handleSteal(player);
                            break;
                        case 21000002:
                        case 21100001:
                        case 21100002:
                        case 21100004:
                        case 21110002:
                        case 21110003:
                        case 21110004:
                        case 21110006:
                        case 21110007:
                        case 21110008:
                        case 21120002:
                        case 21120005:
                        case 21120006:
                        case 21120009:
                        case 21120010:
                            if ((player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) && (!monster.getStats().isBoss())) {
                                MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.WK_CHARGE);
                                if (eff != null) {
                                    monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.SPEED, eff.getX(), eff.getSourceId(), null, false), false, eff.getY() * 1000, true, eff);
                                }
                            }
                            if ((player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) != null) && (!monster.getStats().isBoss())) {
                                MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BODY_PRESSURE);

                                if ((eff != null) && (eff.makeChanceResult()) && (!monster.isBuffed(MonsterStatus.NEUTRALISE))) {
                                    monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.NEUTRALISE, 1, eff.getSourceId(), null, false), false, eff.getX() * 1000, true, eff);
                                }
                            }
                            break;
                    }
//          int randomDMG = Randomizer.nextInt(player.getDamage2() - player.getReborns() + 1) + player.getReborns();
//          monster.damage(player, randomDMG, true, attack.skill);
//          if (player.getshowdamage() == 1)
//            player.dropMessage(5, new StringBuilder().append("You have done ").append(randomDMG).append(" extra RB damage! (disable/enable this with @dmgnotice)").toString());
//        }
                    //else {
//          if (player.getDamage() > 2147483647L) {
//            long randomDMG = player.getDamage();
//            monster.damage(player, monster.getMobMaxHp(), true, attack.skill);
//            if (player.getshowdamage() == 1) {
//              player.dropMessage(5, new StringBuilder().append("You have done ").append(randomDMG).append(" extra RB damage! (disable/enable this with @dmgnotice)").toString());
//            }
//          }
                    if (totDamageToOneMonster > 0) {
                        Item weapon_ = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
                        if (weapon_ != null) {
                            MonsterStatus stat = GameConstants.getStatFromWeapon(weapon_.getItemId());
                            if ((stat != null) && (Randomizer.nextInt(100) < GameConstants.getStatChance())) {
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(stat, Integer.valueOf(GameConstants.getXForStat(stat)), GameConstants.getSkillForStat(stat), null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, 10000L, false, null);
                            }
                        }
                        if (player.getBuffedValue(MapleBuffStat.BLIND) != null) {
                            MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BLIND);

                            if ((eff != null) && (eff.makeChanceResult())) {
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.ACC, Integer.valueOf(eff.getX()), eff.getSourceId(), null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, true, eff);
                            }
                        }

                        if (player.getBuffedValue(MapleBuffStat.HAMSTRING) != null) {
                            MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.HAMSTRING);

                            if ((eff != null) && (eff.makeChanceResult())) {
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.SPEED, eff.getX(), 3121007, null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 1000, true, eff);
                            }
                        }
                        if ((player.getJob() == 121) || (player.getJob() == 122)) {
                            Skill skill = SkillFactory.getSkill(1211006);
                            if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, skill)) {
                                MapleStatEffect eff = skill.getEffect(player.getTotalSkillLevel(skill));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.FREEZE, 1, skill.getId(), null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 2000, true, eff);
                            }
                            skill = SkillFactory.getSkill(1211005);
                            if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, skill)) {
                                MapleStatEffect eff = skill.getEffect(player.getTotalSkillLevel(skill));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.FREEZE, 1, skill.getId(), null, false);
                                monster.applyStatus(player, monsterStatusEffect, false, eff.getY() * 2000, true, eff);
                            }
                        }
                    }
                    if ((effect != null) && (effect.getMonsterStati().size() > 0) && (effect.makeChanceResult())) {
                        for (Map.Entry z : effect.getMonsterStati().entrySet()) {
                            monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus) z.getKey(), (Integer) z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
                        }
                    }
                }
            }
        }
        MapleMonster monster;
        if ((attack.skill == 4331003) && ((hpMob <= 0L) || (totDamageToOneMonster < hpMob))) {
            return;
        }
        if ((hpMob > 0L) && (totDamageToOneMonster > 0)) {
            player.afterAttack(attack.targets, attack.hits, attack.skill);
        }
        if ((attack.skill != 0) && ((attack.targets > 0) || ((attack.skill != 4331003) && (attack.skill != 4341002))) && (!GameConstants.isNoDelaySkill(attack.skill))) {
            boolean applyTo = effect.applyTo(player, attack.position);
        }
        if (attack.skill == 14111006) {
            effect.applyTo(player, attack.positionxy);
        }
        if ((totDamage > 1) && (GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)) {
            CheatTracker tracker = player.getCheatTracker();

            tracker.setAttacksWithoutHit(true);
            if (tracker.getAttacksWithoutHit() > 50) {
                tracker.setAttacksWithoutHit(false);
               // World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + player.getName() + " (等级 " + player.getLevel() + ") " + "疑似无敌。 "));
                //tracker.registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(tracker.getAttacksWithoutHit()));
            }
        }
    }

    @SuppressWarnings("empty-statement")
    public static final void applyAttackMagic(AttackInfo attack, Skill theSkill, MapleCharacter player, MapleStatEffect effect, double maxDamagePerHit) {
        if (!player.isAlive()) {
            player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            //System.out.println("Return 7");
            return;
        }
        if ((attack.real) && (GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)) {
            player.getCheatTracker().checkAttack(attack.skill, attack.lastAttackTickCount);
        }

        if (effect != null && effect.getBulletCount() > 1) {
            if ((attack.hits > effect.getBulletCount()) || (attack.targets > effect.getMobCount())) {
                player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT);
                //System.out.println("Return 9");
                return;
            }

        } else if (effect != null && (((attack.hits > effect.getAttackCount()) && (effect.getAttackCount() != 0)) || (attack.targets > effect.getMobCount()))) {
            player.getCheatTracker().registerOffense(CheatingOffense.MISMATCHING_BULLETCOUNT);
            //System.out.println("Return 10");
            return;
        }
       if ((ServerConfig.applAttackNumber) && 
         (applAttackNumber(attack, player, effect))) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
       
       if ((ServerConfig.applAttackmobcount) && 
         (applAttackmobcount(attack, player, effect))) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
       
      if ((ServerConfig.applAttackMP) && 
         (applAttackMP(attack, player, effect))) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
          
        if ((attack.hits > 0) && (attack.targets > 0) && (!player.getStat().checkEquipDurabilitys(player, -1))) {
            player.dropMessage(5, "一个项目已经用完的耐久性，但没有库存室去o.");
            //System.out.println("Return 11");
            return;
        }

        if (GameConstants.isMulungSkill(attack.skill)) {
            if (player.getMapId() / 10000 != 92502) {
                return;
            }
            if (player.getMulungEnergy() < 10000) {
                return;
            }
            player.mulung_EnergyModify(false);
        } else if (GameConstants.isPyramidSkill(attack.skill)) {
            if (player.getMapId() / 1000000 != 926) {
                return;
            }

            if ((player.getPyramidSubway() != null) && (player.getPyramidSubway().onSkillUse(player)));
        } else if ((GameConstants.isInflationSkill(attack.skill)) && (player.getBuffedValue(MapleBuffStat.GIANT_POTION) == null)) {
            return;
        }

        if (player.getClient().getChannelServer().isAdminOnly()) {
            player.dropMessage(-1, new StringBuilder().append("Animation: ").append(Integer.toHexString((attack.display & 0x8000) != 0 ? attack.display - 32768 : attack.display)).toString());
        }
        PlayerStats stats = player.getStat();

        Element element = player.getBuffedValue(MapleBuffStat.ELEMENT_RESET) != null ? Element.NEUTRAL : theSkill != null ? theSkill.getElement() : null;

        double MaxDamagePerHit = 0.0D;
        int totDamage = 0;

        int CriticalDamage = stats.passive_sharpeye_percent();
        Skill eaterSkill = SkillFactory.getSkill(GameConstants.getMPEaterForJob(player.getJob()));
        int eaterLevel = player.getTotalSkillLevel(eaterSkill);

        MapleMap map = player.getMap();
        if (map.isPvpMap()) {
            MaplePvp.doPvP(player, map, attack, effect);
        } else if (map.isPartyPvpMap()) {
            MaplePvp.doPartyPvP(player, map, attack, effect);
        } else if (map.isGuildPvpMap()) {
            MaplePvp.doGuildPvP(player, map, attack, effect);
        }
        for (AttackPair oned : attack.allDamage) {
            MapleMonster monster = map.getMonsterByOid(oned.objectid);
       if ((ServerConfig.applAttackRange) && 
         (applAttackRange(attack, player, effect, monster))) {
         player.getClient().getSession().write(CWvsContext.enableActions());
         return;
       }
            if ((monster != null) && (monster.getLinkCID() <= 0)) {
                boolean Tempest = (monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006) && (!monster.getStats().isBoss());
                int totDamageToOneMonster = 0;
                MapleMonsterStats monsterstats = monster.getStats();
                int fixeddmg = monsterstats.getFixedDamage();
                if ((!Tempest) /*&& (!player.isGM())*/) {
                    if ((!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) {
                        MaxDamagePerHit = CalculateMaxMagicDamagePerHit(player, theSkill, monster, monsterstats, stats, element, CriticalDamage, maxDamagePerHit, effect);
                    } else {
                        MaxDamagePerHit = 1.0D;
                    }
                }
                byte overallAttackCount = 0;

                for (Pair eachde : oned.attack) {
                    Integer eachd = (Integer) eachde.left;
                    overallAttackCount = (byte) (overallAttackCount + 1);
                    if (fixeddmg != -1) {
                        eachd = monsterstats.getOnlyNoramlAttack() ? 0 : fixeddmg;
                    } else if (monsterstats.getOnlyNoramlAttack()) {
                        eachd = 0;
                    } else /*if (!player.isGM())*/ if (Tempest) {
                        if (eachd > monster.getMobMaxHp()) {
                            eachd = (int) Math.min(monster.getMobMaxHp(), 2147483647L);
                            player.getCheatTracker().registerOffense(CheatingOffense.HIGH_DAMAGE_MAGIC);
                        }
                    } else if ((!monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) && (!monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT))) {
                        if (eachd > MaxDamagePerHit) {
                            eachd = (int) MaxDamagePerHit;
                        }
                    }
                    totDamageToOneMonster += eachd;
                }

                totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);

                if ((GameConstants.getAttackDelay(attack.skill, theSkill) >= 100) && (!GameConstants.isNoDelaySkill(attack.skill)) && (!monster.getStats().isBoss()) && (player.getTruePosition().distanceSq(monster.getTruePosition()) > GameConstants.getAttackRange(effect, player.getStat().defRange))) {
                    player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, new StringBuilder().append("[Distance: ").append(player.getTruePosition().distanceSq(monster.getTruePosition())).append(", Expected Distance: ").append(GameConstants.getAttackRange(effect, player.getStat().defRange)).append(" Job: ").append(player.getJob()).append("]").toString());
                    return;
                }
                double range = player.getPosition().distanceSq(monster.getPosition());
                double SkillRange = GameConstants.getAttackRange(player, effect, attack);

                if (range > SkillRange && !GameConstants.inBossMap(player.getMapId())) { // 815^2 <-- the most ranged attack in the game is Flame Wheel at 815 range
                    World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + player.getName() + " (等级 " + player.getLevel() + ") " + " 攻击范围异常,技能:" + attack.skill + "　怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 计算范围:" + (int) range));
                    FileoutputUtil.logToFile("logs/Hack/攻击范围异常.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家<" + player.getLevel() + ">: " + player.getName() + " 攻击范围异常,技能:" + attack.skill + "　怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 计算范围:" + (int) range);
                    if (range > SkillRange * 2) {
                        World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + player.getName() + " (等级 " + player.getLevel() + ") " + " 超大攻击范围,技能:" + attack.skill + "　怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 计算范围:" + (int) range));
                        FileoutputUtil.logToFile("logs/Hack/超大攻击范围异常.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家<" + player.getLevel() + ">: " + player.getName() + " 超大攻击范围,技能:" + attack.skill + "　怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 计算范围:" + (int) range);
                    }
                }
                if ((attack.skill == 2301002) && (!monsterstats.getUndead())) {
                    player.getCheatTracker().registerOffense(CheatingOffense.HEAL_ATTACKING_UNDEAD);
                    return;
                }
                if (GameConstants.isLuminous(player.getJob())) {
                    player.handleLuminous(attack.skill);
                }
                if (totDamageToOneMonster > 0) {
                    monster.damage(player, totDamageToOneMonster, true, attack.skill);
                    if(monster.getId()==怪物代码){
                        记录打怪伤害(player.getId(), totDamageToOneMonster);
                        }
                    if ((player.getJob() == 210) || (player.getJob() == 211) || (player.getJob() == 212)) {
                        if ((attack.skill == 2111006) || (attack.skill == 2101005)) {

                            Skill skill = SkillFactory.getSkill(attack.skill);
                            MapleStatEffect eff = skill.getEffect(player.getTotalSkillLevel(skill));
                            if (eff.makeChanceResult()) {
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.POISON, 1, skill.getId(), null, false);
                                monster.applyStatus(player, monsterStatusEffect, true, eff.getDuration(), true, eff);
                            }
                        }
                    }
                    if ((effect != null) && (effect.getMonsterStati().size() > 0) && (effect.makeChanceResult())) {
                        for (Map.Entry z : effect.getMonsterStati().entrySet()) {
                            monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus) z.getKey(), (Integer) z.getValue(), theSkill.getId(), null, false), effect.isPoison(), effect.getDuration(), true, effect);
                        }
                    }
                    if (eaterLevel > 0) {
                        eaterSkill.getEffect(eaterLevel).applyPassive(player, monster);
                    }
                }
            }

        }
        if (attack.skill != 2301002) {
            if (effect != null) {
                effect.applyTo(player);
            }
        }

        if ((totDamage > 1) && (GameConstants.getAttackDelay(attack.skill, theSkill) >= 100)) {
            CheatTracker tracker = player.getCheatTracker();
            tracker.setAttacksWithoutHit(true);

            if (tracker.getAttacksWithoutHit() > 50) {
                tracker.setAttacksWithoutHit(false);
                World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + player.getName() + " (等级 " + player.getLevel() + ") " + "疑似无敌。 "));
                //tracker.registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(tracker.getAttacksWithoutHit()));
            }
        }
    }

    private static double CalculateMaxMagicDamagePerHit(MapleCharacter chr, Skill skill, MapleMonster monster, MapleMonsterStats mobstats, PlayerStats stats, Element elem, Integer sharpEye, double maxDamagePerMonster, MapleStatEffect attackEffect) {
        int dLevel = Math.max(mobstats.getLevel() - chr.getLevel(), 0) * 2;
        int HitRate = Math.min((int) Math.floor(Math.sqrt(stats.getAccuracy())) - (int) Math.floor(Math.sqrt(mobstats.getEva())) + 100, 100);
        if (dLevel > HitRate) {
            HitRate = dLevel;
        }
        HitRate -= dLevel;
        if ((HitRate <= 0) && ((!GameConstants.isBeginnerJob(skill.getId() / 10000)) || (skill.getId() % 10000 != 1000))) {
            return 0.0D;
        }

        int CritPercent = sharpEye;
        ElementalEffectiveness ee = monster.getEffectiveness(elem);
        double elemMaxDamagePerMob;
        switch (ee) {
            case IMMUNE:
                elemMaxDamagePerMob = 1.0D;
                break;
            default:
                elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster * ee.getValue(), stats);
        }

        int MDRate = monster.getStats().getMDRate();
        MonsterStatusEffect pdr = monster.getBuff(MonsterStatus.MDEF);
        if (pdr != null) {
            MDRate += pdr.getX();
        }
        elemMaxDamagePerMob -= elemMaxDamagePerMob * (Math.max(MDRate - stats.ignoreTargetDEF - attackEffect.getIgnoreMob(), 0) / 100.0D);

        elemMaxDamagePerMob += elemMaxDamagePerMob / 100.0D * CritPercent;

        elemMaxDamagePerMob *= (chr.getStat().dam_r) / 100.0D;
        MonsterStatusEffect imprint = monster.getBuff(MonsterStatus.IMPRINT);
        if (imprint != null) {
            elemMaxDamagePerMob += elemMaxDamagePerMob * imprint.getX() / 100.0D;
        }
        elemMaxDamagePerMob += elemMaxDamagePerMob * chr.getDamageIncrease(monster.getObjectId()) / 100.0D;
        if (GameConstants.isBeginnerJob(skill.getId() / 10000)) {
            switch (skill.getId() % 10000) {
                case 1000:
                    elemMaxDamagePerMob = 40.0D;
                    break;
                case 1020:
                    elemMaxDamagePerMob = 1.0D;
                    break;
                case 1009:
                    elemMaxDamagePerMob = monster.getStats().isBoss() ? monster.getMobMaxHp() / 30L * 100L : monster.getMobMaxHp();
            }
        }

        switch (skill.getId()) {
            case 32001000:
            case 32101000:
            case 32111002:
            case 32121002:
                elemMaxDamagePerMob *= 1.5D;
        }

        if (elemMaxDamagePerMob > 999999.0D) {
            elemMaxDamagePerMob = 999999.0D;
        } else if (elemMaxDamagePerMob <= 0.0D) {
            elemMaxDamagePerMob = 1.0D;
        }

        return elemMaxDamagePerMob;
    }

    private static double ElementalStaffAttackBonus(Element elem, double elemMaxDamagePerMob, PlayerStats stats) {
        switch (elem) {
            case FIRE:
                return elemMaxDamagePerMob / 100.0D * (stats.element_fire + stats.getElementBoost(elem));
            case ICE:
                return elemMaxDamagePerMob / 100.0D * (stats.element_ice + stats.getElementBoost(elem));
            case LIGHTING:
                return elemMaxDamagePerMob / 100.0D * (stats.element_light + stats.getElementBoost(elem));
            case POISON:
                return elemMaxDamagePerMob / 100.0D * (stats.element_psn + stats.getElementBoost(elem));
        }
        return elemMaxDamagePerMob / 100.0D * (stats.def + stats.getElementBoost(elem));
    }

    private static void handlePickPocket(MapleCharacter player, MapleMonster mob, AttackPair oned) {
        int maxmeso = player.getBuffedValue(MapleBuffStat.PICKPOCKET);

        for (Pair eachde : oned.attack) {
            Integer eachd = (Integer) eachde.left;
            if ((player.getStat().pickRate >= 100) || (Randomizer.nextInt(99) < player.getStat().pickRate)) {
                player.getMap().spawnMesoDrop(Math.min((int) Math.max(eachd / 20000.0D * maxmeso, 1.0D), maxmeso), new Point((int) (mob.getTruePosition().getX() + Randomizer.nextInt(100) - 50.0D), (int) mob.getTruePosition().getY()), mob, player, false, (byte) 0);
            }
        }
    }

    private static double CalculateMaxWeaponDamagePerHit(MapleCharacter player, MapleMonster monster, AttackInfo attack, Skill theSkill, MapleStatEffect attackEffect, double maximumDamageToMonster, Integer CriticalDamagePercent) {
        int dLevel = Math.max(monster.getStats().getLevel() - player.getLevel(), 0) * 2;
        int HitRate = Math.min((int) Math.floor(Math.sqrt(player.getStat().getAccuracy())) - (int) Math.floor(Math.sqrt(monster.getStats().getEva())) + 100, 100);
        if (dLevel > HitRate) {
            HitRate = dLevel;
        }
        HitRate -= dLevel;
        if ((HitRate <= 0) && ((!GameConstants.isBeginnerJob(attack.skill / 10000)) || (attack.skill % 10000 != 1000)) && (!GameConstants.isPyramidSkill(attack.skill)) && (!GameConstants.isMulungSkill(attack.skill)) && (!GameConstants.isInflationSkill(attack.skill))) {
            return 0.0D;
        }
        if ((player.getMapId() / 1000000 == 914) || (player.getMapId() / 1000000 == 927)) {
            return 999999.0D;
        }

        List<Element> elements = new ArrayList();
        boolean defined = false;
        int CritPercent = CriticalDamagePercent;
        int PDRate = monster.getStats().getPDRate();
        MonsterStatusEffect pdr = monster.getBuff(MonsterStatus.WDEF);
        if (pdr != null) {
            PDRate += pdr.getX();
        }
        if (theSkill != null) {
            elements.add(theSkill.getElement());
            if (GameConstants.isBeginnerJob(theSkill.getId() / 10000)) {
                switch (theSkill.getId() % 10000) {
                    case 1000:
                        maximumDamageToMonster = 40.0D;
                        defined = true;
                        break;
                    case 1020:
                        maximumDamageToMonster = 1.0D;
                        defined = true;
                        break;
                    case 1009:
                        maximumDamageToMonster = monster.getStats().isBoss() ? monster.getMobMaxHp() / 30L * 100L : monster.getMobMaxHp();
                        defined = true;
                }
            }

            switch (theSkill.getId()) {
                case 1311005:
                    PDRate = monster.getStats().isBoss() ? PDRate : 0;
                    break;
                case 3221001:
                case 33101001:
                    maximumDamageToMonster *= attackEffect.getMobCount();
                    defined = true;
                    break;
                // case 3101005:
                //     defined = true;
                //     break;
                case 32001000:
                case 32101000:
                case 32111002:
                case 32121002:
                    maximumDamageToMonster *= 1.5D;
                    break;
                case 5121007:
                    maximumDamageToMonster *= 2.0D;
                    break;
                case 21101003:
                    maximumDamageToMonster *= 3.0D;
                    break;
                //case 1221009:
                case 3221007:
                case 4331003:
                case 23121003:
                    if (!monster.getStats().isBoss()) {
                        maximumDamageToMonster = monster.getMobMaxHp();
                        defined = true;
                    }
                    break;
                case 4211006:
                    maximumDamageToMonster = 100000.0D;
                    break;
                case 1221011:
                case 21120006:
                    maximumDamageToMonster = monster.getStats().isBoss() ? 500000.0D : monster.getHp() - 1L;
                    defined = true;
                    break;
                case 3211006:
                    if (monster.getStatusSourceID(MonsterStatus.FREEZE) == 3211003) {
                        defined = true;
                        maximumDamageToMonster = 999999.0D;
                    }
                    break;
            }
        }
        double elementalMaxDamagePerMonster = maximumDamageToMonster;
        if ((player.getJob() == 311) || (player.getJob() == 312) || (player.getJob() == 321) || (player.getJob() == 322)) {
            Skill mortal = SkillFactory.getSkill((player.getJob() == 311) || (player.getJob() == 312) ? 3110001 : 3210001);
            if (player.getTotalSkillLevel(mortal) > 0) {
                MapleStatEffect mort = mortal.getEffect(player.getTotalSkillLevel(mortal));
                if ((mort != null) && (monster.getHPPercent() < mort.getX())) {
                    elementalMaxDamagePerMonster = 999999.0D;
                    defined = true;
                    if (mort.getZ() > 0) {
                        player.addHP(player.getStat().getMaxHp() * mort.getZ() / 100);
                    }
                }
            }
        } else if ((player.getJob() == 221) || (player.getJob() == 222)) {
            Skill mortal = SkillFactory.getSkill(2210000);
            if (player.getTotalSkillLevel(mortal) > 0) {
                MapleStatEffect mort = mortal.getEffect(player.getTotalSkillLevel(mortal));
                if ((mort != null) && (monster.getHPPercent() < mort.getX())) {
                    elementalMaxDamagePerMonster = 999999.0D;
                    defined = true;
                }
            }
        }
        if ((!defined) || ((theSkill != null) && ((theSkill.getId() == 33101001) || (theSkill.getId() == 3221001)))) {
            if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
                int chargeSkillId = player.getBuffSource(MapleBuffStat.WK_CHARGE);

                switch (chargeSkillId) {
                    case 1211003: //烈焰之劍
                    case 1211004: //烈焰之棍
                        elements.add(Element.FIRE);
                        break;
                    case 1211005: //寒冰之劍
                    case 1211006: //寒冰之棍
                    case 21111005:
                        elements.add(Element.ICE);
                        break;
                    case 1211007: //雷鳴之劍
                    case 1211008: //雷鳴之棍
                    case 15101006:
                        elements.add(Element.LIGHTING);
                        break;
                    case 1221003: //聖靈之劍
                    case 1221004: //聖靈之棍
                    case 11111007:
                        elements.add(Element.HOLY);
                        break;
                    case 12101005:
                }

            }

            if (player.getBuffedValue(MapleBuffStat.LIGHTNING_CHARGE) != null) {
                elements.add(Element.LIGHTING);
            }
            if (player.getBuffedValue(MapleBuffStat.ELEMENT_RESET) != null) {
                elements.clear();
            }
            double elementalEffect;
            if (elements.size() > 0) {
                switch (attack.skill) {
                    case 3111003:
                    case 3211003:
                        elementalEffect = attackEffect.getX() / 100.0D;
                        break;
                    default:
                        elementalEffect = 0.5D / elements.size();
                }

                for (Element element : elements) {
                    switch (monster.getEffectiveness(element)) {
                        case IMMUNE:
                            elementalMaxDamagePerMonster = 1.0D;
                            break;
                        case WEAK:
                            elementalMaxDamagePerMonster *= (1.0D + elementalEffect + player.getStat().getElementBoost(element));
                            break;
                        case STRONG:
                            elementalMaxDamagePerMonster *= (1.0D - elementalEffect - player.getStat().getElementBoost(element));
                    }

                }

            }

            elementalMaxDamagePerMonster -= elementalMaxDamagePerMonster * (Math.max(PDRate - Math.max(player.getStat().ignoreTargetDEF, 0) - Math.max(attackEffect == null ? 0 : attackEffect.getIgnoreMob(), 0), 0) / 100.0D);

            elementalMaxDamagePerMonster += elementalMaxDamagePerMonster / 100.0D * CritPercent;

            MonsterStatusEffect imprint = monster.getBuff(MonsterStatus.IMPRINT);
            if (imprint != null) {
                elementalMaxDamagePerMonster += elementalMaxDamagePerMonster * imprint.getX() / 100.0D;
            }

            elementalMaxDamagePerMonster += elementalMaxDamagePerMonster * player.getDamageIncrease(monster.getObjectId()) / 100.0D;
            elementalMaxDamagePerMonster *= player.getStat().dam_r / 100.0D;
        }
        if (elementalMaxDamagePerMonster > 999999.0D) {
            if (!defined) {
                elementalMaxDamagePerMonster = 999999.0D;
            }
        } else if (elementalMaxDamagePerMonster <= 0.0D) {
            elementalMaxDamagePerMonster = 1.0D;
        }
        return elementalMaxDamagePerMonster;
    }

    public static final AttackInfo DivideAttack(final AttackInfo attack, final int rate) {
        attack.real = false;
        if (rate <= 1) {
            return attack; //lol
        }
        for (AttackPair p : attack.allDamage) {
            if (p.attack != null) {
                for (Pair<Integer, Boolean> eachd : p.attack) {
                    eachd.left /= rate; //too ex.
                }
            }
        }
        return attack;
    }

    public static final AttackInfo Modify_AttackCrit(AttackInfo attack, MapleCharacter chr, int type, MapleStatEffect effect) {
        int CriticalRate;
        boolean shadow;
        List damages;
        List damage;
        if ((attack.skill != 4211006) && (attack.skill != 3211003) && (attack.skill != 4111004)) {
            CriticalRate = chr.getStat().passive_sharpeye_rate() + (effect == null ? 0 : effect.getCr());
            shadow = (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) && ((type == 1) || (type == 2));
            damages = new ArrayList();
            damage = new ArrayList();

            for (AttackPair p : attack.allDamage) {
                if (p.attack != null) {
                    int hit = 0;
                    int mid_att = shadow ? p.attack.size() / 2 : p.attack.size();

                    int toCrit = (attack.skill == 4221001) || (attack.skill == 3221007) || (attack.skill == 23121003) || (attack.skill == 4341005) || (attack.skill == 4331006) || (attack.skill == 21120005) ? mid_att : 0;
                    if (toCrit == 0) {
                        for (Pair eachd : p.attack) {
                            if ((!((Boolean) eachd.right)) && (hit < mid_att)) {
                                if ((((Integer) eachd.left) > 999999) || (Randomizer.nextInt(100) < CriticalRate)) {
                                    toCrit++;
                                }
                                damage.add(eachd.left);
                            }
                            hit++;
                        }
                        if (toCrit == 0) {
                            damage.clear();
                        } else {
                            Collections.sort(damage);
                            for (int i = damage.size(); i > damage.size() - toCrit; i--) {
                                damages.add(damage.get(i - 1));
                            }
                            damage.clear();
                        }
                    } //else {
                    hit = 0;
                    for (Pair eachd : p.attack) {
                        if (!((Boolean) eachd.right)) {
                            if (attack.skill == 4221001) {
                                eachd.right = hit == 3;
                            } else if ((attack.skill == 3221007) || (attack.skill == 23121003) || (attack.skill == 21120005) || (attack.skill == 4341005) || (attack.skill == 4331006) || (((Integer) eachd.left) > 999999)) {
                                eachd.right = true;
                            } else if (hit >= mid_att) {
                                eachd.right = ((Pair) p.attack.get(hit - mid_att)).right;
                            } else {
                                eachd.right = damages.contains(eachd.left);
                            }
                        }
                        hit++;
                    }
                    damages.clear();
                    //}
                }
            }
        }
        return attack;
    }

    public static final AttackInfo parseDmgMa(final LittleEndianAccessor lea, final MapleCharacter chr) {
        //System.out.println("parseDmgMa..");
        final AttackInfo ret = new AttackInfo();

        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();
        ret.targets = (byte) ((ret.tbyte >>> 4) & 0xF);
        ret.hits = (byte) (ret.tbyte & 0xF);
        lea.skip(8); //?
        ret.skill = lea.readInt();
        if (ret.skill >= 91000000 && ret.skill >= 200000000) { //guild/recipe? no
            //System.out.println("Return 1");
            return null;
        }
        lea.skip(12); // ORDER [1] byte on bigbang, [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82
        if (GameConstants.isMagicChargeSkill(ret.skill)) {
            ret.charge = lea.readInt();
        } else {
            ret.charge = -1;
        }
        ret.unk = lea.readByte();
        ret.display = lea.readUShort();
        lea.skip(1); // Weapon class
        ret.speed = lea.readByte(); // Confirmed
        ret.lastAttackTickCount = lea.readInt(); // Ticks

        int damage, oid;
        List<Pair<Integer, Boolean>> allDamageNumbers;
        ret.allDamage = new ArrayList<AttackPair>();

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();

            lea.skip(14); // [1] Always 6?, [3] unk, [4] Pos1, [4] Pos2, [2] seems to change randomly for some attack

            allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();

            for (int j = 0; j < ret.hits; j++) {
                damage = lea.readInt();
                allDamageNumbers.add(new Pair<Integer, Boolean>(Integer.valueOf(damage), false));
                //System.out.println("parseDmgMa Damage: " + damage);
            }
            lea.skip(4); // CRC of monster [Wz Editing]
            ret.allDamage.add(new AttackPair(Integer.valueOf(oid), allDamageNumbers));
        }
        ret.position = lea.readPos();
        return ret;
    }

    public static AttackInfo parseDmgMa1(LittleEndianAccessor lea, MapleCharacter chr) // magic
    {
        AttackInfo ret = new AttackInfo();
        lea.skip(1);
        ret.tbyte = lea.readByte();

        ret.targets = ((byte) (ret.tbyte >>> 4 & 0xF));
        ret.hits = ((byte) (ret.tbyte & 0xF));
        ret.skill = lea.readInt();
        if (ret.skill >= 91000000 && ret.skill >= 200000000) {
            return null;
        }

        lea.skip(1); // 1
        lea.readInt(); // 5
        lea.readShort(); // 7 
        if (GameConstants.isMagicChargeSkill(ret.skill)) {
            ret.charge = lea.readInt();
        } else {
            ret.charge = -1;
        }
        ret.unk = lea.readByte();
        ret.display = lea.readUShort();

        lea.skip(4);
        ret.speed = lea.readByte();
        ret.lastAttackTickCount = lea.readInt(); // FD 53 13 07
        lea.skip(4); //

        int damage, oid;
        List<Pair<Integer, Boolean>> allDamageNumbers;
        ret.allDamage = new ArrayList<>();

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();
            lea.skip(20);
            allDamageNumbers = new ArrayList<>();

            for (int j = 0; j < ret.hits; j++) {
                damage = lea.readInt();
                allDamageNumbers.add(new Pair<>(damage, false));
            }
            lea.skip(8);
            //lea.readInt();
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();
        lea.skip(1); // v146 ?
        return ret;
    }

    public static AttackInfo parseDmgM(LittleEndianAccessor lea, MapleCharacter chr)//reg att
    {
        AttackInfo ret = new AttackInfo();
        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();

        ret.targets = ((byte) (ret.tbyte >>> 4 & 0xF));
        ret.hits = ((byte) (ret.tbyte & 0xF));
        lea.skip(8);
        ret.skill = lea.readInt();
        lea.skip(12); // ORDER [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82
        switch (ret.skill) {
            case 1311011:// La Mancha Spear
            case 2221012:
            case 4341002:
            case 4341003:
            case 4221052:
            case 5300007:
            case 5301001:
            case 11121052:// Styx Crossing
            case 11121055:// Styx Crossing charged
            case 24121000:
            case 24121005:
            case 27101202:
            case 27111100:
            case 27120211:
            case 27121201:
            case 31001000:
            case 31101000:
            case 31111005:
            case 36121000:
            case 36101001:
            case 42120003: // Monkey Spirits
            case 61111100:
            case 61111111:
            case 61111113:
            case 65121003:
            case 65121052:// Supreme Supernova
            case 101110101:
            case 101110102:
            case 101110104:
            case 101120200:
            case 101120203:
            case 101120205:
            case 5101004:
            case 15101003:
            case 5201002:
            case 14111006:
                ret.charge = lea.readInt();
                break;
            default:
                ret.charge = 0;
        }

        ret.unk = lea.readByte();
        ret.display = lea.readUShort();
        lea.skip(1); // Weapon class
        ret.speed = lea.readByte();
        ret.lastAttackTickCount = lea.readInt();

        int damage, oid;
        List<Pair<Integer, Boolean>> allDamageNumbers;
        ret.allDamage = new ArrayList<>();
        if (ret.skill == 4211006) {
            return parseExplosionAttack(lea, ret, chr);
        }

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();
            lea.skip(14); // [1] Always 6?, [3] unk, [4] Pos1, [4] Pos2, [2] seems to change randomly for some attack
            allDamageNumbers = new ArrayList<>();

            for (int j = 0; j < ret.hits; j++) {
                damage = lea.readInt();
                allDamageNumbers.add(new Pair<>(damage, false));
            }
            lea.skip(4); // CRC of monster [Wz Editing]
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();
        if (ret.skill == 14111006) {
            ret.positionxy = lea.readPos();
        }
        return ret;
    }

    public static AttackInfo parseDmgR(LittleEndianAccessor lea, MapleCharacter chr)//ranged att
    {
        AttackInfo ret = new AttackInfo();
        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();

        ret.targets = ((byte) (ret.tbyte >>> 4 & 0xF));
        ret.hits = ((byte) (ret.tbyte & 0xF));
        lea.skip(8);
        ret.skill = lea.readInt();
        lea.skip(12); // ORDER [4] bytes on v.79, [4] bytes on v.80, [1] byte on v.82
        switch (ret.skill) {
            case 3121004:
            case 3221001:
            case 5321052:
            case 5221004:
            case 5311002:
            case 5711002:
            case 5721001:
            case 13111002://Hurricane
            case 13111020://Sentient Arrow
            case 13121001://Song of Heaven
            case 23121000:
            case 24121000:
            case 33121009:
            case 35001001:
            case 35101009:
            case 60011216://Soul Buster
            case 3101008:
            case 3111009:// Hurricane
            case 3121013:// Arrow Blaster
//            case 5221022:
                lea.skip(4);
                break;
        }

        ret.charge = -1;
        ret.unk = lea.readByte();
        ret.display = lea.readUShort();
        lea.skip(1); // Weapon class
        ret.speed = lea.readByte();
        ret.lastAttackTickCount = lea.readInt();
        ret.slot = ((byte) lea.readShort());
        ret.csstar = ((byte) lea.readShort());
        ret.AOE = lea.readByte();
        ret.allDamage = new ArrayList<>();

        int damage, oid;
        List<Pair<Integer, Boolean>> allDamageNumbers;
        ret.allDamage = new ArrayList<>();

        for (int i = 0; i < ret.targets; i++) {
            oid = lea.readInt();
            lea.skip(14); // [1] Always 6?, [3] unk, [4] Pos1, [4] Pos2, [2] seems to change randomly for some attack
            allDamageNumbers = new ArrayList<>();

            for (int j = 0; j < ret.hits; j++) {
                damage = lea.readInt();
                allDamageNumbers.add(new Pair<>(damage, false));
            }
            lea.skip(4); // CRC of monster [Wz Editing]
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        lea.skip(4);
        ret.position = lea.readPos();
        return ret;
    }

    public static AttackInfo parseExplosionAttack(LittleEndianAccessor lea, AttackInfo ret, MapleCharacter chr) {
        if (ret.hits == 0) {
            lea.skip(4);
            byte bullets = lea.readByte();
            for (int j = 0; j < bullets; j++) {
                ret.allDamage.add(new AttackPair(lea.readInt(), null));
                lea.skip(1);
            }
            lea.skip(2);
            return ret;
        }

        for (int i = 0; i < ret.targets; i++) {
            int oid = lea.readInt();

            lea.skip(12);
            byte bullets = lea.readByte();
            List allDamageNumbers = new ArrayList();
            for (int j = 0; j < bullets; j++) {
                allDamageNumbers.add(new Pair(lea.readInt(), false));
            }
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
            lea.skip(4);
        }
        lea.skip(4);
        byte bullets = lea.readByte();

        for (int j = 0; j < bullets; j++) {
            ret.allDamage.add(new AttackPair(lea.readInt(), null));
            lea.skip(1);
        }
        lea.skip(2); // 8F 02/ 63 02
        return ret;
    }
    
    /*
    2021-01-21 浪子杰记录怪物伤害
    */
    public static Map<Integer, Long> 打怪伤害记录表单 = new LinkedHashMap<>();
    public static int 怪物代码 =9600074;
    
    public static void 清理伤害记录表单() {
        打怪伤害记录表单.clear();
    }
    
    public static long 读取打怪伤害(int numb) {
        int id = numb;
        if (打怪伤害记录表单.containsKey(id)) {
            return 打怪伤害记录表单.get(id);
        }
        return 0;
    }

     public static void 记录打怪伤害(int a, long b) {
        打怪伤害记录表单.put(a, 读取打怪伤害(a) + b);
    }
     public static String 打怪伤害() {
        StringBuilder sb = new StringBuilder();
        List<Map.Entry<Integer, Long>> cityInfoList = new ArrayList<>(打怪伤害记录表单.entrySet());
        Collections.sort(cityInfoList, new Comparator<Map.Entry<Integer, Long>>() {
            @Override
            public int compare(Map.Entry<Integer, Long> o1, Map.Entry<Integer, Long> o2) {
                return o2.getValue().compareTo(o1.getValue());

            }
        });
        int a = 1;
        sb.append("#d#e怪物伤害排行:#n\r\n\r\n");
        for (Map.Entry<Integer, Long> entry : cityInfoList) {
            Integer key = entry.getKey();
            Long value = entry.getValue();
            String 玩家名字 = NPCConversationManager.角色ID取名字(key);
            if (a < 10) {
                sb.append("Top.#r").append(a).append("#k  #d玩家 : #b").append(玩家名字).append("");
            } else {
                sb.append("Top.#b").append(a).append("#k  #d玩家 : #b").append(玩家名字).append("");
            }
            for (int j = 13 - 玩家名字.getBytes().length; j > 0; j--) {
                sb.append(" ");
            }
            sb.append("#d造成伤害 : #r").append(value).append("#k\r\n");
            a++;
        }
        return sb.toString();
    }

    private DamageParse() {
    }
    
    
    
    
    
}
