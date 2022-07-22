package handling.channel.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MonsterFamiliar;
import client.MonsterStatus;
import client.MonsterStatusEffect;
import client.SkillFactory;
import client.anticheat.CheatingOffense;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.world.World;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.StructFamiliar;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleNodes;
import server.movement.AbstractLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;

public class MobHandler {

    private static final Logger logger = LogManager.getLogger(MobHandler.class);

    public static final void MoveMonster(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        int oid = slea.readInt();
        MapleMonster monster = chr.getMap().getMonsterByOid(oid);
        if (monster == null) {
            return;
        }
        if (monster.getLinkCID() > 0) {
            return;
        }

        short moveid = slea.readShort();
        boolean useSkill = slea.readByte() > 0;
        byte skill = slea.readByte();
        int unk = slea.readInt();
        int realskill = 0;
        int level = 0;

        if (useSkill) {
            byte size = monster.getNoSkills();
            boolean used = false;

            if (size > 0) {
                Pair skillToUse = monster.getSkills().get((byte) Randomizer.nextInt(size));
                realskill = ((Integer) skillToUse.getLeft()).intValue();
                level = ((Integer) skillToUse.getRight()).intValue();

                MobSkill mobSkill = MobSkillFactory.getMobSkill(realskill, level);

                if ((mobSkill != null) && (!mobSkill.checkCurrentBuff(chr, monster))) {
                    long now = System.currentTimeMillis();
                    long ls = monster.getLastSkillUsed(realskill);
                    //System.out.println("技能ID：" + realskill + "aaa" + (now - ls > mobSkill.getCoolTime()) + " " + (!mobSkill.onlyOnce()));
                    if ((ls == 0L) || ((now - ls > mobSkill.getCoolTime()) && (!mobSkill.onlyOnce()))) {
                        monster.setLastSkillUsed(realskill, now, mobSkill.getCoolTime());

                        int reqHp = (int) ((float) monster.getHp() / (float) monster.getMobMaxHp() * 100.0F);
                        if (reqHp <= mobSkill.getHP()) {
                            used = true;
                            mobSkill.applyEffect(chr, monster, true);
                        }
                    }
                }
            }
            if (!used) {
                realskill = 0;
                level = 0;
            }
        }
        List unk3 = new ArrayList();
        //byte size1 = slea.readByte();
        //for (int i = 0; i < size1; i++) {
        //    unk3.add(new Pair(Integer.valueOf(slea.readShort()), Integer.valueOf(slea.readShort())));
        //}
        List unk2 = new ArrayList();
        //byte size = slea.readByte();
        //for (int i = 0; i < size; i++) {
        //    unk2.add(Integer.valueOf(slea.readShort()));
        //}
        slea.skip(1);
        int unk4 = slea.readInt(); // sometimes 0, 1
        slea.skip(4); // CC DD FF 00  same for all mobs
        slea.skip(4); // CC DD FF 00  same for all mobs
        if (unk4 == 0x12) {
            slea.readMapleAsciiString();
        }

        slea.skip(4); // original pos
        Point startPos = monster.getPosition();
        List res;
        try {
            res = MovementParse.parseMovement(slea, 2);
        } catch (ArrayIndexOutOfBoundsException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Movement_Log, e);
            FileoutputUtil.log(FileoutputUtil.Movement_Log, "MOBID " + monster.getId() + ", AIOBE Type2:\n" + slea.toString(true));
            return;
        }
        if ((GameConstants.isLuminous(chr.getJob())) && (Randomizer.nextInt(100) < 20)) {
            chr.applyBlackBlessingBuff(1);
        }

        if ((res != null) && (chr != null) && (res.size() > 0)) {
            if (monster.getController() != c.getPlayer()) {
                if (monster.isAttackedBy(c.getPlayer())) {// aggro and controller change
                    monster.switchController(c.getPlayer(), true);
                } else {
                    return;
                }
            } else if (skill == -1 && monster.isControllerKnowsAboutAggro() && !monster.isFirstAttack()) {
                monster.setControllerHasAggro(false);
                monster.setControllerKnowsAboutAggro(false);
            }
            boolean aggro = monster.isControllerHasAggro();
            if (aggro) {
                monster.setControllerKnowsAboutAggro(true);
            }
            MapleMap map = chr.getMap();
            try {

                CheckMobDirection(c, monster, res, startPos);
                CheckMobVac(c, monster, res, startPos);
                CheckMobVac_x(c, monster, res, startPos);
            } catch (Exception ex) {

            }

            c.getSession().write(MobPacket.moveMonsterResponse(monster.getObjectId(), moveid, monster.getMp(), monster.isControllerHasAggro(), realskill, level));
            if (slea.available() != 8) {
                System.err.println("Move_life : slea.available != 8");
                return;
            }
            MovementParse.updatePosition(res, monster, -1);
            Point endPos = monster.getTruePosition();
            map.moveMonster(monster, endPos);
            map.broadcastMessage(chr, MobPacket.moveMonster(useSkill, skill, unk, monster.getObjectId(), startPos, res, unk2, unk3), endPos);
            chr.getCheatTracker().checkMoveMonster(endPos);
        }
    }

    public static final void FriendlyDamage(LittleEndianAccessor slea, MapleCharacter chr) {
        MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        MapleMonster mobfrom = map.getMonsterByOid(slea.readInt());
        //int damage = slea.readInt();
        slea.readInt();
        MapleMonster mobto = map.getMonsterByOid(slea.readInt());

        if ((mobfrom != null) && (mobto != null) && (mobto.getStats().isFriendly())) {
            int damage = mobto.getStats().getLevel() * Randomizer.nextInt(mobto.getStats().getLevel()) / 2;
            mobto.damage(chr, damage, true);
            checkShammos(chr, mobto, map);
        }
    }

    @SuppressWarnings("empty-statement")
    public static final void MobBomb(LittleEndianAccessor slea, MapleCharacter chr) {
        MapleMap map = chr.getMap();
        if (map == null) {
            return;
        }
        MapleMonster mobfrom = map.getMonsterByOid(slea.readInt());
        slea.skip(4);
        slea.readInt();

        if ((mobfrom != null) && (mobfrom.getBuff(MonsterStatus.MONSTER_BOMB) != null));
    }

    public static final void checkShammos(MapleCharacter chr, MapleMonster mobto, MapleMap map) {
        MapleMap mapp;
        if ((!mobto.isAlive()) && (mobto.getStats().isEscort())) {
            for (MapleCharacter chrz : map.getCharactersThreadsafe()) {
                if ((chrz.getParty() != null) && (chrz.getParty().getLeader().getId() == chrz.getId())) {
                    if (!chrz.haveItem(2022698)) {
                        break;
                    }
                    MapleInventoryManipulator.removeById(chrz.getClient(), MapleInventoryType.USE, 2022698, 1, false, true);
                    mobto.heal((int) mobto.getMobMaxHp(), mobto.getMobMaxMp(), true);
                    return;
                }

            }

            map.broadcastMessage(CWvsContext.broadcastMsg(6, "Your party has failed to protect the monster."));
            mapp = chr.getMap().getForcedReturnMap();
            for (MapleCharacter chrz : map.getCharactersThreadsafe()) {
                chrz.changeMap(mapp, mapp.getPortal(0));
            }
        } else if ((mobto.getStats().isEscort()) && (mobto.getEventInstance() != null)) {
            mobto.getEventInstance().setProperty("HP", String.valueOf(mobto.getHp()));
        }
    }

    public static final void MonsterBomb(int oid, MapleCharacter chr) {
        MapleMonster monster = chr.getMap().getMonsterByOid(oid);

        if ((monster == null) || (!chr.isAlive()) || (chr.isHidden()) || (monster.getLinkCID() > 0)) {
            return;
        }
        byte selfd = monster.getStats().getSelfD();
        if (selfd != -1) {
            chr.getMap().killMonster(monster, chr, false, false, selfd);
        }
    }

    public static final void AutoAggro(int monsteroid, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null) || (chr.isHidden())) {
            return;
        }
        MapleMonster monster = chr.getMap().getMonsterByOid(monsteroid);

        if ((monster != null) && (chr.getTruePosition().distanceSq(monster.getTruePosition()) < 200000.0D) && (monster.getLinkCID() <= 0)) {
            if (monster.getController() != null) {
                if (chr.getMap().getCharacterById(monster.getController().getId()) == null) {
                    monster.switchController(chr, true);
                } else {
                    monster.switchController(monster.getController(), true);
                }
            } else {
                monster.switchController(chr, true);
            }
        }
    }

    public static final void HypnotizeDmg(LittleEndianAccessor slea, MapleCharacter chr) {
        MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt());
        slea.skip(4);
        int to = slea.readInt();
        slea.skip(1);
        int damage = slea.readInt();

        MapleMonster mob_to = chr.getMap().getMonsterByOid(to);

        if ((mob_from != null) && (mob_to != null) && (mob_to.getStats().isFriendly())) {
            if (damage > 30000) {
                return;
            }
            mob_to.damage(chr, damage, true);
            checkShammos(chr, mob_to, chr.getMap());
        }
    }

    public static final void DisplayNode(LittleEndianAccessor slea, MapleCharacter chr) {
        MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt());
        if (mob_from != null) {
            chr.getClient().getSession().write(MobPacket.getNodeProperties(mob_from, chr.getMap()));
        }
    }

    public static final void MobNode(LittleEndianAccessor slea, MapleCharacter chr) {
        MapleMonster mob_from = chr.getMap().getMonsterByOid(slea.readInt());
        int newNode = slea.readInt();
        int nodeSize = chr.getMap().getNodes().size();
        if ((mob_from != null) && (nodeSize > 0)) {
            MapleNodes.MapleNodeInfo mni = chr.getMap().getNode(newNode);
            if (mni == null) {
                return;
            }
            if (mni.attr == 2) {
                switch (chr.getMapId() / 100) {
                    case 9211200:
                    case 9211201:
                    case 9211202:
                    case 9211203:
                    case 9211204:
                        chr.getMap().talkMonster("Please escort me carefully.", 5120035, mob_from.getObjectId());
                        break;
                    case 9320001:
                    case 9320002:
                    case 9320003:
                        chr.getMap().talkMonster("Please escort me carefully.", 5120051, mob_from.getObjectId());
                }
            }

            mob_from.setLastNode(newNode);
            if (chr.getMap().isLastNode(newNode)) {
                switch (chr.getMapId() / 100) {
                    case 9211200:
                    case 9211201:
                    case 9211202:
                    case 9211203:
                    case 9211204:
                    case 9320001:
                    case 9320002:
                    case 9320003:
                        chr.getMap().broadcastMessage(CWvsContext.broadcastMsg(5, "Proceed to the next stage."));
                        chr.getMap().removeMonster(mob_from);
                }
            }
        }
    }

    public static final void RenameFamiliar(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        MonsterFamiliar mf = c.getPlayer().getFamiliars().get(Integer.valueOf(slea.readInt()));
        String newName = slea.readMapleAsciiString();
        if ((mf != null) && (mf.getName().equals(mf.getOriginalName())) && (MapleCharacterUtil.isEligibleCharName(newName, false))) {
            mf.setName(newName);
            c.getSession().write(CField.renameFamiliar(mf));
        } else {
            chr.dropMessage(1, "姓名不合格.");
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void SpawnFamiliar(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        c.getPlayer().updateTick(slea.readInt());
        int mId = slea.readInt();
        c.getSession().write(CWvsContext.enableActions());
        c.getPlayer().removeFamiliar();
        if ((c.getPlayer().getFamiliars().containsKey(Integer.valueOf(mId))) && (slea.readByte() > 0)) {
            MonsterFamiliar mf = c.getPlayer().getFamiliars().get(Integer.valueOf(mId));
            if (mf.getFatigue() > 0) {
                c.getPlayer().dropMessage(1, "请等待 " + mf.getFatigue() + " 秒召唤它.");
            } else {
                c.getPlayer().spawnFamiliar(mf, false);
            }
        }
    }

    public static final void MoveFamiliar(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.skip(17);
        List res = MovementParse.parseMovement(slea, 6);
        if ((chr != null) && (chr.getSummonedFamiliar() != null) && (res.size() > 0)) {
            Point pos = chr.getSummonedFamiliar().getPosition();
            MovementParse.updatePosition(res, chr.getSummonedFamiliar(), 0);
            chr.getSummonedFamiliar().updatePosition(res);
            if (!chr.isHidden()) {
                chr.getMap().broadcastMessage(chr, CField.moveFamiliar(chr.getId(), pos, res), chr.getTruePosition());
            }
        }
    }

    public static final void AttackFamiliar(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (chr.getSummonedFamiliar() == null) {
            return;
        }
        slea.skip(6);
        int skillid = slea.readInt();

        SkillFactory.FamiliarEntry f = SkillFactory.getFamiliar(skillid);
        if (f == null) {
            return;
        }
        byte unk = slea.readByte();
        byte size = slea.readByte();
        List<Triple<Integer, Integer, List<Integer>>> attackPair = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            int oid = slea.readInt();
            int type = slea.readInt();
            slea.skip(10);
            byte si = slea.readByte();
            List attack = new ArrayList(si);
            for (int x = 0; x < si; x++) {
                attack.add(Integer.valueOf(slea.readInt()));
            }
            attackPair.add(new Triple(Integer.valueOf(oid), Integer.valueOf(type), attack));
        }
        if ((attackPair.isEmpty()) || (!chr.getCheatTracker().checkFamiliarAttack(chr)) || (attackPair.size() > f.targetCount)) {
            return;
        }
        MapleMonsterStats oStats = chr.getSummonedFamiliar().getOriginalStats();
        chr.getMap().broadcastMessage(chr, CField.familiarAttack(chr.getId(), unk, attackPair), chr.getTruePosition());
        for (Triple attack : attackPair) {
            MapleMonster mons = chr.getMap().getMonsterByOid(((Integer) attack.left).intValue());
            if ((mons != null) && (mons.isAlive()) && (!mons.getStats().isFriendly()) && (mons.getLinkCID() <= 0) && (((List) attack.right).size() <= f.attackCount)) {
                if ((chr.getTruePosition().distanceSq(mons.getTruePosition()) > 640000.0D) || (chr.getSummonedFamiliar().getTruePosition().distanceSq(mons.getTruePosition()) > GameConstants.getAttackRange(f.lt, f.rb))) {
                    chr.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER_SUMMON);
                }
                for (Iterator i$ = ((List) attack.right).iterator(); i$.hasNext();) {
                    int damage = ((Integer) i$.next()).intValue();
                    if (damage <= oStats.getPhysicalAttack() * 4) {
                        mons.damage(chr, damage, true);
                    }
                }
                if ((f.makeChanceResult()) && (mons.isAlive())) {
                    for (MonsterStatus s : f.status) {
                        mons.applyStatus(chr, new MonsterStatusEffect(s, Integer.valueOf(f.speed), MonsterStatusEffect.genericSkill(s), null, false), false, f.time * 1000, false, null);
                    }
                    if (f.knockback) {
                        mons.switchController(chr, true);
                    }
                }
            }
        }
        chr.getSummonedFamiliar().addFatigue(chr, attackPair.size());
    }

    public static final void TouchFamiliar(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (chr.getSummonedFamiliar() == null) {
            return;
        }
        slea.skip(6);
        byte unk = slea.readByte();

        MapleMonster target = chr.getMap().getMonsterByOid(slea.readInt());
        if (target == null) {
            return;
        }
        int type = slea.readInt();
        slea.skip(4);
        int damage = slea.readInt();
        int maxDamage = chr.getSummonedFamiliar().getOriginalStats().getPhysicalAttack() * 5;
        if (damage < maxDamage) {
            damage = maxDamage;
        }
        if ((!target.getStats().isFriendly()) && (chr.getCheatTracker().checkFamiliarAttack(chr))) {
            chr.getMap().broadcastMessage(chr, CField.touchFamiliar(chr.getId(), unk, target.getObjectId(), type, 600, damage), chr.getTruePosition());
            target.damage(chr, damage, true);
            chr.getSummonedFamiliar().addFatigue(chr);
        }
    }

    public static final void UseFamiliar(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null) || (chr.hasBlockedInventory())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

        c.getSession().write(CWvsContext.enableActions());
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId) || (itemId / 10000 != 287)) {
            return;
        }
        StructFamiliar f = MapleItemInformationProvider.getInstance().getFamiliarByItem(itemId);
        if (MapleLifeFactory.getMonsterStats(f.mob).getLevel() <= c.getPlayer().getLevel()) {
            MonsterFamiliar mf = c.getPlayer().getFamiliars().get(Integer.valueOf(f.familiar));
            if (mf != null) {
                if (mf.getVitality() >= 3) {
                    mf.setExpiry(Math.min(System.currentTimeMillis() + 7776000000L, mf.getExpiry() + 2592000000L));
                } else {
                    mf.setVitality(mf.getVitality() + 1);
                    mf.setExpiry(mf.getExpiry() + 2592000000L);
                }
            } else {
                mf = new MonsterFamiliar(c.getPlayer().getId(), f.familiar, System.currentTimeMillis() + 2592000000L);
                c.getPlayer().getFamiliars().put(Integer.valueOf(f.familiar), mf);
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false, false);
            c.getSession().write(CField.registerFamiliar(mf));
            return;
        }
    }

    public static void CheckMobVac(MapleClient c, MapleMonster monster, List<LifeMovementFragment> res, Point startPos) {
        MapleCharacter chr = c.getPlayer();
        try {
            boolean fly = monster.getStats().getFly();
            Point endPos = null;
            int reduce_x = 0;
            int reduce_y = 0;
            for (LifeMovementFragment move : res) {
                if ((move instanceof AbstractLifeMovement)) {
                    endPos = ((LifeMovement) move).getPosition();
                    //System.out.println(startPos+ " || " + endPos);
                    try {
                        reduce_x = Math.abs(startPos.x - endPos.x);
                        reduce_y = Math.abs(startPos.y - endPos.y);
                    } catch (Exception ex) {
                    }
                }
            }

            if (!fly) {
                int GeneallyDistance_y = 150;
                int GeneallyDistance_x = 200;
                int Check_x = 250;
                int max_x = 450;
                switch (chr.getMapId()) {
                    case 100040001:
                        GeneallyDistance_y = 200;
                        break;
                    case 926013100:
                    case 926013200:
                    case 926013300:
                    case 926013400:
                    case 926013500:
                        GeneallyDistance_y = 300;
                        break;
                    case 200010300:
                        GeneallyDistance_x = 1000;
                        GeneallyDistance_y = 500;
                        break;
                    case 220010600:
                        GeneallyDistance_x = 200;
                        break;
                    case 211040001:
                        GeneallyDistance_x = 220;
                        break;
                    case 101030001:
                    case 101030105:
                        GeneallyDistance_x = 250;
                        break;
                    case 240030200:
                    case 541020500:
                        Check_x = 300;
                        break;
                }
                switch (monster.getId()) {
                    case 4230100:
                        GeneallyDistance_y = 200;
                        break;
                    case 9410066:
                        Check_x = 1000;
                        break;
                }
                if (GeneallyDistance_x > max_x) {
                    max_x = GeneallyDistance_x;
                }
                if (((reduce_x > GeneallyDistance_x || reduce_y > GeneallyDistance_y) && reduce_y != 0) || (reduce_x > Check_x && reduce_y == 0) || reduce_x > max_x) {
                    chr.addMobVac();
                    if (c.getPlayer().getMobVac() % 50 == 0 || reduce_x > max_x) {

                       // World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + chr.getName() + " (编号: " + chr.getId() + ")使用吸怪(" + chr.getMobVac() + ")! - 地图:" + chr.getMapId() + "(" + chr.getMap().getMapName() + ")"));
                        StringBuilder sb = new StringBuilder();
                        sb.append("\r\n");
                        sb.append(FileoutputUtil.NowTime());
                        sb.append(" 玩家: ");
                        sb.append(StringUtil.getRightPaddedStr(c.getPlayer().getName(), ' ', 13));
                        sb.append("(编号:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(c.getPlayer().getId()), ' ', 5));
                        sb.append(" )怪物: ");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(monster.getId()), ' ', 7));
                        sb.append("(");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(monster.getObjectId()), ' ', 6));
                        sb.append(")");
                        sb.append(" 地图: ");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(c.getPlayer().getMapId()), ' ', 9));
                        sb.append(" 初始座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(startPos.x), ' ', 4));
                        sb.append(",");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(startPos.y), ' ', 4));
                        sb.append(" 移动座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(endPos.x), ' ', 4));
                        sb.append(",");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(endPos.y), ' ', 4));
                        sb.append(" 相差座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(reduce_x), ' ', 4));
                        sb.append(",");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(reduce_y), ' ', 4));
                        FileoutputUtil.logToFile("logs/Hack/吸怪.txt", sb.toString());
                        if (chr.hasGmLevel(1)) {
                            c.getPlayer().dropMessage(6, "触发吸怪 --  x: " + reduce_x + ", y: " + reduce_y);
                        }
                    }
                }
            }

        } catch (Exception ex) {

        }
    }

    public static void CheckMobVac_x(MapleClient c, MapleMonster monster, List<LifeMovementFragment> res, Point startPos) {
        MapleCharacter chr = c.getPlayer();
        try {
            boolean fly = monster.getStats().getFly();
            Point endPos = null;
            int reduce_x = 0;
            for (LifeMovementFragment move : res) {
                if ((move instanceof AbstractLifeMovement)) {
                    endPos = ((LifeMovement) move).getPosition();

                    try {
                        reduce_x = Math.abs(startPos.x - endPos.x);
                    } catch (Exception ex) {
                    }
                }
            }
            if (!fly) {
                int GeneallyDistance_x = 500;
                if (reduce_x > GeneallyDistance_x) {
                    chr.addMobVac_X();
                    if (c.getPlayer().getMobVac_X() % 10 == 0) {
                       // World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + chr.getName() + " (编号: " + chr.getId() + ")使用X座标吸怪(" + chr.getMobVac_X() + ")! - 地图:" + chr.getMapId() + "(" + chr.getMap().getMapName() + ")"));
                        StringBuilder sb = new StringBuilder();
                        sb.append("\r\n");
                        sb.append(FileoutputUtil.NowTime());
                        sb.append(" 玩家: ");
                        sb.append(StringUtil.getRightPaddedStr(c.getPlayer().getName(), ' ', 13));
                        sb.append("(编号:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(c.getPlayer().getId()), ' ', 5));
                        sb.append(" )怪物: ");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(monster.getId()), ' ', 7));
                        sb.append("(");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(monster.getObjectId()), ' ', 6));
                        sb.append(")");
                        sb.append(" 地图: ");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(c.getPlayer().getMapId()), ' ', 9));
                        sb.append(" 初始座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(startPos.x), ' ', 4));
                        sb.append(" 移动座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(endPos.x), ' ', 4));
                        sb.append(" 相差座标:");
                        sb.append(StringUtil.getRightPaddedStr(String.valueOf(reduce_x), ' ', 4));
                        FileoutputUtil.logToFile("logs/Hack/X座标吸怪.txt", sb.toString());
                        if (chr.hasGmLevel(1)) {
                            c.getPlayer().dropMessage(6, "触发吸怪 --  x: " + reduce_x);
                        }
                    }
                }
            }

        } catch (Exception ex) {

        }
    }

    public static void CheckMobDirection(MapleClient c, MapleMonster monster, List<LifeMovementFragment> res, Point startPos) {
        double moveDistance = 0;
        Point endPos = null;
        for (LifeMovementFragment move : res) {
            if ((move instanceof AbstractLifeMovement)) {
                endPos = ((LifeMovement) move).getPosition();
            }
        }
        if (endPos == null) {
            return;
        }
        switch (c.getPlayer().getMapId()) {
            case 910320100:
            case 910320200:
            case 910320300:
            case 980000301:
            case 926013100:
            case 926013200:
            case 926013300:
            case 926013400:
                return;
        }
        if (c.getPlayer().getMapId() / 100000 == 9260) {
            return;
        }
        //if (MobConstants.isMonsterSpawn(c.getPlayer().getMap()) > 1) { //判斷是否地圖上有怪物加倍生
        //    return;
        //}
        int range = 500;
        switch (monster.getId()) {
            case 9410032:// 鬼兔
                range = 700;
                break;
        }
        moveDistance = startPos.getX() - endPos.getX();
        if (moveDistance != 0.0) {
            if (moveDistance >= 0) {
                c.getPlayer().addMobDirection(1);
            } else {
                c.getPlayer().addMobDirection(-1);
            }
            if (c.getPlayer().getMobDirection() < -range || c.getPlayer().getMobDirection() > range) {
                World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[系统警告] " + c.getPlayer().getName() + " (编号: " + c.getPlayer().getId() + ")使用控怪(地图: " + c.getPlayer().getMapId() + " 频道: " + c.getPlayer().getClient().getChannel() + " 怪物方向控制值:" + c.getPlayer().getMobDirection() + ")"));
                FileoutputUtil.logToFile("logs/Hack/控怪.txt", "角色名：" + c.getPlayer().getName() + " (编号: " + c.getPlayer().getId() + ")使用控怪(地图: " + c.getPlayer().getMapId() + " 频道: " + c.getPlayer().getClient().getChannel() + " 怪物方向控制值:" + c.getPlayer().getMobDirection() + ")");
            }
        }
    }

    private MobHandler() {
    }
}
