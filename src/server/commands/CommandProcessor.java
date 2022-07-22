/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package server.commands;

import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConfig;
import constants.ServerConstants.CommandType;
import constants.ServerConstants.PlayerGMRank;
import database.DBConPool;
import handling.channel.ChannelServer;
import handling.world.World;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.FileoutputUtil;
import tools.packet.CWvsContext;

public class CommandProcessor {

    private final static HashMap<String, MapleCommand> commands = new HashMap<>();
    private final static HashMap<Integer, ArrayList<String>> commandList = new HashMap<>();
    private final static List<String> showcommands = new LinkedList<>();
    private static final Logger logger = LogManager.getLogger(CommandProcessor.class);

    static {

        Class<?>[] CommandFiles = {
            PlayerCommand.class,
            InternCommand.class,
            GMCommand.class,
            SuperGMCommand.class,
            AdminCommand.class
        };

        for (Class<?> clasz : CommandFiles) {
            try {
                PlayerGMRank rankNeeded = (PlayerGMRank) clasz.getMethod("getPlayerLevelRequired", new Class<?>[]{}).invoke(null, (Object[]) null);
                Class<?>[] a = clasz.getDeclaredClasses();
                ArrayList<String> cL = new ArrayList<>();
                for (Class<?> c : a) {
                    try {
                        if (!Modifier.isAbstract(c.getModifiers()) && !c.isSynthetic()) {
                            Object o = c.newInstance();
                            boolean enabled;
                            try {
                                enabled = c.getDeclaredField("enabled").getBoolean(c.getDeclaredField("enabled"));
                            } catch (NoSuchFieldException ex) {
                                enabled = true; //Enable all coded commands by default.
                            }
                            if (o instanceof CommandExecute && enabled) {
                                cL.add(rankNeeded.getCommandPrefix() + c.getSimpleName().toLowerCase());
                                commands.put(rankNeeded.getCommandPrefix() + c.getSimpleName().toLowerCase(), new MapleCommand(rankNeeded.getCommandPrefix() + c.getSimpleName().toLowerCase(), (CommandExecute) o, rankNeeded.getLevel()));
                                showcommands.add(rankNeeded.getCommandPrefix() + c.getSimpleName().toLowerCase());
                                //if (!rankNeeded.getCommandPrefix().equals(PlayerGMRank.GM.getCommandPrefix()) && !rankNeeded.getCommandPrefix().equals(PlayerGMRank.NORMAL.getCommandPrefix())) { //add it again for GM
                                //    commands.put("!" + c.getSimpleName().toLowerCase(), new MapleCommand((CommandExecute) o, PlayerGMRank.GM.getLevel()));
                                // }
                            }
                        }
                    } catch (InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException ex) {
                        FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
                    }
                }
                Collections.sort(cL);
                commandList.put(rankNeeded.getLevel(), cL);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
            }
        }
    }

    private static void sendDisplayMessage(MapleClient c, String msg, CommandType type) {
        if (c.getPlayer() == null) {
            return;
        }
        switch (type) {
            case NORMAL:
                c.getPlayer().dropMessage(6, msg);
                break;
            case TRADE:
                c.getPlayer().dropMessage(-2, "Error : " + msg);
                break;
        }
    }

    public static void dropHelp(MapleClient c) {
        final StringBuilder sb = new StringBuilder("指令列表:\r\n");
        int check = 0;

        check = c.getPlayer().getGMLevel();

        for (int i = 0; i <= check; i++) {
            if (commandList.containsKey(i)) {
                sb.append("权限等级： ").append(i).append("\r\n");
                for (String s : commandList.get(i)) {
                    MapleCommand co = commands.get(s);
                    sb.append(co.getMessage());
                    sb.append(" \r\n");
                }
            }
        }
        c.getPlayer().dropNPC(sb.toString());
    }

    public static boolean processCommand(MapleClient c, String line, CommandType type) {
        for (PlayerGMRank prefix : PlayerGMRank.values()) {
            if (line.startsWith(prefix.getCommandPrefix() + prefix.getCommandPrefix())) {
                return false;
            }
        }
        if (String.valueOf(line.charAt(0)).equals(PlayerGMRank.NORMAL.getCommandPrefix())) {
            String[] splitted = line.split(" ");
            splitted[0] = splitted[0].toLowerCase();

            MapleCommand co = commands.get(splitted[0]);
            if (co == null || co.getType() != type) {
                sendDisplayMessage(c, "没有这个指令,可以使用 @help 来查看指令。", type);
                return true;
            }
            try {
                boolean ret = co.execute(c, splitted);
                if (!ret) {
                    c.getPlayer().dropMessage(6, "指令错误，用法： " + co.getMessage());

                }
            } catch (Exception e) {
                sendDisplayMessage(c, "有错误.", type);
                if (c.getPlayer().isGM()) {
                    sendDisplayMessage(c, "错误: " + e, type);
                    FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
                }
            }
            return true;
        }
         if (!ServerConfig.Gameinstructions) {
            c.getPlayer().dropMessage(1, "管理员已经从后台禁止使用GM指令");
            }
        if (c.getPlayer().getGMLevel() > PlayerGMRank.NORMAL.getLevel()) {
            /*if (line.charAt(0) == '`' && c.getPlayer().getGMLevel() > 2) {
                for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.broadcastGMMessage(tools.packet.CField.multiChat("[GM Chat] " + c.getPlayer().getName(), line.substring(1), 6));
                }
                return true;
            }*/
            if (line.split(" ")[0].equals("cmd") || String.valueOf(line.charAt(0)).equals(PlayerGMRank.INTERN.getCommandPrefix()) || String.valueOf(line.charAt(0)).equals(PlayerGMRank.GM.getCommandPrefix()) || String.valueOf(line.charAt(0)).equals(PlayerGMRank.SUPERGM.getCommandPrefix())) { //Redundant for now, but in case we change symbols later. This will become extensible.
                String[] splitted = line.split(" ");
                splitted[0] = splitted[0].toLowerCase();
                List<String> show = new LinkedList<>();
                for (String com : showcommands) {
                    if (com.contains(splitted[0])) {
                        show.add(com);
                    }
                }
                if (show.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    int iplength = splitted[0].length();
                    for (String com : showcommands) {// 循环出所有指令
                        int sclength = com.length();

                        String[] next = new String[sclength];// true值数量 必须=指令长度名称
                        for (int i = 0; i < next.length; i++) {
                            next[i] = "false";
                        }

                        if (iplength == sclength) {// 第一步先以长度当判断
                            for (int i = 0; i < sclength; i++) {
                                String st = com.substring(i, i + 1);
                                for (int r = 0; r < iplength; r++) {
                                    String it = splitted[0].substring(r, r + 1);
                                    if (st.equals(it)) {
                                        next[i] = "true";
                                    }
                                }
                            }
                            boolean last = true;
                            for (int i = 0; i < next.length; i++) {// 阵列内所有值皆为true即正确
                                if ("false".equals(next[i])) {
                                    last = false;
                                }
                            }
                            if (last) {
                                if (show.isEmpty()) {
                                    show.add(com);
                                }
                            }
                        }
                    }
                }
                if (show.size() == 1) {
                    if (!splitted[0].equals(show.get(0))) {
                        sendDisplayMessage(c, "自动识别关联指令[" + show.get(0) + "].", type);
                        splitted[0] = show.get(0);
                    }
                }

                MapleCommand co = commands.get(splitted[0]);
                if (co == null || co.getType() != type) {
                    if (splitted[0].equals(line.charAt(0) + "help")) {
                        dropHelp(c);
                        return true;
                    } else if (show.isEmpty()) {
                        sendDisplayMessage(c, "指令[" + splitted[0] + "]不存在.", type);
                    } else if (String.valueOf(PlayerGMRank.INTERN.getCommandPrefix()).equals(splitted[0])) {
                        sendDisplayMessage(c, "指令不存在.", type);
                    } else {
                        sendDisplayMessage(c, "相关指令为: " + show.toString(), type);
                    }
                    return true;
                }
                boolean CanUseCommand = false;
                if (c.getPlayer().getGMLevel() >= co.getReqGMLevel()) {
                    CanUseCommand = true;
                }
                if (!CanUseCommand) {
                    sendDisplayMessage(c, "你没有权限可以使用指令.", type);
                    return true;
                }
                // 开始处理指令(GM区)
                if (c.getPlayer() != null) {
                    boolean ret = false;
                    try {
                        //执行指令
                        ret = co.execute(c, splitted);
                        // return ret;

                        if (ret) {
                            //指令log到DB
                            if (c.getPlayer().isGM()) {
                                logCommandToDB(c.getPlayer(), line, "gmlog");
                                World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, "[管理员公告] " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ")使用了指令 " + line + " ---在地图「" + c.getPlayer().getMapId() + "」频道：" + c.getChannel()));
                                FileoutputUtil.logToFile("logs/Data/管理员命令.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSessionIPAddress() + " 帐号: " + c.getAccountName() + " 玩家: " + c.getPlayer().getName() + " 使用管理员命令:" + line);
                            } else {
                                logCommandToDB(c.getPlayer(), line, "internlog");
                            }
                            // 讯息处理
                            //ShowMsg(c, line, type);
                        } else {
                            c.getPlayer().dropMessage(6, "指令错误，用法： " + co.getMessage());
                        }
                    } catch (Exception e) {
                        FileoutputUtil.outputFileError(FileoutputUtil.CommandEx_Log, e);
                        String output = FileoutputUtil.NowTime();
                        if (c != null && c.getPlayer() != null) {
                            output += c.getPlayer().getName() + "(" + c.getPlayer().getId() + ")使用了指令 " + line + " ---在地图「" + c.getPlayer().getMapId() + "」频道：" + c.getChannel();
                        }
                        FileoutputUtil.logToFile(FileoutputUtil.CommandEx_Log, output + " \r\n");
                    }
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    public static void logCommandToDB(MapleCharacter player, String command, String table) {

        try (Connection con = DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO " + table + " (cid, command, mapid) VALUES (?, ?, ?)");
            ps.setInt(1, player.getId());
            ps.setString(2, command);
            ps.setInt(3, player.getMap().getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.MysqlEx_Log, ex);
            System.err.println(ex);
        }
    }

    public static String getCommandsForLevel(int level) {
        String commandlist = "";
        for (int i = 0; i < commandList.get(level).size(); i++) {
            commandlist += commandList.get(level).get(i);
            if (i + 1 < commandList.get(level).size()) {
                commandlist += ", ";
            }
        }
        return commandlist;
    }

    private CommandProcessor() {
    }
}
