package handling;

public enum RecvPacketOpcode implements WritableIntValueHolder {
    LOGIN_PASSWORD(false, (short) 0x01),//登录游戏 79
    SERVERLIST_REQUEST(false, (short) 0x02),//频道反馈 79
    LICENSE_REQUEST(false, (short) 0x03),//许可协议回复 79
    SET_GENDER(false, (short) 0x04),//选择性别 79
    SERVERSTATUS_REQUEST(true, (short) 0x05),//服务器状态 79
    CHARLIST_REQUEST(true, (short) 0x09),//请求人物列表 79
    CHAR_SELECT(true, (short) 0x0A),//开始游戏 79
    PLAYER_LOGGEDIN(false, (short) 0x0B),//请求进入游戏 79
    CHECK_CHAR_NAME(true, (short) 0x0C),//检查人物名字 79
    CREATE_CHAR(true, (short) 0x11),//创建人物 79
    PONG(false, (short) 0x13),//心跳包 79
    CLIENT_ERROR(false, (short) 0x14),//错误日志 79
    STRANGE_DATA(false, (short) 0x15),//数据封包 79

    CHANGE_MAP(true, (short) 0x21),//更换地图 79
    CHANGE_CHANNEL(true, (short) 0x22),//更换频道 79
    ENTER_CASH_SHOP(true, (short) 0x23),//进入商城 79
    MOVE_PLAYER(true, (short) 0x24),//人物移动 79
    CANCEL_CHAIR(true, (short) 0x25),//取消椅子 79
    USE_CHAIR(true, (short) 0x26),//使用椅子 79
    CLOSE_RANGE_ATTACK(true, (short) 0x28),//近距离攻击 79
    RANGED_ATTACK(true, (short) 0x29),//远距离攻击 79
    MAGIC_ATTACK(true, (short) 0x2A),//魔法攻击 79
    PASSIVE_ENERGY(true, (short) 0x2B),//被动攻击 79
    TAKE_DAMAGE(true, (short) 0x2C),//角色受到伤害 79
    GENERAL_CHAT(true, (short) 0x2D),//普通聊天 79
    CLOSE_CHALKBOARD(true, (short) 0x2E),//关闭黑板 79
    FACE_EXPRESSION(true, (short) 0x2F),//人物面部表情 79
    USE_ITEMEFFECT(true, (short) 0x30),//使用物品效果 79
    WHEEL_OF_FORTUNE(true, (short) 0x31),//使用未知效果 79
    MONSTER_BOOK_COVER(true, (short) 0x35),
    NPC_TALK(true, (short) 0x36),//NPC交谈 79
    NPC_TALK_MORE(true, (short) 0x38),//NPC详细交谈 79
    NPC_SHOP(true, (short) 0x3A),//NPC商店 79
    STORAGE(true, (short) 0x3B),//仓库 79
    USE_HIRED_MERCHANT(true, (short) 0x3C),//使用雇佣商店 79
    MERCH_ITEM_STORE(true, (short) 0x3D),//打开雇佣仓库 79
    OWL(true, (short) 0x3F),//商店搜索器 79
    OWL_WARP(true, (short) 0x40),//商店搜索器传送 79
    ITEM_SORT(true, (short) 0x42),//物品整理 79
    ITEM_GATHER(true, (short) 0x43),//物品排序 79
    ITEM_MOVE(true, (short) 0x44),//物品移动 79
    USE_ITEM(true, (short) 0x45),//使用物品 79
    CANCEL_ITEM_EFFECT(true, (short) 0x46),//取消物品效果 79
    USE_SUMMON_BAG(true, (short) 0x48),//召唤包 道具ID: 2100017 79
    PET_FOOD(true, (short) 0x49),//宠物食品 79
    USE_MOUNT_FOOD(true, (short) 0x4A),//坐骑食品 79 
    USE_SCRIPTED_NPC_ITEM(true, (short) 0x4B),//使用特殊消耗物品 79
    USE_CASH_ITEM(true, (short) 0x4C),//使用商城道具 79
    USE_CATCH_ITEM(true, (short) 0x4E),//扑捉道具 道具ID: 2270019 79
    USE_SKILL_BOOK(true, (short) 0x4F),//使用技能书 79
    USE_OWL_MINERVA(true, (short) 0x50),//商店搜索器开始搜索 79
    USE_TELE_ROCK(true, (short) 0x51),//使用瞬移之石 79
    USE_RETURN_SCROLL(true, (short) 0x52),//使用回城卷 79 
    USE_UPGRADE_SCROLL(true, (short) 0x53),//使用砸卷 79
    DISTRIBUTE_AP(true, (short) 0x54),//分配能力点 79
    AUTO_ASSIGN_AP(true, (short) 0x55),//自动分配能力点 79
    HEAL_OVER_TIME(true, (short) 0x56),//自动回复HP/MP 79
    DISTRIBUTE_SP(true, (short) 0x57),//分配技能点 79
    SPECIAL_MOVE(true, (short) 0x58),//角色使用技能 79
    CANCEL_BUFF(true, (short) 0x59),//取消增益效果 79
    SKILL_EFFECT(true, (short) 0x5A),//技能效果 79
    MESO_DROP(true, (short) 0x5B),//金币掉落 79
    GIVE_FAME(true, (short) 0x5C),//加人气 79
    CHAR_INFO_REQUEST(true, (short) 0x5E),//返回人物信息 79
    SPAWN_PET(true, (short) 0x5F),//召唤宠物 79
    CANCEL_DEBUFF(true, (short) 0x60),//取消负面效果 79
    CHANGE_MAP_SPECIAL(true, (short) 0x61),//特殊地图移动 79
    USE_INNER_PORTAL(true, (short) 0x62),//使用时空门 79
    TROCK_ADD_MAP(true, (short) 0x63),//使用缩地石 79
    LIE_DETECTOR(true, (short) 0x64),//使用测谎仪 79
    LIE_DETECTOR_SKILL(true, (short) 0x65),//测谎仪技能 79
    LIE_DETECTOR_RESPONSE(true, (short) 0x66),//测谎仪反馈 79
    LIE_DETECTOR_REFRESH(true, (short) 0x67),
    QUEST_ACTION(true, (short) 0x68),//任务操作 79
    REISSUE_MEDAL(true, (short) 0x999),//EB
    QUEST_WARP(true, (short) 0x6A),//EB
    SPECIAL_ATTACK(true, (short) 0x6C),//特殊技能攻击 79
    SKILL_MACRO(true, (short) 0x6D),//技能宏 79
    REWARD_ITEM(true, (short) 0x70),//随机物品 79
    ITEM_MAKER(true, (short) 0x71),//锻造技能 79
    USE_TREASURE_CHEST(true, (short) 0x72),//迷之蛋 79
    PARTYCHAT(true, (short) 0x74),
    COMMAND(true, (short) 0x75),
    MESSENGER(true, (short) 0x76),
    PLAYER_INTERACTION(true, (short) 0x77),
    PARTY_OPERATION(true, (short) 0x78),
    DENY_PARTY_REQUEST(true, (short) 0x79),
    GUILD_OPERATION(true, (short) 0x7A),
    DENY_GUILD_REQUEST(true, (short) 0x7B),
    ADMIN_COMMAND(true, (short) 0x7C),//12C
    ADMIN_LOG(true, (short) 0x7D),//12D
    BUDDYLIST_MODIFY(true, (short) 0x7E), //12E
    NOTE_ACTION(true, (short) 0x7F),//12F
    USE_DOOR(true, (short) 0x81),//131
    CHANGE_KEYMAP(true, (short) 0x83), //13D
    RPS_GAME(true, (short) 0x84),
    RING_ACTION(true, (short) 0x85),
    ALLIANCE_OPERATION(true, (short) 0x8A),//13B
    DENY_ALLIANCE_REQUEST(true, (short) 0x8B),//13C
    ENTER_MTS(true, (short) 0x8D),//13C
    SOLOMON(true, (short) 0x8E),//C9
    GACH_EXP(true, (short) 0x8F),//CA
    REQUEST_FAMILY(true, (short) 0x95),//13D
    OPEN_FAMILY(true, (short) 0x96),//13E
    FAMILY_OPERATION(true, (short) 0x97),//13F
    DELETE_JUNIOR(true, (short) 0x98),//140
    DELETE_SENIOR(true, (short) 0x99),//141
    ACCEPT_FAMILY(true, (short) 0x9A),//142
    USE_FAMILY(true, (short) 0x9B),//143
    FAMILY_PRECEPT(true, (short) 0x9C),//144
    FAMILY_SUMMON(true, (short) 0x9D),//145
    CYGNUS_SUMMON(true, (short) 0x9E),
    ARAN_COMBO(true, (short) 0x9F),//0x152
    MOVE_PET(true, (short) 0xA5),//1A8
    PET_CHAT(true, (short) 0xA6),//1A9
    PET_COMMAND(true, (short) 0xA7),//1AA
    PET_LOOT(true, (short) 0xA8),//1AB
    PET_AUTO_POT(true, (short) 0xA9),//1AC
    PET_IGNORE(true, (short) 0xAA),//1AD
    MOVE_SUMMON(true, (short) 0xAD),//1b8
    SUMMON_ATTACK(true, (short) 0xAE),//1B9
    DAMAGE_SUMMON(true, (short) 0xAF),//1BA
    SUB_SUMMON(true, (short) 0xB0),//1BB
    REMOVE_SUMMON(true, (short) 0xB1),//1BC
    MOVE_LIFE(true, (short) 0xB7),//怪物移动 79
    AUTO_AGGRO(true, (short) 0xB8),//210
    FRIENDLY_DAMAGE(true, (short) 0xBB),//211
    MONSTER_BOMB(true, (short) 0xBC),//212
    HYPNOTIZE_DMG(true, (short) 0xBD),//213 
    MOB_NODE(true, (short) 0xBE),//218
    DISPLAY_NODE(true, (short) 0xBF),//219

    NPC_ACTION(true, (short) 0xC0),//226
    ITEM_PICKUP(true, (short) 0xC6),//22A
    DAMAGE_REACTOR(true, (short) 0xC9),//22E
    TOUCH_REACTOR(true, (short) 0xCA),//22F
    SNOWBALL(true, (short) 0xCF),
    LEFT_KNOCK_BACK(true, (short) 0xD0),
    COCONUT(true, (short) 0xD1),
    MONSTER_CARNIVAL(true, (short) 0xD7),//21A
    PARTY_SEARCH_START(true, (short) 0xDB),//
    PARTY_SEARCH_STOP(true, (short) 0xDC),//
    CS_UPDATE(true, (short) 0xE8),//28E
    BUY_CS_ITEM(true, (short) 0xE9),//28F
    COUPON_CODE(true, (short) 0xEA),//28F
    UPDATE_QUEST(true, (short) 0xF7),//1C7//+16
    QUEST_ITEM(true, (short) 0xF8),//1D6
    USE_ITEM_QUEST(true, (short) 0xFA),//1C4
    CHATROOM_SYSTEM(true, (short) 0x104),//1C4
    VICIOUS_HAMMER(true, (short) 0x10D),
    //
    CLIENT_HELLO(false, (short) 0x999),//连接到服务器 111
    CREATE_ULTIMATE(true, (short) 0x999),//创建终极冒险家 111
    PART_TIME_JOB(true, (short) 0x999),//打工 111
    CHARACTER_CARD(true, (short) 0x999),//角色卡 111
    CHAR_CARD(true, (short) 0x999),//请求角色卡界面 111
    LOGIN_AUTH(false, (short) 0x999),//频道背景 111
    PACKET_ERROR(false, (short) 0x999),//封包错误 111
    PVP_ATTACK(true, (short) 0x999),//PVP攻击 111
    USE_TITLE(true, (short) 0x999),//使用称号效果 111
    PROFESSION_INFO(true, (short) 0x999),//专业技术窗口 111
    // General
    RSA_KEY(false),
    MAPLETV,
    LOGIN_REDIRECTOR(false, (short) 0x999),
    CRASH_INFO(false, (short) 0x999),
    // Login
    GUEST_LOGIN(true, (short) 0x999),
    TOS(true, (short) 0x999),
    VIEW_SERVERLIST(false, (short) 0x999),
    REDISPLAY_SERVERLIST(true),
    CHAR_SELECT_NO_PIC(false),
    DELETE_CHAR(true, (short) 0x999),
    AUTH_REQUEST(false, (short) 0x999),
    VIEW_REGISTER_PIC(true),
    VIEW_SELECT_PIC(true, (short) 0x999),
    CLIENT_START(false, (short) 0x999),
    CLIENT_FAILED(false, (short) 0x999),
    ENABLE_LV50_CHAR(true, (short) 0x999),
    CREATE_LV50_CHAR(true, (short) 0x999),
    ENABLE_SPECIAL_CREATION(true, (short) 0x999),
    CREATE_SPECIAL_CHAR(true, (short) 0x999),
    AUTH_SECOND_PASSWORD(true, (short) 0x999),
    WRONG_PASSWORD(false, (short) 0x999),//v145
    ENTER_AZWAN(true, (short) 0x999),
    ENTER_AZWAN_EVENT(true, (short) 0x999),
    LEAVE_AZWAN(true, (short) 0x999),
    ENTER_PVP(true, (short) 0x999),
    ENTER_PVP_PARTY(true, (short) 0x999),
    LEAVE_PVP(true, (short) 0x999),
    FACE_ANDROID(true, (short) 0x999),//6D
    ANGELIC_CHANGE(true, (short) 0x999),//72
    CHANGE_CODEX_SET(true, (short) 0x999),//7A
    CODEX_UNK(true, (short) 0x999),//7B
    MONSTER_BOOK_DROPS(true, (short) 0x999),//7C
    PACKAGE_OPERATION(true, (short) 0x999),//87
    MECH_CANCEL(true, (short) 0x999),//87
    MOVE_BAG(true, (short) 0x999),//95
    SWITCH_BAG(true, (short) 0x999),//96
    USE_RECIPE(true, (short) 0x999),//9D
    USE_NEBULITE(true, (short) 0x999),//9E
    USE_ALIEN_SOCKET(true, (short) 0x999),//9F
    USE_ALIEN_SOCKET_RESPONSE(true, (short) 0x999),//A0
    USE_NEBULITE_FUSION(true, (short) 0x999),//A1
    USE_EXP_POTION(true, (short) 0x999),//A8
    TOT_GUIDE(true, (short) 0x999),//B6
    USE_FLAG_SCROLL(true, (short) 0x999),//BE
    USE_EQUIP_SCROLL(true, (short) 0x999),//BF
    USE_POTENTIAL_SCROLL(true, (short) 0x999),//C3
    USE_ABYSS_SCROLL(true, (short) 0x999),//C4
    USE_CARVED_SEAL(true, (short) 0x999),//C5
    USE_BAG(true, (short) 0x999),
    USE_CRAFTED_CUBE(true, (short) 0x999),
    USE_MAGNIFY_GLASS(true, (short) 0x999),//CA
    GET_BOOK_INFO(true, (short) 0x999),//DC
    USE_FAMILIAR(true, (short) 0x999),//DD
    SPAWN_FAMILIAR(true, (short) 0x999),//DE
    RENAME_FAMILIAR(true, (short) 0x999),//DF
    PET_BUFF(true, (short) 0x999),//E0
    REPORT(true, (short) 0x999),//E9

    REPAIR_ALL(true, (short) 0x999),//C7
    REPAIR(true, (short) 0x999),//C8
    FOLLOW_REQUEST(true, (short) 0x999),//FD
    PQ_REWARD(true, (short) 0x999),//FE
    FOLLOW_REPLY(true, (short) 0x999),//101
    AUTO_FOLLOW_REPLY(true, (short) 0x999),
    USE_POT(true, (short) 0x999),//D6
    CLEAR_POT(true, (short) 0x999),
    FEED_POT(true, (short) 0x999),
    CURE_POT(true, (short) 0x999),
    REWARD_POT(true, (short) 0x999),
    AZWAN_REVIVE(true, (short) 0x999),
    USE_COSMETIC(true, (short) 0x999),
    INNER_CIRCULATOR(true, (short) 0x999),
    PVP_RESPAWN(true, (short) 0x999),
    GAIN_FORCE(true, (short) 0x999),
    ADMIN_CHAT(true, (short) 0x999),
    SPOUSE_CHAT(true, (short) 0x999),
    ALLOW_PARTY_INVITE(true, (short) 0x999),//12F
    EXPEDITION_OPERATION(true, (short) 0x999),//130
    EXPEDITION_LISTING(true, (short) 0x999),
    USE_MECH_DOOR(true, (short) 0x999),//132
    WEDDING_ACTION(true, (short) 0x999),
    BBS_OPERATION(true, (short) 0x999),//150
    SOLOMON_EXP(true, (short) 0x999),//151
    NEW_YEAR_CARD(true, (short) 0x999),
    XMAS_SURPRISE(true, (short) 0x999),
    TWIN_DRAGON_EGG(true, (short) 0x999),
    TRANSFORM_PLAYER(true, (short) 0x999),
    CRAFT_DONE(true, (short) 0x999),//157
    CRAFT_EFFECT(true, (short) 0x999),//158
    CRAFT_MAKE(true, (short) 0x999),//159
    CHANGE_ROOM_CHANNEL(true, (short) 0x999),//15D
    EVENT_CARD(true, (short) 0x999),//15E
    CHOOSE_SKILL(true, (short) 0x999),//15F
    SKILL_SWIPE(true, (short) 0x999),//160
    VIEW_SKILLS(true, (short) 0x999),//161
    CANCEL_OUT_SWIPE(true, (short) 0x999),//162
    YOUR_INFORMATION(true, (short) 0x999),//163
    FIND_FRIEND(true, (short) 0x999),//164
    PINKBEAN_CHOCO_OPEN(true, (short) 0x999),//165
    PINKBEAN_CHOCO_SUMMON(true, (short) 0x999),//166
    BUY_SILENT_CRUSADE(true, (short) 0x999),
    CASSANDRAS_COLLECTION(true, (short) 0x999),//new v145
    BUDDY_ADD(true, (short) 0x999),
    MOVE_HAKU(true, (short) 0x999),//1B1
    CHANGE_HAKU(true, (short) 0x999),//1B2
    PVP_SUMMON(true, (short) 0x999),//1BE
    MOVE_DRAGON(true, (short) 0x999),//1C0
    MOVE_ANDROID(true, (short) 0x999),//1C5
    MOVE_FAMILIAR(true, (short) 0x999),//1DC
    TOUCH_FAMILIAR(true),//1DD
    ATTACK_FAMILIAR(true, (short) 0x999),//1DE
    REVEAL_FAMILIAR(true, (short) 0x999),//1DF
    QUICK_SLOT(true, (short) 0x999),
    PAM_SONG(true, (short) 0x999),
    MOB_BOMB(true, (short) 0x999),//217
    CLICK_REACTOR(true, (short) 0x999),//230
    MAKE_EXTRACTOR(true, (short) 0x999),//231
    UPDATE_ENV(true, (short) 0x999),
    CANDY_RANKING(true, (short) 0x999),
    LUCKY_LUCKY_MONSTORY(true, (short) 0x999),//new v147
    SHIP_OBJECT(true, (short) 0x999),
    START_HARVEST(true, (short) 0x999),//24E
    STOP_HARVEST(true, (short) 0x999),//24F
    QUICK_MOVE(true, (short) 0x999),//19E
    CASH_CATEGORY(true, (short) 0x999),//295
    GOLDEN_HAMMER(true, (short) 0x999),
    PYRAMID_BUY_ITEM(true, (short) 0x999),
    CLASS_COMPETITION(true, (short) 0x999),
    MAGIC_WHEEL(true, (short) 0x999),
    REWARD(true, (short) 0x999),
    BLACK_FRIDAY(true, (short) 0x999),
    RECEIVE_GIFT_EFFECT(true, (short) 0x999),//new v145
    UPDATE_RED_LEAF(true, (short) 0x999),
    //Not Placed:
    SPECIAL_STAT(false, (short) 0x999),//107
    UPDATE_HYPER(true, (short) 0x999),//
    RESET_HYPER(true, (short) 0x999),//
    DRESSUP_TIME(true),
    DF_COMBO(true, (short) 0x999),
    BUTTON_PRESSED(true, (short) 0x999),//1D3
    OS_INFORMATION(true, (short) 0x999),//1D6
    LUCKY_LOGOUT(true, (short) 0x999),
    MESSENGER_RANKING(true, (short) 0x999),
    UNKNOWN;
    private short code = -2;

    @Override
    public void setValue(short code) {
        this.code = code;
    }

    @Override
    public final short getValue() {
        return code;
    }
    private final boolean CheckState;

    private RecvPacketOpcode() {
        this.CheckState = true;
    }

    private RecvPacketOpcode(final boolean CheckState) {
        this.CheckState = CheckState;
    }

    private RecvPacketOpcode(final boolean CheckState, short code) {
        this.CheckState = CheckState;
        this.code = code;
    }

    public final boolean NeedsChecking() {
        return CheckState;
    }

    public static String nameOf(short value) {
        for (RecvPacketOpcode header : RecvPacketOpcode.values()) {
            if (header.getValue() == value) {
                return header.name();
            }
        }
        return "UNKNOWN";
    }

    public static boolean isSpamHeader(RecvPacketOpcode header) {
        switch (header) {
            case PONG:
            case NPC_ACTION:
            //case PROFESSION_INFO:
            //            case ENTER:
            //            case CRASH_INFO:
            //            case AUTH_REQUEST:
            case MOVE_LIFE:
            case MOVE_PLAYER:
            //case SPECIAL_MOVE:
            //case MOVE_ANDROID:
            //            case MOVE_DRAGON:
            //case MOVE_SUMMON:
            //            case MOVE_FAMILIAR:
            //case MOVE_PET:
            //            case CLOSE_RANGE_ATTACK:
            //case QUEST_ACTION:
            case AUTO_AGGRO:
            //case HEAL_OVER_TIME:
            case STRANGE_DATA:
                //       case CHANGE_KEYMAP:
                //            case USE_INNER_PORTAL:
                //            case MOVE_HAKU:
                //            case FRIENDLY_DAMAGE:
                //             case CLOSE_RANGE_ATTACK: //todo code zero
                //case RANGED_ATTACK: //todo code zero
                //            case ARAN_COMBO:
                //            case SPECIAL_STAT:
                //            case UPDATE_HYPER:
                //            case RESET_HYPER:
                //            case ANGELIC_CHANGE:
                //            case DRESSUP_TIME:
                //            case BUTTON_PRESSED:
                return true;
            default:
                return false;
        }
    }
}
