/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import client.MapleJob;
import client.Skill;
import client.SkillFactory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.ItemInformation;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.quest.MapleQuest;

/**
 *
 * @author alienware
 */
public class SearchGenerator {

    public enum SearchType {
        道具(1),
        NPC(2),
        地图(3),
        怪物(4),
        任务(5),
        技能(6),
        职业(7),
        伺服器包头(8),
        用户端包头(9),
        髮型(10),
        脸型(11),
        未知;

        private int value;

        SearchType() {
            this.value = 0;
        }

        SearchType(int value) {
            this.value = value;
        }

        public final int getValue() {
            return value;
        }

        public static String nameOf(int value) {
            for (SearchType type : SearchType.values()) {
                if (type.getValue() == value) {
                    return type.name();
                }
            }
            return "未知";
        }
    }
    public static final int 道具 = SearchType.道具.getValue();
    public static final int NPC = SearchType.NPC.getValue();
    public static final int 地图 = SearchType.地图.getValue();
    public static final int 怪物 = SearchType.怪物.getValue();
    public static final int 任务 = SearchType.任务.getValue();
    public static final int 技能 = SearchType.技能.getValue();
    public static final int 职业 = SearchType.职业.getValue();
    public static final int 髮型 = SearchType.髮型.getValue();
    public static final int 脸型 = SearchType.脸型.getValue();
    private static final Map<SearchType, Map<Integer, String>> searchs = new HashMap();

    public static Map<Integer, String> getSearchs(int type) {
        return getSearchs(SearchType.valueOf(SearchType.nameOf(type)));
    }

    public static Map<Integer, String> getSearchs(SearchType type) {
        Map<Integer, String> value = null;
        if (searchs.containsKey(type)) {
            return searchs.get(type);
        } else if ((type == SearchType.脸型 || type == SearchType.髮型) && searchs.containsKey(SearchType.道具)) {
            value = searchs.get(SearchType.道具);
        }

        Map<Integer, String> values = new TreeMap();

        switch (type) {
            case 道具:
                for (ItemInformation itemInfo : MapleItemInformationProvider.getInstance().getAllItems()) {
                    values.put(itemInfo.itemId, itemInfo.name);
                }
                break;
            case NPC:
                values = MapleLifeFactory.getNPCNames();
                break;
            case 地图:
                MapleData data = MapleDataProviderFactory.getDataProvider(new File((System.getProperty("wzpath") != null ? System.getProperty("wzpath") : "") + "wz/String.wz")).getData("Map.img");
                for (MapleData mapAreaData : data.getChildren()) {
                    for (MapleData mapIdData : mapAreaData.getChildren()) {
                        values.put(Integer.parseInt(mapIdData.getName()), "'" + MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "无名称") + " : " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "无名称") + "'");
                    }
                }
                break;
            case 怪物:
                for (Pair<Integer, String> b : MapleMonsterInformationProvider.getInstance().getAllMonsters()) {
                    values.put(b.getLeft(), b.getRight());
                }
                break;
            case 任务:
                for (MapleQuest quest : MapleQuest.getAllInstances()) {
                    values.put(quest.getId(), quest.getName());
                }
                break;
            case 技能: {
                for (Skill skill : SkillFactory.getAllSkills()) {
                    values.put(skill.getId(), skill.getName());
                }
                break;
            }
            case 职业:
                for (MapleJob job : MapleJob.values()) {
                    values.put(job.getId(), job.name());
                }
                break;
            case 伺服器包头:
                for (SendPacketOpcode send : SendPacketOpcode.values()) {
                    values.put((int) send.getValue(), send.name());
                }
                break;
            case 用户端包头:
                for (RecvPacketOpcode recv : RecvPacketOpcode.values()) {
                    values.put((int) recv.getValue(), recv.name());
                }
                break;
            case 髮型:
            case 脸型:
                if (value == null) {
                    value = new TreeMap();
                    for (ItemInformation itemInfo : MapleItemInformationProvider.getInstance().getAllItems()) {
                        value.put(itemInfo.itemId, itemInfo.name);
                    }
                    searchs.put(SearchType.道具, value);
                }
                MapleInventoryType iType = type == SearchType.髮型 ? MapleInventoryType.HAIR : MapleInventoryType.FACE;
                for (Map.Entry<Integer, String> entry : value.entrySet()) {
                    if (GameConstants.getInventoryType(entry.getKey()) == iType) {
                        values.put(entry.getKey(), entry.getValue());
                    }
                }
                break;
        }

        searchs.put(type, values);
        return values;
    }

    public static Map<Integer, String> getSearchData(int type, String search) {
        return getSearchData(SearchType.valueOf(SearchType.nameOf(type)), search);
    }

    public static Map<Integer, String> getSearchData(SearchType type, String search) {
        Map<Integer, String> values = new TreeMap();
        Map<Integer, String> ss = getSearchs(type);

        for (int i : ss.keySet()) {
            if (String.valueOf(i).toLowerCase().contains(search.toLowerCase()) || ss.get(i).toLowerCase().contains(search.toLowerCase())) {
                values.put(i, ss.get(i));
            }
        }

        return values;
    }

    public static String searchData(int type, String search) {
        return searchData(SearchType.valueOf(SearchType.nameOf(type)), search);
    }

    public static String searchData(SearchType type, String search) {
        Map<Integer, String> ss = getSearchData(type, search);
        List<String> ret = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        switch (type) {
            case 道具:
                if (!search.isEmpty()) {
                    for (Integer i : ss.keySet()) {
                        if (MapleItemInformationProvider.getInstance().itemExists(i)) {
                            ret.add("\r\n#L" + i + "##z" + i + "# (" + i + ")#l");
                            //System.out.println(i+"  "+MapleItemInformationProvider.getInstance().getName(i));
                            //ret.add("\r\n#L" + i + "##i" + i + ":# #z" + i + "#(" + i + ")#l");
                        }
                    }
                } else {
                    sb.append("\r\n请输入道具名称....");
                }
                break;
            case NPC:
                for (Map.Entry<Integer, String> i : ss.entrySet()) {
                    ret.add("\r\n#L" + i.getKey() + "##p" + i.getKey() + "#(" + i.getKey() + ")#l");
                }
                break;
            case 地图:
                for (Integer i : ss.keySet()) {
                    ret.add("\r\n#L" + i + "##m" + i + "#(" + i + ")#l");
                }
                break;
            case 怪物:
                for (Integer i : ss.keySet()) {
                    ret.add("\r\n#L" + i + "##o" + i + "#(" + i + ")#l");
                }
                break;
            case 任务:
                for (Map.Entry<Integer, String> i : ss.entrySet()) {
                    ret.add("\r\n#L" + i.getKey() + "#" + i.getValue() + "(" + i.getKey() + ")#l");
                }
                break;
            case 技能:
                for (Map.Entry<Integer, String> i : ss.entrySet()) {
                    ret.add("\r\n#L" + i.getKey() + "##s" + i.getKey() + "#" + i.getValue() + "(" + i.getKey() + ")#l");
                }
                break;
            case 职业:
                for (Map.Entry<Integer, String> i : ss.entrySet()) {
                    ret.add("\r\n#L" + i.getKey() + "#" + i.getValue() + "(" + i.getKey() + ")#l");
                }
                break;
            case 伺服器包头:
            case 用户端包头:
                for (Map.Entry<Integer, String> i : ss.entrySet()) {
                    ret.add("\r\n" + i.getValue() + " 值: " + i.getKey() + " 16进制: " + HexTool.getOpcodeToString(i.getKey()));
                }
                break;
            default:
                sb.append("对不起, 这个检索类型不被支援");
        }

        if (ret.size() > 0) {
            for (String singleRetItem : ret) {
                if (sb.length() > 35000) {
                    sb.append("\r\n\r\n后面还有很多搜寻结果, 但已经无法显示更多");
                    break;
                }
                sb.append(singleRetItem);
            }
        }

        StringBuilder sbs = new StringBuilder();
        if (!sb.toString().isEmpty() && !sb.toString().equalsIgnoreCase("对不起, 这个检索指令不被支援")) {
            sbs.append("<<类型: ").append(type.name()).append(" | 搜寻讯息: ").append(search).append(">>");
        }
        sbs.append(sb);
//        System.out.println(sbs.toString());
        if (sbs.toString().isEmpty()) {
            sbs.append("搜寻不到此").append(type.name());
        }
        return sbs.toString();
    }

    public static String searchItem(final String text) {
        if (text.isEmpty()) {
            return "请输入物品名称。";
        }
        StringBuilder sb = new StringBuilder();
        final Map<Integer, String> items = getSearchData(SearchType.valueOf(SearchType.nameOf(1)), text);
        for (Integer i : items.keySet()) {
            if (MapleItemInformationProvider.getInstance().itemExists(i)) {
                sb.append("\r\n#L" + i + "##z" + i + "# #l");
            }
        }
        if (sb.length() <= 0) {
            return "搜寻不到此物品,请重新输入关键字。";
        }
        return sb.toString();
    }
    
    public static boolean foundData(int type, String search) {
        return !getSearchData(type, search).isEmpty();
    }

    private SearchGenerator() {
    }

}
