package handling;

public enum SendPacketOpcode implements WritableIntValueHolder {
    LOGIN_STATUS((short) 0x00),//登陆反馈 79
    LICENSE_RESULT((short) 0x02),//许可协议 79
    CHOOSE_GENDER((short) 0x04),//性别选择 79
    GENDER_SET((short) 0x05),//性别选择反馈 79
    SERVERSTATUS((short) 0x06),//服务器状态 79
    SERVERLIST((short) 0x09),//服务器列表 79
    CHARLIST((short) 0x0A),//角色列表 79
    SERVER_IP((short) 0x0B),//服务器IP 79
    CHAR_NAME_RESPONSE((short) 0x0C),//检查人物名字 79
    ADD_NEW_CHAR_ENTRY((short) 0x11),//新增角色 79
    CHANGE_CHANNEL((short) 0x13),//更换频道 79
    PING((short) 0x14),//心跳包 79
    CS_USE((short) 0x15),//商城刷新 79

    INVENTORY_OPERATION((short) 0x20),//道具栏信息 79
    INVENTORY_GROW((short) 0x21),//更新道具栏数量 79
    UPDATE_STATS((short) 0x22),//刷新属性 79
    GIVE_BUFF((short) 0x23),//获得增益效果状态 79
    CANCEL_BUFF((short) 0x24),//取消增益效果状态 79
    TEMP_STATS((short) 0x25),//临时能力值开始 79
    TEMP_STATS_RESET((short) 0x26),//临时能力值重置 79
    UPDATE_SKILLS((short) 0x27),//刷新技能 79
    FAME_RESPONSE((short) 0x29),//人气反馈 79
    SHOW_STATUS_INFO((short) 0x2A),//显示人物具体信息 79
    SHOW_NOTES((short) 0x2B),//显示小纸条 79
    TROCK_LOCATIONS((short) 0x2C),//使用缩地石 79
    LIE_DETECTOR((short) 0x2D),//测谎仪 79
    UPDATE_MOUNT((short) 0x32),//更新骑宠 79
    SHOW_QUEST_COMPLETION((short) 0x33),//任务提示 79
    SEND_TITLE_BOX((short) 0x34),//雇佣商店 79
    USE_SKILL_BOOK((short) 0x35),//使用技能书 79

    FINISH_GATHER((short) 0x36),//道具排序 79
    FINISH_SORT((short) 0x37),//道具集合 79
    CHAR_INFO((short) 0x3A),//角色信息 79

    PARTY_OPERATION((short) 0x3B),//组队操作 79

    BUDDYLIST((short) 0x3C),//好友列表 79
    GUILD_OPERATION((short) 0x3E),//家族操作 79
    ALLIANCE_OPERATION((short) 0x3F),//家族联盟 79
    SPAWN_PORTAL((short) 0x40),//祭祀时空门 70
    SERVERMESSAGE((short) 0x41),//服务器公告 79

    OWL_OF_MINERVA((short) 0x43),//商店搜索器 79
    OWL_RESULT((short) 0x44),//商店搜索传送 79

    MARRAGE_EFFECT((short) 0x47),//红鸾宫结婚滚动特效条 79
    YELLOW_CHAT((short) 0x4E),//黄字信息 79
    CATCH_MOB((short) 0x50),//捕抓怪物 79
    AVATAR_MEGA((short) 0x56),//人物广播喇叭 79
    PLAYER_NPC((short) 0x59),//玩家NPC 79
    DISABLE_NPC((short) 0x5A),//删除玩家NPC 79
    MONSTERBOOK_ADD((short) 0x5B),
    MONSTERBOOK_CHANGE_COVER((short) 0x5C),
    SESSION_VALUE((short) 0x62),//道场能量 79
    EXP_BONUS((short) 0x63),
    SEND_PEDIGREE((short) 0x64),//打开校谱 79
    OPEN_FAMILY((short) 0x65),//打开学院 79
    FAMILY_MESSAGE((short) 0x66),//学院返回信息 79
    FAMILY_INVITE((short) 0x67),//学院邀请 79
    FAMILY_JUNIOR((short) 0x68),//接受(拒绝)学院邀请 79
    SENIOR_MESSAGE((short) 0x69),//??? 79
    FAMILY((short) 0x6A),//加载学院 79
    REP_INCREASE((short) 0x6B),//改变学院点数 79
    FAMILY_LOGGEDIN((short) 0x6C),//学院玩家登录 79
    FAMILY_BUFF((short) 0x6D),//学院状态 79
    FAMILY_USE_REQUEST((short) 0x6E),//使用学院点 79
    LEVEL_UPDATE((short) 0x6F),//升级提示 79
    MARRIAGE_UPDATE((short) 0x70),//结婚提示 79
    JOB_UPDATE((short) 0x71),//转职提示 79
    SLOT_UPDATE((short) 0x72),//项链扩充 79
    TOP_MSG((short) 0x73),//顶部的公告 79
    FISHING_BOARD_UPDATE((short) 0x75),//钓鱼效果 79
    OPEN_WEB((short) 0x76),//打开网页 79
    MAPLE_POINT((short) 0x7D),//显示抵用卷 79
    SKILL_MACRO((short) 0x80),//组合技能 79
    WARP_TO_MAP((short) 0x81),//进入地图 79
    CS_OPEN((short) 0x83),//进入商城 79
    RESET_SCREEN((short) 0x84),//重置屏幕  79
    MAP_BLOCKED((short) 0x85),//地图错误 79
    SERVER_BLOCKED((short) 0x86),//服务器错误 79
    PARTY_BLOCKED((short) 0x87),//组队提示错误 79 
    SHOW_EQUIP_EFFECT((short) 0x89),//显示装备效果 79
    MULTICHAT((short) 0x8A),//组队家族聊天 79
    WHISPER((short) 0x8B),//悄悄话 79
    SPOUSE_CHAT((short) 0x8C),//不知道啥对话 79
    BOSS_ENV((short) 0x8D),//BOSS血条 79
    MOVE_ENV((short) 0x8E),//不知道啥 79
    UPDATE_ENV((short) 0x8F),//更新BOSS血条 79
    MAP_EFFECT((short) 0x91),//地图效果 79 
    CASH_SONG((short) 0x92),//音乐盒 5100000 79
    GM_EFFECT((short) 0x93),//GM效果 79
    OX_QUIZ((short) 0x94),//0X问答活动 79
    GMEVENT_INSTRUCTIONS((short) 0x95),//0X问答提示 79
    CLOCK((short) 0x96),//时钟 79
    BOAT_STATE((short) 0x97),//船效果 79
    BOAT_MOVE((short) 0x98),//船效果 79
    STOP_CLOCK((short) 0x9C),//停止时钟 79
    MOVE_PLATFORM((short) 0x9F),//移动平台 79
    PYRAMID_UPDATE((short) 0xA0),//金字塔更新 79
    PYRAMID_RESULT((short) 0xA1),//金字塔得分 79
    SPAWN_PLAYER((short) 0xA2),//召唤玩家 79
    REMOVE_PLAYER_FROM_MAP((short) 0xA3),//移除玩家 79
    CHATTEXT((short) 0xA4),//聊天信息 79
    CHATTEXT_1((short) 0xA5),//聊天信息 79
    CHALKBOARD((short) 0xA6),//小黑板 79
    UPDATE_CHAR_BOX((short) 0xA7),//刷新？ 79
    SHOW_CONSUME_EFFECT((short) 0xA8),//效果？ 79
    SHOW_SCROLL_EFFECT((short) 0xA9),//砸卷效果 79
    //0xAA Item/Etc/0429.img/%08d/effect
    FISHING_CAUGHT((short) 0xAB),//钓鱼？ 79
    SPAWN_PET((short) 0xAD),//召唤宠物 79
    MOVE_PET((short) 0xAF),//宠物移动 79
    PET_CHAT((short) 0xB0),//宠物说话 79
    PET_NAMECHANGE((short) 0xB1),//宠物名变更 79
    PET_EXCEPTION_LIST((short) 0xB2),//宠物捡取过滤 79
    PET_COMMAND((short) 0xB3),//宠物命令 [吃宠物食品出包] 79
    SPAWN_SUMMON((short) 0xB4),//召唤召唤兽 79
    REMOVE_SUMMON((short) 0xB5),//移除召唤兽 79
    MOVE_SUMMON((short) 0xB6),//召唤兽移动 79
    SUMMON_ATTACK((short) 0xB7),//召唤兽攻击 79
    SUMMON_SKILL((short) 0xB8),//召唤兽技能 79
    DAMAGE_SUMMON((short) 0xB9),//召唤兽受到伤害 79
    MOVE_PLAYER((short) 0xBB),//玩家移动 79
    CLOSE_RANGE_ATTACK((short) 0xBC),//近距离攻击 79
    RANGED_ATTACK((short) 0xBD),//远距离攻击 79
    MAGIC_ATTACK((short) 0xBE),//魔法攻击 79
    ENERGY_ATTACK((short) 0xBF),//被动攻击 79
    SKILL_EFFECT((short) 0xC0),//技能效果[用主教的创世之破抓到包] 79
    CANCEL_SKILL_EFFECT((short) 0xC1),//取消技能效果 79
    DAMAGE_PLAYER((short) 0xC2),//玩家受到伤害 79
    FACIAL_EXPRESSION((short) 0xC3),//玩家面部表情 79
    SHOW_EFFECT((short) 0xC5),//显示物品效果 79
    SHOW_CHAIR((short) 0xC6),//显示椅子效果 79
    UPDATE_CHAR_LOOK((short) 0xC7),//更新玩家外观 79
    SHOW_FOREIGN_EFFECT((short) 0xC8),//玩家外观状态 79
    GIVE_FOREIGN_BUFF((short) 0xC9),//Buff外观效果 79
    CANCEL_FOREIGN_BUFF((short) 0xCA),//取消Buff外观效果 79
    UPDATE_PARTYMEMBER_HP((short) 0xCB),//更新组队HP显示 79
    LOAD_GUILD_NAME((short) 0xCC),//加载家族名字 79
    LOAD_GUILD_ICON((short) 0xCD),//加载家族图标 79
    SHOW_SPECIAL_ATTACK((short) 0xCE),//显示特殊攻击效果 79
    CANCEL_CHAIR((short) 0xCF),//取消椅子 79
    SHOW_SPECIAL_EFFECT((short) 0xD0),//显示各种特效 79
    CURRENT_MAP_WARP((short) 0xD1),//角色地图瞬移 武林道场会出现 79
    MESOBAG_SUCCESS((short) 0xD2),//使用金币包成功 79
    MESOBAG_FAILURE((short) 0xD3),//使用金币包失败 79
    R_MESOBAG_SUCCESS((short) 0xD4),//使用金币包成功 79
    R_MESOBAG_FAILURE((short) 0xD5),//使用金币包失败 79
    UPDATE_QUEST_INFO((short) 0xD6),//更新任务信息 79
    PET_FLAG_CHANGE((short) 0xD8),//增加(取消)宠物技能 79
    PLAYER_HINT((short) 0xD9),//玩家头顶提示 79
    
    MAKER_SKILL((short) 0xDF),//锻造技能 79

    OPEN_UI((short) 0xE2),//打开UI 79
    INTRO_DISABLE_UI((short) 0xE3),//任务锁界面 79
    INTRO_LOCK((short) 0xE4),//任务锁界面 79
    SUMMON_HINT((short) 0xE5),//战神召唤兽 79
    SUMMON_HINT_MSG((short) 0xE6),//战神召唤兽提示 79
    ARAN_COMBO((short) 0xE7),//战神连击 79
    COOLDOWN((short) 0xEC),//技能冷却 79
    SPAWN_MONSTER((short) 0xEE),//怪物召唤 79
    KILL_MONSTER((short) 0xEF),//杀死怪物 79
    SPAWN_MONSTER_CONTROL((short) 0xF0),//怪物召唤控制 79
    MOVE_MONSTER((short) 0xF1),//怪物移动 79
    MOVE_MONSTER_RESPONSE((short) 0xF2),//怪物移动回应 79
    APPLY_MONSTER_STATUS((short) 0xF4),//添加怪物状态 79
    CANCEL_MONSTER_STATUS((short) 0xF5),//取消怪物状态 79
    MOB_TO_MOB_DAMAGE((short) 0xF7),
    DAMAGE_MONSTER((short) 0xF8),//怪物受到伤害 79
    SHOW_MONSTER_HP((short) 0xFC),//显示怪物HP 79
    SHOW_MAGNET((short) 0xFD),//显示磁铁效果 79
    CATCH_MONSTER((short) 0xFF),//捕捉怪物效果 79

    SPAWN_NPC((short) 0x104),//召唤NPC 79
    REMOVE_NPC((short) 0x105),//移除NPC 79
    SPAWN_NPC_REQUEST_CONTROLLER((short) 0x106),//召唤NPC控制 79

    SPAWN_HIRED_MERCHANT((short) 0x10D),//召唤雇佣商店 79
    DESTROY_HIRED_MERCHANT((short) 0x10E),//取消雇佣商店 79
    UPDATE_HIRED_MERCHANT((short) 0x10F),//更新雇佣商店 79
    DROP_ITEM_FROM_MAPOBJECT((short) 0x110),//掉落物品在地图上 79
    REMOVE_ITEM_FROM_MAP((short) 0x111),//从地图上删除物品 79

    SPAWN_KITE_ERROR((short) 0x112),//召唤风筝错误 79
    SPAWN_KITE((short) 0x113),//召唤风筝 79
    DESTROY_KITE((short) 0x114),//移除风筝 79
    SPAWN_MIST((short) 0x115),//召唤烟雾 79
    REMOVE_MIST((short) 0x116),//移除烟雾 79
    SPAWN_DOOR((short) 0x117),//召唤门 79
    REMOVE_DOOR((short) 0x118),//移除门 79

    REACTOR_HIT((short) 0x11C),//攻击反应堆 79
    REACTOR_MOVE((short) 0x11D),//反应堆移动 79
    REACTOR_SPAWN((short) 0x11E),//召唤反应堆 79
    REACTOR_DESTROY((short) 0x11F),//移除反应堆 79

    ROLL_SNOWBALL((short) 0x120),//
    HIT_SNOWBALL((short) 0x121),
    SNOWBALL_MESSAGE((short) 0x122),
    LEFT_KNOCK_BACK((short) 0x123),
    HIT_COCONUT((short) 0x124),
    COCONUT_SCORE((short) 0x125),
    MONSTER_CARNIVAL_START((short) 0x129),
    MONSTER_CARNIVAL_OBTAINED_CP((short) 0x12A),
    MONSTER_CARNIVAL_PARTY_CP((short) 0x12B),
    MONSTER_CARNIVAL_STATS((short) 0x999),//
    MONSTER_CARNIVAL_SUMMON((short) 0x12C),//
    MONSTER_CARNIVAL_MESSAGE((short) 0x12D),//
    MONSTER_CARNIVAL_DIED((short) 0x12E),//
    
    CHAOS_ZAKUM_SHRINE((short) 0x999),
    NPC_TALK((short) 0x145),//NPC交谈 79
    OPEN_NPC_SHOP((short) 0x146),//打开NPC商店 79
    CONFIRM_SHOP_TRANSACTION((short) 0x147),//NPC商店确认 79
    OPEN_STORAGE((short) 0x14A),//打开仓库 79
    MERCH_ITEM_MSG((short) 0x14B),//弗兰德里 79
    MERCH_ITEM_STORE((short) 0x14C),//弗兰德里 79
    RPS_GAME((short) 0x14D),//剪刀石头布 79
    MESSENGER((short) 0x14E),////聊天招待 79
    PLAYER_INTERACTION((short) 0x14F),//玩家交易 79
    CS_UPDATE((short) 0x161),//商城点券更新 79
    CS_OPERATION((short) 0x162),//商城操作 79
    START_TV((short) 0x16B),//37A
    REMOVE_TV((short) 0x16C),//37B
    ENABLE_TV((short) 0x16D),//37C
    KEYMAP((short) 0x16F),//键盘设置 79
    PET_AUTO_HP((short) 0x170),//自动吃药HP 79
    PET_AUTO_MP((short) 0x171),//自动吃药MP 79
    VICIOUS_HAMMER((short) 0x183),//金锤子 79

    //
    EVENT_CHECK((short) 0x999),//??? 111
    PART_TIME((short) 0x999),//打工 111
    SHOW_CHAR_CARDS((short) 0x999),//显示角色卡 111
    LOGIN_AUTH((short) 0x999),//频道背景 111
    UPDATE_STOLEN_SKILLS((short) 0x999),//复制技能反馈 111
    TARGET_SKILL((short) 0x999),//复制技能列表 111
    REPORT_RESPONSE((short) 0x999),//设置举报 111
    REPORT_TIME((short) 0x999),//设置举报 111
    REPORT_STATUS((short) 0x999),//设置举报 111
    SP_RESET((short) 0x999),//SP重置
    AP_RESET((short) 0x999),//AP_RESET
    MEMBER_SEARCH((short) 0x999),//寻找队员 111
    PARTY_SEARCH((short) 0x999),//寻找组队 111
    BOOK_INFO((short) 0x999),//怪物卡信息
    EXPEDITION_OPERATION((short) 0x999),//远征队操作 111
    MECH_PORTAL((short) 0x999),//机械师避难所 111
    PIGMI_REWARD((short) 0x999),//花生机奖励 111
    ENGAGE_REQUEST((short) 0x999),//戒指操作请求 111
    ENGAGE_RESULT((short) 0x999),//戒指操作返回
    SHOP_DISCOUNT((short) 0x999),//商店优惠价格 111
    
    PARTY_VALUE((short) 0x999),//？？？ v111
    MAP_VALUE((short) 0x999),//？？？ v111
    FOLLOW_REQUEST((short) 0x999),//请求跟随提示 111
    MID_MSG((short) 0x999),//中间的公告 111
    CLEAR_MID_MSG((short) 0x999),//清理中间的公告 111
    SPECIAL_MSG((short) 0x999),//??? 111
    MAPLE_ADMIN_MSG((short) 0x999),//冒险岛运营员NPC自定义对话 111
    GM_STORY_BOARD((short) 0x999),//系统GM警告 111
    UPDATE_JAGUAR((short) 0x999),// 美洲豹更新 111
    ULTIMATE_EXPLORER((short) 0x999),//终极冒险家窗口 111
    PAM_SONG((short) 0x999),//??? 111
    SPECIAL_STAT((short) 0x999),//专业技术 111
    ITEM_POT((short) 0x999),//道具宝宝 111
    GIVE_CHARACTER_SKILL((short) 0x999),//传授技能 1113
    MULUNG_DOJO_RANKING((short) 0x999),//武陵排名 111
    QUICK_MOVE((short) 0x999),//快捷移动 111
    SHOW_HARVEST((short) 0x999),//显示玩家采集效果 111
    OPEN_UI_OPTION((short) 0x999),//打开UI 111
    INTRO_ENABLE_UI((short) 0x999),//任务锁界面 111
    GAME_MSG((short) 0x999),//道具制作提示 111
    GAME_MESSAGE((short) 0x999),//提示信息 111
    // General

    AUTH_RESPONSE((short) 0x999),
    // Login

    SEND_LINK((short) 0x999),
    LOGIN_SECOND((short) 0x999),
    PIN_OPERATION((short) 0x999),
    PIN_ASSIGNED((short) 0x999),
    ALL_CHARLIST((short) 0x999),
    DELETE_CHAR_RESPONSE((short) 0x999),
    RELOG_RESPONSE((short) 0x999),
    REGISTER_PIC_RESPONSE((short) 0x999),
    ENABLE_RECOMMENDED((short) 0x999),
    SEND_RECOMMENDED((short) 0x999),
    CHANNEL_SELECTED((short) 0x999),
    EXTRA_CHAR_INFO((short) 0x999),//23
    SPECIAL_CREATION((short) 0x999),//24
    SECONDPW_ERROR((short) 0x999),//25
    CHANGE_BACKGROUND((short) 0x999),//v148
    // Channel

    FULL_CLIENT_DOWNLOAD((short) 0x999),
    //

    EXP_POTION((short) 0x999),
    UPDATE_GENDER((short) 0x999),
    BBS_OPERATION((short) 0x999),
    ECHO_MESSAGE((short) 0x999),//64
    WEDDING_GIFT((short) 0x999),//71
    WEDDING_MAP_TRANSFER((short) 0x999),
    USE_CASH_PET_FOOD((short) 0x999),
    GET_CARD((short) 0x999),//7B
    CARD_UNK((short) 0x999),//new143
    CARD_SET((short) 0x999),//7D
    BOOK_STATS((short) 0x999),//7E
    UPDATE_CODEX((short) 0x999),//7F
    CARD_DROPS((short) 0x999),//80
    FAMILIAR_INFO((short) 0x999),//81
    CHANGE_HOUR((short) 0x999),//83
    RESET_MINIMAP((short) 0x999),//87
    CONSULT_UPDATE((short) 0x999),//88
    CLASS_UPDATE((short) 0x999),//89
    WEB_BOARD_UPDATE((short) 0x999),//8A
    POTION_BONUS((short) 0x999),//8D
    MAPLE_TV_MSG((short) 0x999),
    LUCKY_LUCKY_MONSTORY((short) 0x999),//new v147
    AVATAR_MEGA_RESULT((short) 0x999),//FF
    AVATAR_MEGA_REMOVE((short) 0x999),//101
    POPUP2((short) 0x999),
    CANCEL_NAME_CHANGE((short) 0x999),
    CANCEL_WORLD_TRANSFER((short) 0x999),
    CLOSE_HIRED_MERCHANT((short) 0x999),//A0
    GM_POLICE((short) 0x999),//A1
    TREASURE_BOX((short) 0x999),//A2
    NEW_YEAR_CARD((short) 0x999),//A3
    RANDOM_MORPH((short) 0x999),//A4
    CANCEL_NAME_CHANGE_2((short) 0x999),//A9
    CAKE_VS_PIE_MSG((short) 0x999),
    INVENTORY_FULL((short) 0x999),//v145
    ZERO_STATS((short) 0x999),
    NEW_TOP_MSG((short) 0x999),//new148

    YOUR_INFORMATION((short) 0x999),
    FIND_FRIEND((short) 0x999),
    VISITOR((short) 0x999),
    PINKBEAN_CHOCO((short) 0x999),
    AUTO_CC_MSG((short) 0x999),
    DISALLOW_DELIVERY_QUEST((short) 0x999),//bb

    UPDATE_IMP_TIME((short) 0x999),//BE
    MULUNG_MESSAGE((short) 0x999),//C2

    UPDATE_INNER_ABILITY((short) 0x999),//CD
    EQUIP_STOLEN_SKILL((short) 0x999),//CE
    REPLACE_SKILLS((short) 0x999),//CE
    INNER_ABILITY_MSG((short) 0x999),//CF
    ENABLE_INNER_ABILITY((short) 0x999),//D0
    DISABLE_INNER_ABILITY((short) 0x999),//D1
    UPDATE_HONOUR((short) 0x999),//D2
    AZWAN_UNKNOWN((short) 0x999),//D3 //probably circulator shit?
    AZWAN_RESULT((short) 0x999),//D4
    AZWAN_KILLED((short) 0x999),//D5
    CIRCULATOR_ON_LEVEL((short) 0x999),//D6
    SILENT_CRUSADE_MSG((short) 0x999),//D7
    SILENT_CRUSADE_SHOP((short) 0x999),//D8
    CASSANDRAS_COLLECTION((short) 0x999),//new v145

    SET_OBJECT_STATE((short) 0x999),//E8
    POPUP((short) 0x999),//E9
    MINIMAP_ARROW((short) 0x999),//ED
    UNLOCK_CHARGE_SKILL((short) 0x999),//F2
    LOCK_CHARGE_SKILL((short) 0x999),//F3
    CANDY_RANKING((short) 0x999),//F8
    ATTENDANCE((short) 0x999),//102
    MESSENGER_OPEN((short) 0x999),//103
    EVENT_CROWN((short) 0x999),//10D
    RANDOM_RESPONSE((short) 0x999),
    MAGIC_WHEEL((short) 0x999),//125
    REWARD((short) 0x999),//126
    MTS_OPEN((short) 0x999),//12A
    REMOVE_BG_LAYER((short) 0x999),//12E
    SET_MAP_OBJECT_VISIBLE((short) 0x999),//12F
    ARIANT_SCOREBOARD((short) 0x999),//12F
    QUICK_SLOT((short) 0x999),//153
    PVP_INFO((short) 0x999),//154
    PYRAMID_KILL_COUNT((short) 0x999),//155
    DIRECTION_STATUS((short) 0x999),//159
    GAIN_FORCE((short) 0x999),//15A
    ACHIEVEMENT_RATIO((short) 0x999),//15B
    SHOW_MAGNIFYING_EFFECT((short) 0x999),//16E
    SHOW_POTENTIAL_RESET((short) 0x999),//16F
    SHOW_FIREWORKS_EFFECT((short) 0x999),//170
    SHOW_NEBULITE_EFFECT((short) 0x999),//171
    SHOW_FUSION_EFFECT((short) 0x999),//172
    PVP_ATTACK((short) 0x999),
    PVP_MIST((short) 0x999),
    PVP_COOL((short) 0x999),
    TESLA_TRIANGLE((short) 0x999),//0x15C
    FOLLOW_EFFECT((short) 0x999),
    SHOW_PQ_REWARD((short) 0x999),
    CRAFT_EFFECT((short) 0x999),//15F
    CRAFT_COMPLETE((short) 0x999),//160
    HARVESTED((short) 0x999),//161
    PLAYER_DAMAGED((short) 0x999),
    NETT_PYRAMID((short) 0x999),
    SET_PHASE((short) 0x999),
    PAMS_SONG((short) 0x999),
    SPAWN_PET_2((short) 0x999),//16D
    PET_COLOR((short) 0x999),//172
    PET_SIZE((short) 0x999),//173
    DRAGON_SPAWN((short) 0x999),//175
    INNER_ABILITY_RESET_MSG((short) 0x999),//173
    DRAGON_MOVE((short) 0x999),//176
    DRAGON_REMOVE((short) 0x999),//177
    ANDROID_SPAWN((short) 0x999),//178
    ANDROID_MOVE((short) 0x999),//179
    ANDROID_EMOTION((short) 0x999),//17A
    ANDROID_UPDATE((short) 0x999),//17B
    ANDROID_DEACTIVATED((short) 0x999), //17C 
    SPAWN_FAMILIAR((short) 0x999),//183
    MOVE_FAMILIAR((short) 0x999),//184
    TOUCH_FAMILIAR((short) 0x999),//185
    ATTACK_FAMILIAR((short) 0x999),//186
    RENAME_FAMILIAR((short) 0x999),//187
    SPAWN_FAMILIAR_2((short) 0x999),//188
    UPDATE_FAMILIAR((short) 0x999),//189
    HAKU_CHANGE_1((short) 0x999),//18A
    HAKU_CHANGE_0((short) 0x999),//18B
    HAKU_MOVE((short) 0x999),//18B
    HAKU_UNK((short) 0x999),//18C
    HAKU_CHANGE((short) 0x999),//18D
    SPAWN_HAKU((short) 0x999),//190
    MOVE_ATTACK((short) 0x999),//19A
    SHOW_TITLE((short) 0x999),//1A1
    ANGELIC_CHANGE((short) 0x999),//1A2
    LOAD_TEAM((short) 0x999),//1AD
    PVP_HP((short) 0x999),//1B0
    DIRECTION_FACIAL_EXPRESSION((short) 0x999),//1E7
    MOVE_SCREEN((short) 0x999),//1E8
    MAP_FADE((short) 0x999),//1F0
    MAP_FADE_FORCE((short) 0x999),//1F1
    HP_DECREASE((short) 0x999),//1F3
    PLAY_EVENT_SOUND((short) 0x999),//1F6
    PLAY_MINIGAME_SOUND((short) 0x999),//1F7
    
    ARAN_COMBO_RECHARGE((short) 0x999),//204
    RANDOM_EMOTION((short) 0x999),//205
    RADIO_SCHEDULE((short) 0x999),//206
    OPEN_SKILL_GUIDE((short) 0x999),//207

    BUFF_ZONE_EFFECT((short) 0x999),//20C
    GO_CASHSHOP_SN((short) 0x999),//20D
    DAMAGE_METER((short) 0x999),//20E
    TIME_BOMB_ATTACK((short) 0x999),//20F
    FOLLOW_MOVE((short) 0x999),//20D
    FOLLOW_MSG((short) 0x999),//211
    AP_SP_EVENT((short) 0x999),//215
    QUEST_GUIDE_NPC((short) 0x999),//214
    REGISTER_FAMILIAR((short) 0x999),//218
    FAMILIAR_MESSAGE((short) 0x999),//219
    CREATE_ULTIMATE((short) 0x999),//21A
    HARVEST_MESSAGE((short) 0x999),//21C
    SHOW_MAP_NAME((short) 0x999),
    OPEN_BAG((short) 0x999),//21D
    DRAGON_BLINK((short) 0x999),//21E
    PVP_ICEGAGE((short) 0x999),//21F
    DIRECTION_INFO((short) 0x999),//223
    REISSUE_MEDAL((short) 0x999),//224
    PLAY_MOVIE((short) 0x999),//227
    CAKE_VS_PIE((short) 0x999),//225
    PHANTOM_CARD((short) 0x999),//226
    LUMINOUS_COMBO((short) 0x999),//229
    MOVE_SCREEN_X((short) 0x999),//199
    MOVE_SCREEN_DOWN((short) 0x999),//19A
    CAKE_PIE_INSTRUMENTS((short) 0x999),//19B
    SEALED_BOX((short) 0x999),//212
    PVP_SUMMON((short) 0x999),//269
    SUMMON_SKILL_2((short) 0x999),
    SUMMON_DELAY((short) 0x999),
    MONSTER_SKILL((short) 0x999),//281
    SKILL_EFFECT_MOB((short) 0x999),//283
    TELE_MONSTER((short) 0x999),
    MONSTER_CRC_CHANGE((short) 0x999),//285
    ITEM_EFFECT_MOB((short) 0x999),//288
    MONSTER_PROPERTIES((short) 0x999),
    REMOVE_TALK_MONSTER((short) 0x999),
    TALK_MONSTER((short) 0x999),
    CYGNUS_ATTACK((short) 0x999),
    MONSTER_RESIST((short) 0x999),
    AZWAN_MOB_TO_MOB_DAMAGE((short) 0x999),
    AZWAN_SPAWN_MONSTER((short) 0x999),
    AZWAN_KILL_MONSTER((short) 0x999),
    AZWAN_SPAWN_MONSTER_CONTROL((short) 0x999),
   // NPC_ACTION((short) 0x999),//2A6
    NPC_ACTION((short) 0x107),
    NPC_TOGGLE_VISIBLE((short) 0x999),//2AA
    INITIAL_QUIZ((short) 0x999),//2AC
    NPC_UPDATE_LIMITED_INFO((short) 0x999),//2AD
    NPC_SET_SPECIAL_ACTION((short) 0x999),//2AE
    NPC_SCRIPTABLE((short) 0x999),//2AF
    RED_LEAF_HIGH((short) 0x999),//2B0
    MECH_DOOR_SPAWN((short) 0x999),
    MECH_DOOR_REMOVE((short) 0x999),
    SPAWN_EXTRACTOR((short) 0x999),//2C5
    REMOVE_EXTRACTOR((short) 0x999),//2C6
    MOVE_HEALER((short) 0x999),
    PULLEY_STATE((short) 0x999),
    MONSTER_CARNIVAL_LEAVE((short) 0x999),//2D0
    MONSTER_CARNIVAL_RESULT((short) 0x999),//2D1
    MONSTER_CARNIVAL_RANKING((short) 0x999),//2D8
    ARIANT_SCORE_UPDATE((short) 0x999),
    SHEEP_RANCH_INFO((short) 0x999),
    SHEEP_RANCH_CLOTHES((short) 0x999),//0x302
    WITCH_TOWER((short) 0x999),//0x303
    EXPEDITION_CHALLENGE((short) 0x999),//0x304
    ZAKUM_SHRINE((short) 0x999),
    
    PVP_TYPE((short) 0x999),
    PVP_TRANSFORM((short) 0x999),
    PVP_DETAILS((short) 0x999),
    PVP_ENABLED((short) 0x999),
    PVP_SCORE((short) 0x999),
    PVP_RESULT((short) 0x999),
    PVP_TEAM((short) 0x999),
    PVP_SCOREBOARD((short) 0x999),
    PVP_POINTS((short) 0x999),
    PVP_KILLED((short) 0x999),
    PVP_MODE((short) 0x999),
    PVP_ICEKNIGHT((short) 0x313),//
    HORNTAIL_SHRINE((short) 0x999),
    CAPTURE_FLAGS((short) 0x999),
    CAPTURE_POSITION((short) 0x999),
    CAPTURE_RESET((short) 0x999),
    PINK_ZAKUM_SHRINE((short) 0x999),
    LOGOUT_GIFT((short) 0x999),
    TOURNAMENT((short) 0x999),
    TOURNAMENT_MATCH_TABLE((short) 0x999),
    TOURNAMENT_SET_PRIZE((short) 0x999),
    TOURNAMENT_UEW((short) 0x999),
    TOURNAMENT_CHARACTERS((short) 0x999),
    WEDDING_PROGRESS((short) 0x999),
    WEDDING_CEREMONY_END((short) 0x999),
    PACKAGE_OPERATION((short) 0x999),//359
    CS_CHARGE_CASH((short) 0x999),
    CS_EXP_PURCHASE((short) 0x999),
    GIFT_RESULT((short) 0x999),
    CHANGE_NAME_CHECK((short) 0x999),
    CHANGE_NAME_RESPONSE((short) 0x999),
    CS_MESO_UPDATE((short) 0x999),//35F
    //0x314 int itemid int sn
    CASH_SHOP((short) 0x999),//372
    CASH_SHOP_UPDATE((short) 0x999),//373
    GACHAPON_STAMPS((short) 0x999),
    FREE_CASH_ITEM((short) 0x999),
    CS_SURPRISE((short) 0x999),
    XMAS_SURPRISE((short) 0x999),
    ONE_A_DAY((short) 0x999),
    NX_SPEND_GIFT((short) 0x999),
    RECEIVE_GIFT((short) 0x999),//new v145
    RANDOM_CHECK((short) 0x999),//25E
    PET_AUTO_CURE((short) 0x999),//379

    GM_ERROR((short) 0x999),
    ALIEN_SOCKET_CREATOR((short) 0x999),
    GOLDEN_HAMMER((short) 0x999),
    BATTLE_RECORD_DAMAGE_INFO((short) 0x999),
    CALCULATE_REQUEST_RESULT((short) 0x999),
    BOOSTER_PACK((short) 0x999),
    BOOSTER_FAMILIAR((short) 0x999),
    BLOCK_PORTAL((short) 0x999),
    NPC_CONFIRM((short) 0x999),
    RSA_KEY((short) 0x999),
    BUFF_BAR((short) 0x999),
    GAME_POLL_REPLY((short) 0x999),
    GAME_POLL_QUESTION((short) 0x999),
    ENGLISH_QUIZ((short) 0x999),
    BOAT_EFFECT((short) 0x999),
    SIDEKICK_OPERATION((short) 0x999),
    FARM_PACKET1((short) 0x999),
    FARM_ITEM_PURCHASED((short) 0x999),
    FARM_ITEM_GAIN((short) 0x999),
    HARVEST_WARU((short) 0x999),
    FARM_MONSTER_GAIN((short) 0x999),
    FARM_INFO((short) 0x999),
    FARM_MONSTER_INFO((short) 0x999),
    FARM_QUEST_DATA((short) 0x999),
    FARM_QUEST_INFO((short) 0x999),
    FARM_MESSAGE((short) 0x999),//36C
    UPDATE_MONSTER((short) 0x999),
    AESTHETIC_POINT((short) 0x999),
    UPDATE_WARU((short) 0x999),
    FARM_EXP((short) 0x999),
    FARM_PACKET4((short) 0x999),
    QUEST_ALERT((short) 0x999),
    FARM_PACKET8((short) 0x999),
    FARM_FRIENDS_BUDDY_REQUEST((short) 0x999),
    FARM_FRIENDS((short) 0x999),
    FARM_USER_INFO((short) 0x999),//388
    FARM_AVATAR((short) 0x999),//38A
    FRIEND_INFO((short) 0x999),//38D
    FARM_RANKING((short) 0x999),//38F
    SPAWN_FARM_MONSTER1((short) 0x999),//393
    SPAWN_FARM_MONSTER2((short) 0x999),//394
    RENAME_MONSTER((short) 0x999),//395
    STRENGTHEN_UI((short) 0x999),//402
    //Unplaced:
    DEATH_COUNT((short) 0x999),
    REDIRECTOR_COMMAND((short) 0x999);

    private short code = -2;

    @Override
    public void setValue(short code) {
        this.code = code;
    }

    @Override
    public short getValue() {
        return getValue(true);
    }

    public short getValue(boolean show) {
        return code;
    }

    private SendPacketOpcode(short code) {
        this.code = code;
    }

    public String getType(short code) {
        String type = null;
        if (code >= 0 && code < 0xE || code >= 0x17 && code < 0x21) {
            type = "CLogin";
        } else if (code >= 0xE && code < 0x17) {
            type = "LoginSecure";
        } else if (code >= 0x21 && code < 0xCB) {
            type = "CWvsContext";
        } else if (code >= 0xD2) {
            type = "CField";
        }
        return type;
    }

    public static String getOpcodeName(int value) {
        for (SendPacketOpcode opcode : SendPacketOpcode.values()) {
            if (opcode.getValue(false) != 0x999) {
                if (opcode.getValue(false) == value) {
                    return opcode.name();
                }
            }
        }
        return "UNKNOWN";
    }

    public static boolean isSpamHeader(SendPacketOpcode opcode) {
        switch (opcode) {
            case PING:
            //case AUTH_RESPONSE:
            //case SERVERLIST:
            //case UPDATE_STATS:
            case MOVE_PLAYER:
            case SPAWN_NPC:
            case SPAWN_NPC_REQUEST_CONTROLLER:
            case REMOVE_NPC:
            case MOVE_MONSTER:
            case MOVE_MONSTER_RESPONSE:
            case SPAWN_MONSTER:
            case SPAWN_MONSTER_CONTROL:
                //case HAKU_MOVE:
                /*case MOVE_SUMMON:
             case MOVE_FAMILIAR:
            
             case ANDROID_MOVE:
             case INVENTORY_OPERATION:*/
                //case MOVE_PET:
                //case SHOW_SPECIAL_EFFECT:
                //case DROP_ITEM_FROM_MAPOBJECT:
                //case REMOVE_ITEM_FROM_MAP:
                //case UPDATE_PARTYMEMBER_HP:
                //case DAMAGE_PLAYER:
                //case SHOW_MONSTER_HP:
                //case CLOSE_RANGE_ATTACK:
                //case RANGED_ATTACK:
                //case ARAN_COMBO:
                //case REMOVE_BG_LAYER:
                //case SPECIAL_STAT:
                //case TOP_MSG:
                case NPC_ACTION:
//            case ANGELIC_CHANGE:
                //case UPDATE_CHAR_LOOK:
                //case KILL_MONSTER:
                return true;
        }
        return false;
    }
}
