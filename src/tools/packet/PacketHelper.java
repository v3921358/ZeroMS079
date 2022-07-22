package tools.packet;

import client.*;
import client.inventory.*;
import constants.GameConstants;
import handling.Buffstat;
import handling.world.MapleCharacterLook;
import java.util.*;
import java.util.Map.Entry;

import server.CashItem;
import server.CharacterCardFactory;
import server.MapleItemInformationProvider;
import server.shops.MapleShop;
import server.shops.MapleShopItem;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.stores.AbstractPlayerStore;
import server.stores.IMaplePlayerShop;
import tools.BitTools;
import tools.HexTool;
import tools.KoreanDateUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class PacketHelper {

    public static final long FT_UT_OFFSET = 116444592000000000L;
    public static final long MAX_TIME = 150842304000000000L;
    public static final long ZERO_TIME = 94354848000000000L;
    public static final long PERMANENT = 150841440000000000L;

    public static long getKoreanTimestamp(long realTimestamp) {
        return getTime(realTimestamp);
    }

    public static long getTime(long realTimestamp) {
        if (realTimestamp == -1L) { // 00 80 05 BB 46 E6 17 02, 1/1/2079
            return MAX_TIME;
        }
        if (realTimestamp == -2L) { // 00 40 E0 FD 3B 37 4F 01, 1/1/1900
            return ZERO_TIME;
        }
        if (realTimestamp == -3L) {
            return PERMANENT;
        }
        return (realTimestamp + (12 * 60 * 60 * 1000)) * 10000L + 116444592000000000L;
    }

    public static long decodeTime(long fakeTimestamp) {
        if (fakeTimestamp == 150842304000000000L) {
            return -1L;
        }
        if (fakeTimestamp == 94354848000000000L) {
            return -2L;
        }
        if (fakeTimestamp == 150841440000000000L) {
            return -3L;
        }
        return (fakeTimestamp - 116444592000000000L) / 10000L;
    }

    public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
        if (TimeZone.getDefault().inDaylightTime(new Date())) {
            timeStampinMillis -= 3600000L;
        }
        long time;

        if (roundToMinutes) {
            time = timeStampinMillis / 1000L / 60L * 600000000L;
        } else {
            time = timeStampinMillis * 10000L;
        }
        return time + 116444592000000000L;
    }

    public static void addImageInfo(MaplePacketLittleEndianWriter mplew, byte[] image) {
        mplew.writeInt(image.length);
        mplew.write(image);
    }

    public static void addStartedQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {

        final List<MapleQuestStatus> started = chr.getStartedQuests();
        mplew.writeShort(started.size());
        for (MapleQuestStatus q : started) {
            mplew.writeShort(q.getQuest().getId());
            if (q.hasMobKills()) {
                StringBuilder sb = new StringBuilder();
                for (final int kills : q.getMobKills().values()) {
                    sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
                }
                mplew.writeMapleAsciiString(sb.toString());
            } else {
                mplew.writeMapleAsciiString(q.getCustomData() == null ? "" : q.getCustomData());
            }
        }
        //addNXQuestInfo(mplew, chr);
    }

    public static void addNXQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(0);
        /*
         mplew.writeShort(7);
         mplew.writeMapleAsciiString("1NX5211068");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("SE20130619");
         mplew.writeMapleAsciiString("20130626060823");
         mplew.writeMapleAsciiString("99NX5533018");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("1NX1003792");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("1NX1702337");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("1NX9102857");
         mplew.writeMapleAsciiString("1");
         mplew.writeMapleAsciiString("SE20130116");
         mplew.writeMapleAsciiString("1");
         */
    }

    public static void addCompletedQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeLong(KoreanDateUtil.getFileTimestamp(q.getCompletionTime()));

        }
    }

    public static void addSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final Map<Skill, SkillEntry> skills = chr.getSkills();

        mplew.writeShort(skills.size());
        for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
            mplew.writeInt(skill.getKey().getId());
            mplew.writeInt(skill.getValue().skillevel);
            if (skill.getKey().isFourthJob()) {
                mplew.writeInt(skill.getValue().masterlevel);
            }
        }
        //mplew.write(1);
        //mplew.writeShort(0);
        /*MaplePacketLittleEndianWriter mplew1 =  new MaplePacketLittleEndianWriter();
         final Map<Skill, SkillEntry> skills = chr.getSkills();
         mplew1.write(1);
         int hyper = 0;
         //for (Skill skill : skills.keySet()) {
         //    if (skill.isHyper()) hyper++;
         //}
         mplew1.writeShort(skills.size() - hyper);
         boolean follow = false;
        
         for (Map.Entry<Skill, SkillEntry> skill : skills.entrySet()) {
         //if (((Skill) skill.getKey()).isHyper()) continue;
            
         if (follow) {
         follow = false;
         if (!GameConstants.isHyperSkill((Skill) skill.getKey()))
         mplew1.writeInt(skill.getKey().getId());
         }
         mplew1.writeInt(skill.getKey().getId());
         mplew1.writeInt(((SkillEntry) skill.getValue()).skillevel);
         addExpirationTime(mplew1, ((SkillEntry) skill.getValue()).expiration);

         if (GameConstants.isHyperSkill((Skill) skill.getKey())) {
         // mplew1.writeInt(1110009);
         follow = true;
         } else if (((Skill) skill.getKey()).isFourthJob()) {
         mplew1.writeInt(((SkillEntry) skill.getValue()).masterlevel);
         }
         //  addSingleSkill(mplew, skill.getKey(), skill.getValue());
         }
         mplew.write(mplew1.getPacket());
         System.out.println(HexTool.toString(mplew1.getPacket()));
         */
    }

    public static void addLinkSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Map<Skill, SkillEntry> skills = chr.getLinkSkills();
        mplew.writeShort(skills.size());
        for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
            //MapleCharacter chara = MapleCharacter.getCharacterById(skill.getValue().teachId);
            /*if (Arrays.binarySearch(GameConstants.CygnusLink, ((Skill) skill.getKey()).getId()) != -1) {
             can = true;
             skilllevel += chr.getTotalSkillLevel((Skill) skill.getKey());
             continue;
             }*/
            mplew.writeInt(((Skill) skill.getKey()).getId());
            mplew.writeShort(chr.getTotalSkillLevel((Skill) skill.getKey()) - 1);
        }

    }

//    public static void addSingleSkill(MaplePacketLittleEndianWriter mplew, Skill skill, SkillEntry ske) {
//        try {
//            // if (skill.getId() != 1001008) return;
//
//            MaplePacketLittleEndianWriter mplew1 = new MaplePacketLittleEndianWriter();
//
//            mplew1.writeInt(skill.getId());
//            mplew1.writeInt(ske.skillevel);
//            addExpirationTime(mplew1, ske.expiration);
//
//            if (GameConstants.isHyperSkill(skill)) {
//                //System.out.println("HYPER: " + ((Skill) skill.getKey()).getId());
//                mplew1.writeInt(0);
//            } else if (((Skill) skill).isFourthJob()) {
//                mplew1.writeInt(((SkillEntry) ske).masterlevel);
//            }
//            if (skill.getId() == 1001008) {
//                System.out.println(HexTool.toString(mplew1.getPacket()));
//            }
//            mplew.write(mplew1.getPacket());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    public static void addCoolDownInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<MapleCoolDownValueHolder> cd = chr.getCooldowns();

        mplew.writeShort(cd.size());
        for (MapleCoolDownValueHolder cooling : cd) {
            mplew.writeInt(cooling.skillId);
            mplew.writeShort((short) (cooling.length + cooling.startTime - System.currentTimeMillis()) / 1000);
        }
    }

    public static void addRocksInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int[] mapz = chr.getRegRocks();
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(mapz[i]);
        }

        int[] map = chr.getRocks();
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(map[i]);
        }

    }

    public static void addUnk400Info(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        short size = 0;
        mplew.writeShort(size);
        for (int i = 0; i < size; i++) {
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
    }

    public static void addRingInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
        List<MapleRing> cRing = aRing.getLeft();
        mplew.writeShort(cRing.size());
        for (MapleRing ring : cRing) {
            //33
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 13);
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
        }
        List<MapleRing> fRing = aRing.getMid();
        mplew.writeShort(fRing.size());
        for (MapleRing ring : fRing) {
            //
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 13);
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
            mplew.writeInt(ring.getItemId());
        }
        List<MapleRing> mRing = aRing.getRight();
        mplew.writeShort(mRing.size());
        int marriageId = 30000;
        for (MapleRing ring : mRing) {
            mplew.writeInt(marriageId);
            mplew.writeAsciiString(ring.getPartnerName(), 13);
            mplew.writeInt(chr.getId());
            mplew.writeInt(ring.getPartnerChrId());

        }
    }

    public static void addMoneyInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt((int) chr.getMeso());
    }

    public static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {

        mplew.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit());
        MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(122700));
        if ((stat != null) && (stat.getCustomData() != null) && (Long.parseLong(stat.getCustomData()) > System.currentTimeMillis())) {
            mplew.writeLong(getTime(Long.parseLong(stat.getCustomData())));
        } else {
            mplew.writeLong(getTime(-2L));
        }
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        final List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        for (Item item : equipped) {
            if ((item.getPosition() < 0) && (item.getPosition() > -100)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.write(0);//1
        for (Item item : equipped) {
            if ((item.getPosition() <= -100) && (item.getPosition() > -1000)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.write(0);//2
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);//3

        iv = chr.getInventory(MapleInventoryType.USE);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);//4
        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);//5
        iv = chr.getInventory(MapleInventoryType.ETC);
        for (Item item : iv.list()) {
            if (item.getPosition() < 100) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.write(0);//6
        iv = chr.getInventory(MapleInventoryType.CASH);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);//7

    }

    public static void addPotionPotInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {

        mplew.writeInt(chr.getPotionPots().size());
        for (MaplePotionPot p : chr.getPotionPots()) {
            mplew.writeInt(p.getItmeId());
            mplew.writeInt(p.getChrId());
            mplew.writeInt(p.getMaxValue());
            mplew.writeInt(p.getHp());
            mplew.writeInt(p.getMp());
            mplew.writeLong(PacketHelper.getTime(p.getStartDate()));
            mplew.writeLong(PacketHelper.getTime(p.getEndDate()));
        }
    }

    public static void addCharCreateStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {

        mplew.writeInt(chr.getId());
        mplew.writeAsciiString(chr.getName(), 13);
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.writeInt(chr.getHair());
        mplew.writeZeroBytes(24);
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        chr.getStat().connectData(mplew);
        mplew.writeShort(chr.getRemainingAp());
        mplew.writeShort(chr.getRemainingSp());
        mplew.writeInt((int) chr.getExp());
        mplew.writeShort(chr.getFame());
        mplew.writeInt(chr.getGachExp());
        mplew.writeLong((getTime(System.currentTimeMillis())));
        mplew.writeInt(chr.getMapId());
        mplew.write(chr.getInitialSpawnpoint());
    }

    public static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {

        mplew.writeInt(chr.getId());
        mplew.writeAsciiString(chr.getName(), 13);
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.writeInt(chr.getHair());
        mplew.writeZeroBytes(24);
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        chr.getStat().connectData(mplew);
        mplew.writeShort(chr.getRemainingAp());
        mplew.writeShort(chr.getRemainingSp());
        mplew.writeInt((int) chr.getExp());
        mplew.writeShort(chr.getFame());
        mplew.writeInt(chr.getGachExp());
        mplew.writeLong((getTime(System.currentTimeMillis())));
        mplew.writeInt(chr.getMapId());
        mplew.write(chr.getInitialSpawnpoint());
    }

    public static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacterLook chr, boolean mega, boolean second) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair());

        final Map<Byte, Integer> myEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> maskedEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> totemEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> equip = second ? chr.getSecondEquips(true) : chr.getEquips(true);
        for (final Entry<Byte, Integer> item : equip.entrySet()) {
            if ((item.getKey()) < -127) {
                continue;
            }
            byte pos = (byte) ((item.getKey()) * -1);

            if ((pos < 100) && (myEquip.get(pos) == null)) {
                myEquip.put(pos, item.getValue());
            } else if ((pos > 100) && (pos != 111)) {
                pos = (byte) (pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getValue());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getValue());
            }
        }
        for (final Entry<Byte, Integer> totem : chr.getTotems().entrySet()) {
            byte pos = (byte) ((totem.getKey()) * -1);
            if (pos < 0 || pos > 2) { //3 totem slots
                continue;
            }
            if (totem.getValue() < 1200000 || totem.getValue() >= 1210000) {
                continue;
            }

            totemEquip.put(pos, totem.getValue());
        }

        for (Map.Entry entry : myEquip.entrySet()) {
            int weapon = ((Integer) entry.getValue());
            if (GameConstants.getWeaponType(weapon) == (second ? MapleWeaponType.LONG_SWORD : MapleWeaponType.BIG_SWORD)) {
                continue;
            }
            mplew.write(((Byte) entry.getKey()));
            mplew.writeInt(((Integer) entry.getValue()));
        }
        mplew.write(255);

        for (Map.Entry entry : maskedEquip.entrySet()) {
            mplew.write(((Byte) entry.getKey()));
            mplew.writeInt(((Integer) entry.getValue()));
        }
        mplew.write(255);

        Integer cWeapon = equip.get((byte) -111);
        mplew.writeInt(cWeapon != null ? cWeapon : 0);
        mplew.writeZeroBytes(12);

    }

    public static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time) {
        mplew.writeLong(time > -1 ? KoreanDateUtil.getFileTimestamp(time) : getTime(time));
    }

    public static void addItemPosition(MaplePacketLittleEndianWriter mplew, Item item, boolean trade, boolean bagSlot) {
        if (item == null) {
            mplew.write(0);
            return;
        }
        short pos = item.getPosition();
        if (pos <= -1) {
            pos = (short) (pos * -1);
            if ((pos > 100) && (pos < 1000)) {
                pos = (short) (pos - 100);
            }
        }
        if (bagSlot) {
            mplew.writeInt(pos % 100 - 1);
        } else if ((!trade) && (item.getType() == 1)) {
            mplew.write(pos);
        } else {
            mplew.write(pos);
        }
    }

    public static void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item) {
        addItemInfo(mplew, item, null);
    }

    public static void addItemInfo(final MaplePacketLittleEndianWriter mplew, final Item item, final MapleCharacter chr) {
        mplew.write(item.getPet() != null ? 3 : item.getType());
        mplew.writeInt(item.getItemId());
        boolean hasUniqueId = item.getUniqueId() > 0 && !GameConstants.isMarriageRing(item.getItemId()) && item.getItemId() / 10000 != 166;
        //marriage rings arent cash items so dont have uniqueids, but we assign them anyway for the sake of rings
        mplew.write(hasUniqueId ? 1 : 0);
        if (hasUniqueId) {
            mplew.writeLong(item.getUniqueId());
        }
        if (item.getPet() != null) { // Pet
            addPetItemInfo(mplew, item, item.getPet(), true);
        } else {
            addExpirationTime(mplew, item.getExpiration());
            if (item.getType() == 1) {
                final Equip equip = (Equip) item;
                addEquipStats(mplew, equip);
                if (!hasUniqueId) {
                    mplew.writeLong(item.getUniqueId()); //some tracking ID
                }
                mplew.writeLong(getTime(-2));
                mplew.writeInt(-1);

            } else {
                mplew.writeShort(item.getQuantity());
                mplew.writeMapleAsciiString(item.getOwner());
                mplew.writeShort(item.getFlag());
                if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId()) || item.getItemId() / 10000 == 287) {
                    mplew.writeLong(item.getInventoryId() <= 0 ? -1 : item.getInventoryId());
                }
            }
        }
    }

    public static void addEquipStatsTest(MaplePacketLittleEndianWriter mplew, Equip equip) {
        int mask;
        int masklength = 2;
        for (int i = 1; i <= masklength; i++) {
            mask = 0;
            if (equip.getStatsTest().size() > 0) {
                for (EquipStat stat : equip.getStatsTest().keySet()) {
                    if (stat.getPosition() == i) {
                        mask += stat.getValue();
                    }
                }
            }
            mplew.writeInt(mask);
            if (mask != 0) {
                for (EquipStat stat : equip.getStatsTest().keySet()) {
                    if (stat.getDatatype() == 8) {
                        mplew.writeLong(equip.getStatsTest().get(stat));
                    } else if (stat.getDatatype() == 4) {
                        mplew.writeInt(equip.getStatsTest().get(stat).intValue());
                    } else if (stat.getDatatype() == 2) {
                        mplew.writeShort(equip.getStatsTest().get(stat).shortValue());
                    } else if (stat.getDatatype() == 1) {
                        mplew.write(equip.getStatsTest().get(stat).byteValue());
                    }
                }
            }
        }
    }

    public static void addEquipStats(MaplePacketLittleEndianWriter mplew, Equip equip) {

        mplew.write(equip.getUpgradeSlots());
        mplew.write(equip.getLevel());
        mplew.writeShort(equip.getStr());
        mplew.writeShort(equip.getDex());
        mplew.writeShort(equip.getInt());
        mplew.writeShort(equip.getLuk());
        mplew.writeShort(equip.getHp());
        mplew.writeShort(equip.getMp());
        mplew.writeShort(equip.getWatk());
        mplew.writeShort(equip.getMatk());
        mplew.writeShort(equip.getWdef());
        mplew.writeShort(equip.getMdef());
        mplew.writeShort(equip.getAcc());
        mplew.writeShort(equip.getAvoid());
        mplew.writeShort(equip.getHands());
        mplew.writeShort(equip.getSpeed());
        mplew.writeShort(equip.getJump());
        mplew.writeMapleAsciiString(equip.getOwner());
        mplew.writeShort(equip.getFlag());
        mplew.write(equip.getIncSkill() > 0 ? 1 : 0);
        mplew.write(Math.max(equip.getBaseLevel(), equip.getEquipLevel())); // Item level

        mplew.writeInt((int) (equip.getExpPercentage() * 300000)); // Item Exp... 10000000 = 100%

        mplew.writeInt(equip.getViciousHammer());

    }

    public static void addEquipSpecialStats(MaplePacketLittleEndianWriter mplew, Equip equip) {
        int head = 0;
        if (equip.getSpecialStats().size() > 0) {
            for (EquipSpecialStat stat : equip.getSpecialStats()) {
                head |= stat.getValue();
            }
        }
        mplew.writeInt(head);

        if (head != 0) {
            if (equip.getSpecialStats().contains(EquipSpecialStat.TOTAL_DAMAGE)) {
                mplew.write(equip.getTotalDamage());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.ALL_STAT)) {
                mplew.write(equip.getAllStat());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.KARMA_COUNT)) {
                mplew.write(equip.getKarmaCount());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.UNK8)) {
                mplew.writeLong(System.currentTimeMillis());
            }
            if (equip.getSpecialStats().contains(EquipSpecialStat.UNK10)) {
                mplew.writeInt(0);
            }
        }
    }

//    public static void addEquipBonusStats(MaplePacketLittleEndianWriter mplew, Equip equip, boolean hasUniqueId) {
//        mplew.writeMapleAsciiString(equip.getOwner());
//        mplew.write(equip.getState()); // 17 = rare, 18 = epic, 19 = unique, 20 = legendary, potential flags. special grade is 14 but it crashes
//        mplew.write(equip.getEnhance());
//        mplew.writeShort(equip.getPotential1());
//        mplew.writeShort(equip.getPotential2());
//        mplew.writeShort(equip.getPotential3());
//        mplew.writeShort(equip.getBonusPotential1());
//        mplew.writeShort(equip.getBonusPotential2());
//        mplew.writeShort(equip.getBonusPotential3());
//        mplew.writeShort(equip.getFusionAnvil() % 100000);
//        mplew.writeShort(equip.getSocketState());
//        mplew.writeShort(equip.getSocket1() % 10000); // > 0 = mounted, 0 = empty, -1 = none.
//        mplew.writeShort(equip.getSocket2() % 10000);
//        mplew.writeShort(equip.getSocket3() % 10000);
//        if (!hasUniqueId) {
//            mplew.writeLong(equip.getInventoryId() <= 0 ? -1 : equip.getInventoryId()); //some tracking ID
//        }
//        mplew.writeLong(getTime(-2));
//        mplew.writeInt(-1); //?
//        
//    }
    public static void addEquipBonusStats(MaplePacketLittleEndianWriter mplew, Equip equip, boolean hasUniqueId) {
        mplew.writeMapleAsciiString(equip.getOwner());
        mplew.write(equip.getState()); // 17 = rare, 18 = epic, 19 = unique, 20 = legendary, potential flags. special grade is 14 but it crashes
        mplew.write(equip.getEnhance());
        mplew.writeShort(equip.getPotential1());
        mplew.writeShort(equip.getPotential2());
        mplew.writeShort(equip.getPotential3());
        mplew.writeShort(equip.getBonusPotential1());
        mplew.writeShort(equip.getBonusPotential2());
        mplew.writeShort(equip.getBonusPotential3());
        mplew.writeShort(equip.getFusionAnvil() % 100000);
        mplew.writeShort(equip.getSocketState());
        mplew.writeShort(equip.getSocket1() % 10000); // > 0 = mounted, 0 = empty, -1 = none.
        mplew.writeShort(equip.getSocket2() % 10000);
        mplew.writeShort(equip.getSocket3() % 10000);
        if (!hasUniqueId) {
            mplew.writeLong(equip.getInventoryId() <= 0 ? -1 : equip.getInventoryId()); //some tracking ID
        }
        mplew.writeLong(getTime(-2));
        mplew.writeInt(-1); //?

        mplew.writeLong(0);
        mplew.writeLong(getTime(-2));//
        mplew.writeInt(0);//
        mplew.writeInt(0);//
        mplew.writeInt(0);//
        mplew.writeInt(0);//

        mplew.writeShort(0);//
        mplew.writeInt(0);//破功
    }

    public static void serializeMovementList(MaplePacketLittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if ((chr.getPlayerShop() != null) && (chr.getPlayerShop().isOwner(chr)) && (chr.getPlayerShop().getShopType() != 1) && (chr.getPlayerShop().isAvailable())) {
            addInteraction(mplew, chr.getPlayerShop());
        } else {
            mplew.write(0);
        }
    }

    public static void addInteraction(MaplePacketLittleEndianWriter mplew, IMaplePlayerShop shop) {
        mplew.write(shop.getGameType());
        mplew.writeInt(((AbstractPlayerStore) shop).getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        if (shop.getShopType() != 1) {
            mplew.write(shop.getPassword().length() > 0 ? 1 : 0);
        }
        mplew.write(shop.getItemId() % 10);
        mplew.write(shop.getSize());
        mplew.write(shop.getMaxSize());
        if (shop.getShopType() != 1) {
            mplew.write(shop.isOpen() ? 0 : 1);
        }
    }

    public static void addCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        long mask = 0xFF_FF_FF_FF_FF_FF_FF_FFL; //FF FF FF FF FF FF DF FF v148+
        mplew.writeLong(mask);
        mplew.write(0);

        if ((mask & 1) != 0) {
            addCharCreateStats(mplew, chr);
            mplew.write(chr.getBuddylist().getCapacity());
            mplew.write(chr.getBlessOfFairyOrigin() != null);// 精灵的祝福
            if (chr.getBlessOfFairyOrigin() != null) {
                mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
            }
        }
        if ((mask & 2) != 0) {
            addMoneyInfo(mplew, chr);
            mplew.writeInt(chr.getId());
            mplew.writeInt(0);
            mplew.writeInt(chr.getCSPoints(2));
        }
        if ((mask & 8) != 0) {

            addInventoryInfo(mplew, chr);

        }
        if ((mask & 0x100) != 0) {
            addSkillInfo(mplew, chr);

        }
        if ((mask & 0x8000) != 0) {
            addCoolDownInfo(mplew, chr);
        }
        if ((mask & 0x200) != 0) {
            addStartedQuestInfo(mplew, chr);
        }
        if ((mask & 0x4000) != 0) {
            addCompletedQuestInfo(mplew, chr);
        }
        if ((mask & 0x400) != 0) {//小游戏讯息
            addUnk400Info(mplew, chr);
        }
        if ((mask & 0x800) != 0) {
            addRingInfo(mplew, chr);
        }
        if ((mask & 0x1000) != 0) {
            addRocksInfo(mplew, chr);
        }
        //怪物书
        addMonsterBookInfo(mplew, chr);
        
        if ((mask & 0x40000) != 0) {
            chr.QuestInfoPacket(mplew);
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);

    }

    public static int getSkillBook(final int i) {
        switch (i) {
            case 1:
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 2;
        }
        return 0;
    }

    public static void addAbilityInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<InnerSkillValueHolder> skills = chr.getInnerSkills();
        mplew.writeShort(skills.size());
        for (int i = 0; i < skills.size(); ++i) {
            mplew.write(i + 1); // key
            mplew.writeInt(skills.get(i).getSkillId()); //d 7000000 id ++, 71 = char cards
            mplew.write(skills.get(i).getSkillLevel()); // level
            mplew.write(skills.get(i).getRank()); //rank, C, B, A, and S
        }

    }

    public static void addHonorInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getHonorLevel()); //honor lvl
        mplew.writeInt(chr.getHonourExp()); //honor exp
    }

    public static void addEvolutionInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(0);
        mplew.writeShort(0);
    }

    public static void addCoreAura(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        MapleCoreAura aura = chr.getCoreAura();
        mplew.writeInt(aura.getId()); //nvr change
        mplew.writeInt(chr.getId());
        int level = chr.getSkillLevel(80001151) > 0 ? chr.getSkillLevel(80001151) : chr.getSkillLevel(1214);
        mplew.writeInt(level);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(aura.getExpire());//timer
        mplew.writeInt(0);
        mplew.writeInt(aura.getAtt());//wep att
        mplew.writeInt(aura.getDex());//dex
        mplew.writeInt(aura.getLuk());//luk
        mplew.writeInt(aura.getMagic());//magic att
        mplew.writeInt(aura.getInt());//int
        mplew.writeInt(aura.getStr());//str
        mplew.writeInt(0);
        mplew.writeInt(aura.getTotal());//max
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeLong(getTime(System.currentTimeMillis() + 86400000L));
        mplew.writeInt(0);
        mplew.write(GameConstants.isJett(chr.getJob()) && GameConstants.isBeastTamer(chr.getJob()) ? 1 : 0);
    }

    public static void addStolenSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int jobNum, boolean writeJob) {
        if (writeJob) {
            mplew.writeInt(jobNum);
        }
        int count = 0;
        if (chr.getStolenSkills() != null) {
            for (Pair<Integer, Boolean> sk : chr.getStolenSkills()) {
                if (GameConstants.getJobNumber(sk.left / 10000) == jobNum) {
                    mplew.writeInt(sk.left);
                    count++;
                    if (count >= GameConstants.getNumSteal(jobNum)) {
                        break;
                    }
                }
            }
        }
        while (count < GameConstants.getNumSteal(jobNum)) { //for now?
            mplew.writeInt(0);
            count++;
        }
    }

    public static void addChosenSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        for (int i = 1; i <= 4; i++) {
            boolean found = false;
            if (chr.getStolenSkills() != null) {
                for (Pair<Integer, Boolean> sk : chr.getStolenSkills()) {
                    if (GameConstants.getJobNumber(sk.left / 10000) == i && sk.right) {
                        mplew.writeInt(sk.left);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                mplew.writeInt(0);
            }
        }
    }

    public static void addStealSkills(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
        for (int i = 1; i <= 4; i++) {
            addStolenSkills(mplew, chr, i, false); // 52
        }
        addChosenSkills(mplew, chr); // 16
    }

    public static void addMonsterBookInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMonsterBookCover());
        mplew.write(0);
        chr.getMonsterBook().addCardPacket(mplew);
    }

    public static void addPetItemInfo(MaplePacketLittleEndianWriter mplew, Item item, MaplePet pet, boolean active) {
        if (item == null) {
            mplew.writeLong(PacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }

        mplew.writeAsciiString(pet.getName(), 13);
        mplew.write(pet.getLevel());
        mplew.writeShort(pet.getCloseness());
        mplew.write(pet.getFullness());
        if (item == null) {
            mplew.writeLong(PacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }
        mplew.writeShort(0);
        mplew.writeShort(pet.getFlags());
        mplew.writeShort(0);

        for (int i = 0; i < 4; i++) {
            mplew.write(0);
        }
    }

    public static void addShopInfo(MaplePacketLittleEndianWriter mplew, MapleShop shop, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        mplew.writeShort(shop.getItems().size());
        for (MapleShopItem item : shop.getItems()) {
            addShopItemInfo(mplew, item, shop, ii, null, c.getPlayer());
        }

    }

    /*
     * Categories:
     * 0 - No Tab
     * 1 - Equip
     * 2 - Use
     * 3 - Setup
     * 4 - Etc
     * 5 - Recipe
     * 6 - Scroll
     * 7 - Special
     * 8 - 8th Anniversary
     * 9 - Button
     * 10 - Invitation Ticket
     * 11 - Materials
     * 12 - Maple
     * 13 - Homecoming
     * 14 - Cores
     * 80 - JoeJoe
     * 81 - Hermoninny
     * 82 - Little Dragon
     * 83 - Ika
     */
    public static void addShopItemInfo(MaplePacketLittleEndianWriter mplew, MapleShopItem item, MapleShop shop, MapleItemInformationProvider ii, Item i, MapleCharacter chr) {
        mplew.writeInt(item.getItemId());
        mplew.writeInt(item.getPrice());

        if ((!GameConstants.isThrowingStar(item.getItemId())) && (!GameConstants.isBullet(item.getItemId()))) {
            mplew.writeShort(1); // stacksize o.o
            mplew.writeShort(item.getBuyable()); //buyable
        } else {
            mplew.writeZeroBytes(6);
            mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
            mplew.writeShort(ii.getSlotMax(item.getItemId()));

        }
    }

    public static void addJaguarInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(chr.getIntNoRecord(GameConstants.JAGUAR));
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(0);
        }
    }

    public static void addAdventurerInfo(MaplePacketLittleEndianWriter mplew) {
        mplew.write(HexTool.getByteArrayFromHexString("2A 00 98 46 07 00 63 6F 75 6E 74 3D 30 10 47 06 00 76 61 6C 32 3D 30 16 69 00 00 99 46 07 00 63 6F 75 6E 74 3D 30 09 47 03 00 44 3D 32 6A 36 1D 00 6C 61 73 74 47 61 6D 65 3D 31 34 2F 30 32 2F 31 39 3B 53 6E 57 41 74 74 65 6E 64 3D 30 CD 33 0B 00 62 6F 72 6E 3D 31 34 30 32 30 34 8B 45 00 00 17 69 00 00 9A 46 1A 00 63 6F 75 6E 74 30 3D 31 3B 63 6F 75 6E 74 31 3D 31 3B 63 6F 75 6E 74 32 3D 31 0A 47 03 00 45 3D 31 22 47 17 00 63 6F 6D 70 3D 31 3B 69 3D 32 33 30 30 30 30 30 30 30 30 30 30 30 30 FA 46 20 00 63 6F 75 6E 74 3D 35 3B 74 69 6D 65 3D 32 30 31 33 2F 31 32 2F 31 34 20 30 38 3A 33 32 3A 31 32 12 47 40 00 4D 4C 3D 30 3B 4D 4D 3D 30 3B 4D 41 3D 30 3B 4D 42 3D 30 3B 4D 43 3D 30 3B 4D 44 3D 30 3B 4D 45 3D 30 3B 4D 46 3D 30 3B 4D 47 3D 30 3B 4D 48 3D 30 3B 4D 49 3D 30 3B 4D 4A 3D 30 3B 4D 4B 3D 30 CB 36 1B 00 6D 41 74 74 65 6E 64 3D 30 3B 6C 61 73 74 47 61 6D 65 3D 31 34 2F 30 32 2F 31 39 1C 1E 13 00 64 72 61 77 3D 30 3B 6C 6F 73 65 3D 30 3B 77 69 6E 3D 30 18 69 00 00 23 47 09 00 62 41 74 74 65 6E 64 3D 30 63 47 20 00 64 74 3D 31 33 2F 31 32 2F 32 34 3B 64 3D 32 30 31 33 31 32 32 34 3B 69 3D 33 30 30 30 30 30 30 D7 33 05 00 73 6E 32 3D 30 F7 33 04 00 30 33 3D 31 19 69 00 00 B4 46 07 00 63 6F 75 6E 74 3D 30 23 7F 1F 00 6C 61 73 74 44 65 63 54 69 6D 65 3D 32 30 31 34 2F 30 32 2F 31 39 20 30 35 3A 34 31 3A 31 32 2C 47 07 00 4C 6F 67 69 6E 3D 31 64 47 04 00 41 51 3D 31 85 46 17 00 31 3D 30 3B 32 3D 30 3B 33 3D 30 3B 34 3D 30 3B 35 3D 30 3B 36 3D 30 B5 46 07 00 63 6F 75 6E 74 3D 30 0D 47 14 00 65 54 69 6D 65 3D 31 32 2F 31 32 2F 33 31 2F 30 30 2F 30 30 1D 47 06 00 73 74 65 70 3D 30 B6 46 07 00 63 6F 75 6E 74 3D 30 55 67 05 00 76 61 6C 3D 30 16 47 31 00 52 48 3D 30 3B 47 54 3D 30 3B 57 4D 3D 30 3B 46 41 3D 30 3B 45 43 3D 30 3B 43 48 3D 30 3B 4B 44 3D 30 3B 49 4B 3D 30 3B 50 44 3D 30 3B 50 46 3D 30 31 15 04 00 64 63 3D 30 87 46 1E 00 52 47 3D 30 3B 53 4D 3D 30 3B 41 4C 50 3D 30 3B 44 42 3D 30 3B 43 44 3D 30 3B 4D 48 3D 30 14 69 13 00 73 66 3D 30 3B 6D 74 3D 30 3B 61 6C 3D 31 3B 69 64 3D 30 42 34 0C 00 52 6F 6C 6C 50 65 72 44 61 79 3D 30 9F 46 1C 00 69 6E 64 65 78 3D 31 3B 6C 61 73 74 52 3D 31 33 2F 30 39 2F 32 35 3B 73 6E 31 3D 30 24 C8 0A 00 53 74 61 67 65 4B 65 79 3D 30 C3 33 07 00 63 6F 75 6E 74 3D 30 53 0C 07 00 72 65 73 65 74 3D 31 A0 46 05 00 6E 75 6D 3D 30 00 00"));
    }

    public static void addZeroInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        short mask = 0;
        mplew.writeShort(mask);
        if ((mask & 1) != 0) {
            mplew.write(0); //bool
        }
        if ((mask & 2) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 4) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 8) != 0) {
            mplew.write(0);
        }
        if ((mask & 10) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 20) != 0) {
            mplew.writeInt(0);
        }
        if ((mask & 40) != 0) {
            mplew.writeInt(0);
        }
        if (mask < 0) {
            mplew.writeInt(0);
        }
        if ((mask & 100) != 0) {
            mplew.writeInt(0);
        }
    }

    public static void addAdventurerInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(HexTool.getByteArrayFromHexString("14 00 0D 47 14 00 65 54 69 6D 65 3D 31 32 2F 31 32 2F 33 31 2F 30 30 2F 30 30 B6 46 07 00 63 6F 75 6E 74 3D 30 87 46 1E 00 52 47 3D 30 3B 53 4D 3D 30 3B 41 4C 50 3D 30 3B 44 42 3D 30 3B 43 44 3D 30 3B 4D 48 3D 30 16 47 31 00 52 48 3D 30 3B 47 54 3D 30 3B 57 4D 3D 30 3B 46 41 3D 30 3B 45 43 3D 30 3B 43 48 3D 30 3B 4B 44 3D 30 3B 49 4B 3D 30 3B 50 44 3D 30 3B 50 46 3D 30 9F 46 1C 00 69 6E 64 65 78 3D 31 3B 6C 61 73 74 52 3D 31 34 2F 30 33 2F 32 36 3B 73 6E 31 3D 30 A0 46 05 00 6E 75 6D 3D 30 40 47 3A 00 63 6F 75 6E 74 3D 30 3B 61 67 6F 3D 35 3B 64 6F 31 3D 30 3B 64 6F 32 3D 30 3B 64 61 69 6C 79 46 50 3D 30 3B 6C 61 73 74 44 61 74 65 3D 32 30 31 34 30 33 32 38 3B 46 50 3D 30 10 47 06 00 76 61 6C 32 3D 30 9A 46 1A 00 63 6F 75 6E 74 30 3D 31 3B 63 6F 75 6E 74 31 3D 31 3B 63 6F 75 6E 74 32 3D 31 22 47 17 00 63 6F 6D 70 3D 31 3B 69 3D 32 33 30 30 30 30 30 30 30 30 30 30 30 30 0A 47 03 00 45 3D 31 5B 46 0C 00 52 65 74 75 72 6E 55 73 65 72 3D 31 12 47 40 00 4D 4C 3D 30 3B 4D 4D 3D 30 3B 4D 41 3D 30 3B 4D 42 3D 30 3B 4D 43 3D 30 3B 4D 44 3D 30 3B 4D 45 3D 30 3B 4D 46 3D 30 3B 4D 47 3D 30 3B 4D 48 3D 30 3B 4D 49 3D 30 3B 4D 4A 3D 30 3B 4D 4B 3D 30 FA 46 20 00 63 6F 75 6E 74 3D 35 3B 74 69 6D 65 3D 32 30 31 34 2F 30 33 2F 32 36 20 30 37 3A 30 38 3A 33 37 23 47 09 00 62 41 74 74 65 6E 64 3D 30 B4 46 07 00 63 6F 75 6E 74 3D 30 85 46 17 00 31 3D 30 3B 32 3D 30 3B 33 3D 30 3B 34 3D 30 3B 35 3D 30 3B 36 3D 30 2C 47 07 00 4C 6F 67 69 6E 3D 31 64 47 04 00 41 51 3D 30 B5 46 07 00 63 6F 75 6E 74 3D 30"));
    }

    public static void addBeastTamerInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int beast = GameConstants.isBeastTamer(chr.getJob()) ? 1 : 0;
        String ears = Integer.toString(chr.getEars());
        String tail = Integer.toString(chr.getTail());

        mplew.write(HexTool.getByteArrayFromHexString("28 00 10 47 06 00 76 61 6C 32 3D 30 16 69 00 00 6A 36 1D 00 6C 61 73 74 47 61 6D 65 3D 31 34 2F 30 33 2F 30 36 3B 53 6E 57 41 74 74 65 6E 64 3D 30 8B 45 00 00 CD 33 0B 00 62 6F 72 6E 3D 31 34 30 33 30 36 9A 46 1A 00 63 6F 75 6E 74 30 3D 31 3B 63 6F 75 6E 74 31 3D 31 3B 63 6F 75 6E 74 32 3D 31 17 69 00 00 0A 47 03 00 45 3D 31 22 47 17 00 63 6F 6D 70 3D 31 3B 69 3D 32 33 30 30 30 30 30 30 30 30 30 30 30 30 FA 46 20 00 63 6F 75 6E 74 3D 35 3B 74 69 6D 65 3D 32 30 31 34 2F 30 33 2F 30 36 20 30 31 3A 30 39 3A 32 35 12 47 40 00 4D 4C 3D 30 3B 4D 4D 3D 30 3B 4D 41 3D 30 3B 4D 42 3D 30 3B 4D 43 3D 30 3B 4D 44 3D 30 3B 4D 45 3D 30 3B 4D 46 3D 30 3B 4D 47 3D 30 3B 4D 48 3D 30 3B 4D 49 3D 30 3B 4D 4A 3D 30 3B 4D 4B 3D 30 CB 36 1B 00 6D 41 74 74 65 6E 64 3D 30 3B 6C 61 73 74 47 61 6D 65 3D 31 34 2F 30 33 2F 30 36 1C 1E 13 00 64 72 61 77 3D 30 3B 6C 6F 73 65 3D 30 3B 77 69 6E 3D 30 18 69 00 00 23 47 09 00 62 41 74 74 65 6E 64 3D 30 D7 33 05 00 73 6E 32 3D 30 F7 33 04 00 30 33 3D 31 19 69 00 00 B4 46 07 00 63 6F 75 6E 74 3D 30 23 7F 1F 00 6C 61 73 74 44 65 63 54 69 6D 65 3D 32 30 31 34 2F 30 33 2F 30 36 20 30 36 3A 35 39 3A 32 32 2C 47 07 00 4C 6F 67 69 6E 3D 31 64 47 04 00 41 51 3D 30 85 46 17 00 31 3D 30 3B 32 3D 30 3B 33 3D 30 3B 34 3D 30 3B 35 3D 30 3B 36 3D 30 B5 46 07 00 63 6F 75 6E 74 3D 30 0D 47 14 00 65 54 69 6D 65 3D 31 32 2F 31 32 2F 33 31 2F 30 30 2F 30 30 1D 47 06 00 73 74 65 70 3D 30 B6 46 07 00 63 6F 75 6E 74 3D 30 55 67 05 00 76 61 6C 3D 30 16 47 31 00 52 48 3D 30 3B 47 54 3D 30 3B 57 4D 3D 30 3B 46 41 3D 30 3B 45 43 3D 30 3B 43 48 3D 30 3B 4B 44 3D 30 3B 49 4B 3D 30 3B 50 44 3D 30 3B 50 46 3D 30 31 15 04 00 64 63 3D 30 87 46 1E 00 52 47 3D 30 3B 53 4D 3D 30 3B 41 4C 50 3D 30 3B 44 42 3D 30 3B 43 44 3D 30 3B 4D 48 3D 30 14 69 13 00 73 66 3D 30 3B 6D 74 3D 30 3B 61 6C 3D 31 3B 69 64 3D 30 42 34 0C 00 52 6F 6C 6C 50 65 72 44 61 79 3D 30 9F 46 1C 00 69 6E 64 65 78 3D 31 3B 6C 61 73 74 52 3D 31 34 2F 30 33 2F 30 36 3B 73 6E 31 3D 30 8D E6 0E 00 6D 6F 76 69 65 3D 31 3B 74 75 74 6F 3D 31 A4 E7 2B 00"));
        mplew.writeAsciiString("bTail=" + beast + ";");
        mplew.writeAsciiString("bEar=" + beast + ";");
        mplew.writeAsciiString("TailID=" + tail + ";");
        mplew.writeAsciiString("EarID=" + ears);
        mplew.write(HexTool.getByteArrayFromHexString("24 C8 0A 00 53 74 61 67 65 4B 65 79 3D 30 C3 33 07 00 63 6F 75 6E 74 3D 30 53 0C 07 00 72 65 73 65 74 3D 31 A0 46 05 00 6E 75 6D 3D 30 00 00"));
    }

    public static void addRedLeafInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int idarr[] = new int[]{9410165, 9410166, 9410167, 9410168, 9410198};
        mplew.writeInt(chr.getClient().getAccID());
        mplew.writeInt(chr.getId());
        int size = 5;
        mplew.writeInt(size);
        mplew.writeInt(0);
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(idarr[i]);
            mplew.writeInt(/*chr.getFriendShipPoints()[i]*/0);
        }

    }

    public static void addLuckyLogoutInfo(MaplePacketLittleEndianWriter mplew, boolean enable, CashItem item0, CashItem item1, CashItem item2) {
        mplew.writeInt(enable ? 1 : 0);
        if (enable) {
            CSPacket.addCSItemInfo(mplew, item0);
            CSPacket.addCSItemInfo(mplew, item1);
            CSPacket.addCSItemInfo(mplew, item2);
        }
    }

    public static void addPartTimeJob(MaplePacketLittleEndianWriter mplew, PartTimeJob parttime) {
        mplew.write(parttime.getJob());
        mplew.writeReversedLong(getTime(System.currentTimeMillis()));
        mplew.writeInt(parttime.getReward());
        mplew.write(parttime.getReward() > 0);
    }

    public static <E extends Buffstat> void writeSingleMask(MaplePacketLittleEndianWriter mplew, E statup) {
        for (int i = GameConstants.MAX_BUFFSTAT; i >= 1; i--) {
            mplew.writeInt(i == statup.getPosition() ? statup.getValue() : 0);
        }
    }

    public static <E extends Buffstat> void writeMask(MaplePacketLittleEndianWriter mplew, Collection<E> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];

        for (Buffstat statup : statups) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Collection<Pair<E, Integer>> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];

        for (Pair statup : statups) {
            mask[(((Buffstat) statup.left).getPosition() - 1)] |= ((Buffstat) statup.left).getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Map<E, Integer> statups) {
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];

        for (Buffstat statup : statups.keySet()) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    private PacketHelper() {
    }

}
