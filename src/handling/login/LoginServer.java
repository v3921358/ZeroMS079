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
package handling.login;

import constants.GameConstants;
import constants.ServerConfig;
import handling.MapleServerHandler;
import handling.netty.ServerConnection;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ServerProperties;
import static tools.FileoutputUtil.CurrentReadable_Time;
import tools.Pair;
import tools.Triple;

public class LoginServer {

    public static short PORT = 8484;
    private static ServerConnection acceptor;
    private static Map<Integer, Integer> load = new HashMap<>();
    private static String serverName, eventMessage;
    private static int flag;
    private static int maxCharacters, userLimit, usersOn = 0;
    private static boolean finishedShutdown = true, adminOnly = false;
    private static final HashMap<Integer, Triple<String, String, Integer>> loginAuth = new HashMap<>();
    private static final HashSet<String> loginIPAuth = new HashSet<>();
    private static final Map<Integer, Integer> LoginKey = new HashMap<>();
    private static Map<Integer, Pair<Integer, Integer>> chrpos = new HashMap<>();

    public static void putLoginAuth(int chrid, String ip, String tempIP, int channel) {
        Triple<String, String, Integer> put = loginAuth.put(chrid, new Triple<>(ip, tempIP, channel));
        loginIPAuth.add(ip);
    }

    public static Triple<String, String, Integer> getLoginAuth(int chrid) {
        return loginAuth.remove(chrid);
    }

    public static boolean containsIPAuth(String ip) {
        return loginIPAuth.contains(ip);
    }

    public static void removeIPAuth(String ip) {
        loginIPAuth.remove(ip);
    }

    public static void addIPAuth(String ip) {
        loginIPAuth.add(ip);
    }

    public static final void addChannel(final int channel) {
        load.put(channel, 0);
    }

    public static final void removeChannel(final int channel) {
        load.remove(channel);
    }

    public static final void run_startup_configurations() {
        userLimit = ServerConfig.userLimit;
        serverName = ServerConfig.serverName;
        eventMessage = ServerConfig.eventMessage;
        flag = ServerConfig.flag;
        adminOnly = ServerConfig.adminOnly;
        maxCharacters = ServerConfig.maxCharacters;
        PORT = Short.parseShort(ServerProperties.getProperty("LoginPort"));
        try {
            acceptor = new ServerConnection(PORT, 0, -1, false);
            acceptor.run();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】登录端口:" + Short.toString(PORT) + "");
        } catch (Exception e) {
            System.err.println(" Failed!");
            System.err.println("Could not bind to port " + PORT + ": " + e);
        }
    }

    public static final void shutdown() {
        if (finishedShutdown) {
            System.out.println("【" + CurrentReadable_Time() + "】【服务器】已经关闭了...无法执行此操作");
            return;
        }
        System.out.println("【" + CurrentReadable_Time() + "】【服务器】关闭中...");
        acceptor.close();
        System.out.println("【" + CurrentReadable_Time() + "】【服务器】关闭完成...");
        finishedShutdown = true; //nothing. lol
    }

    public static final String getServerName() {
        return serverName;
    }

    public static final String getTrueServerName() {
        return serverName.substring(0, serverName.length() - (GameConstants.GMS ? 2 : 3));
    }

    public static String getEventMessage() {
        return eventMessage;
    }

    public static int getMaxCharacters() {
        return maxCharacters;
    }

    public static Map<Integer, Integer> getLoad() {
        return load;
    }

    public static void setLoad(final Map<Integer, Integer> load_, final int usersOn_) {
        load = load_;
        usersOn = usersOn_;
    }

    public static String getEventMessage(int world) { //TODO: Finish this
        switch (world) {
            case 0:
                return null;
        }
        return null;
    }

    public static final void setFlag(final byte newflag) {
        flag = newflag;
    }

    public static int getFlag() {
        return flag;
    }

    public static final int getUserLimit() {
        return userLimit;
    }

    public static final int getUsersOn() {
        return usersOn;
    }

    public static final void setUserLimit(final int newLimit) {
        userLimit = newLimit;
    }

    public static final boolean isAdminOnly() {
        return adminOnly;
    }

    public static final boolean isShutdown() {
        return finishedShutdown;
    }

    public static final void setOn() {
        finishedShutdown = false;
    }

    public static boolean CanLoginKey(int key, int AccID) {
        if (LoginKey.containsValue(key)) {
            if (LoginKey.get(AccID).equals(key)) {
                return true;
            }
        }
        return false;
    }

    public static boolean RemoveLoginKey(int AccID) {
        LoginKey.remove(AccID);
        return true;
    }
   
    public static boolean addLoginKey(int key, int AccID) {
        if (LoginKey.get(AccID) == null) {
            LoginKey.put(AccID, key);
        }
        LoginKey.remove(AccID);
        LoginKey.put(AccID, key);
        return true;
    }

    public static int getLoginKey(int AccID) {
        return LoginKey.get(AccID);
    }

    public static void setChrPos(int keys, int x, int y) {
        chrpos.put(keys, new Pair<>(x, y));
    }

    public static Map<Integer, Pair<Integer, Integer>> getChrPos() {
        return chrpos;
    }

    public static void RemoveChrPos(int chrid) {
        chrpos.remove(chrid);
    }
    
    public static boolean getAutoReg() {
        return ServerConfig.AUTO_REGISTER;
    }

    public static void setAutoReg(boolean x) {
        ServerConfig.AUTO_REGISTER = x;
    }
    public static int getRSGS() {
        return ServerConfig.RSGS;
    }

    public static void setRSGS(int x) {
        ServerConfig.RSGS = x;
    }

    private LoginServer() {
    }
}
