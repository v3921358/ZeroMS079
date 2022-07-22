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
package handling.channel;

import client.MapleCharacter;
import constants.ServerConfig;
import constants.WorldConstants.WorldOption;
import handling.MapleServerHandler;
import handling.login.LoginServer;
import handling.netty.ServerConnection;
import handling.world.CheaterData;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scripting.EventScriptManager;
import server.MapleSquad;
import server.MapleSquad.MapleSquadType;
import server.ServerProperties;
import server.events.*;
import server.life.PlayerNPC;
import server.maps.AramiaFireWorks;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.stores.HiredMerchant;
import tools.ConcurrentEnumMap;
import static tools.FileoutputUtil.CurrentReadable_Time;
import tools.packet.CWvsContext;

public class ChannelServer {

    public static long serverStartTime;
    private final int cashRate = 1, traitRate = 1;//宠物亲密度
    private int expRate;
    private int mesoRate;
    private int dropRate;
    private int statLimit;
    private short port = 7575;
    private static final short DEFAULT_PORT = 7575;
    private int channel, running_MerchantID = 0, flags = 0;
    private String ServerMessage,ip, serverName;
    private boolean shutdown = false, finishedShutdown = false, MegaphoneMuteState = false, adminOnly = false;
    private PlayerStorage players;
    private ServerConnection acceptor;
    private final MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private final AramiaFireWorks works = new AramiaFireWorks();
    private static final Map<Integer, ChannelServer> instances = new HashMap<>();
    private final Map<MapleSquadType, MapleSquad> mapleSquads = new ConcurrentEnumMap<>(MapleSquadType.class);
    private final Map<Integer, HiredMerchant> merchants = new HashMap<>();
    private final List<PlayerNPC> playerNPCs = new LinkedList<>();
    private final ReentrantReadWriteLock merchLock = new ReentrantReadWriteLock(); //merchant
    private int eventmap = -1;
    private final Map<MapleEventType, MapleEvent> events = new EnumMap<>(MapleEventType.class);
    public boolean eventOn = false;
    public int eventMap = 0;
    private boolean eventWarp;
    private String eventHost;
    private String eventName;
    private boolean manualEvent = false;
    private int manualEventMap = 0;
    private boolean bomberman = false;
    private boolean marketUseBoard = false;//市场说话黑板

    private ChannelServer(final int channel) {
        this.channel = channel;
        mapFactory = new MapleMapFactory(channel);
    }

    public static Set<Integer> getAllInstance() {
        return new HashSet<>(instances.keySet());
    }

    public final void loadEvents() {
        if (!events.isEmpty()) {
            return;
        }
        events.put(MapleEventType.打瓶盖, new MapleCoconut(channel, MapleEventType.打瓶盖)); //yep, coconut. same shit
        events.put(MapleEventType.打椰子, new MapleCoconut(channel, MapleEventType.打椰子));
        events.put(MapleEventType.向高地, new MapleFitness(channel, MapleEventType.向高地));
        events.put(MapleEventType.上楼上楼, new MapleOla(channel, MapleEventType.上楼上楼));
        events.put(MapleEventType.ox答题, new MapleOxQuiz(channel, MapleEventType.ox答题));
        events.put(MapleEventType.雪球赛, new MapleSnowball(channel, MapleEventType.雪球赛));
        //events.put(MapleEventType.Survival, new MapleSurvival(channel, MapleEventType.Survival));
    }

    public final void run_startup_configurations() {
        setChannel(channel); //instances.put
        try {
            boolean Doubleburstchannel = ServerConfig.Doubleburstchannel;
            int Channelnumber = ServerConfig.Channelnumber;
          //  if (双爆频道开关 == 0) {
                //if (this.channel == Integer.parseInt(ServerProperties.getProperty("THMS.双爆频道"))) {//双爆频道
                if (this.channel == Channelnumber ) {//双爆频道
                    dropRate *= 2;
                    expRate *= 2;
                    mesoRate *= 2;
                }
            statLimit = ServerConfig.statLimit;//最大能力值
            expRate = ServerConfig.expRate;
            mesoRate = ServerConfig.mesoRate;
            dropRate = ServerConfig.dropRate;
            serverName = ServerConfig.serverName;
            flags = ServerConfig.flags;
            adminOnly = ServerConfig.adminOnly;
            marketUseBoard = ServerConfig.marketUseBoard;//市场说话黑板
            eventSM = new EventScriptManager(this, ServerConfig.events.split(","));
          //  port = (short) (DEFAULT_PORT + channel);
            port = (short) ((ServerProperties.getProperty("ChannelPort", DEFAULT_PORT) + channel) - 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ip = ServerConfig.IP + ":" + port;
        players = new PlayerStorage(channel);
        loadEvents();
        try {
            acceptor = new ServerConnection(port, 0, channel, false);
            acceptor.run();
        System.out.println("【" + CurrentReadable_Time() + "】【信息】频道" + getChannel() + "端口:" + port + "");
            eventSM.init();
        } catch (Exception e) {
            System.err.println("Could not bind port " + port + " (ch: " + getChannel() + ")" + e);
        }
    }

    public final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        broadcastPacket(CWvsContext.broadcastMsg(0, "【" + CurrentReadable_Time() + "】【频道" + getChannel() + "】频道正在关闭."));
        shutdown = true;

        System.out.println("【" + CurrentReadable_Time() + "】【频道" + getChannel() + "】保存角色资料...");

        getPlayerStorage().disconnectAll();

        System.out.println("【" + CurrentReadable_Time() + "】【频道" + getChannel() + "】解除端口綁定中...");
        try {
            if (acceptor != null) {
                acceptor.close();
                System.out.println("【" + CurrentReadable_Time() + "】【频道" + getChannel() + "】解除端口成功");
            }
        } catch (Exception e) {
            System.out.println("【" + CurrentReadable_Time() + "】【频道" + getChannel() + "】解除端口失败");
        }
        //temporary while we dont have !addchannel
        instances.remove(channel);
        setFinishShutdown();
    }

    public final boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public final MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public static final ChannelServer newInstance(final int channel) {
        return new ChannelServer(channel);
    }

    public static final ChannelServer getInstance(final int channel) {
        return instances.get(channel);
    }

    public final void addPlayer(final MapleCharacter chr) {
        getPlayerStorage().registerPlayer(chr);
        
    }
    public final PlayerStorage getPlayerStorage() {
        if (players == null) { //wth
            players = new PlayerStorage(channel); //wthhhh
        }
        return players;
    }

    public final void removePlayer(final MapleCharacter chr) {
        getPlayerStorage().deregisterPlayer(chr);

    }

    public final void removePlayer(final int idz, final String namez) {
        getPlayerStorage().deregisterPlayer(idz, namez);

    }

    public final String getServerMessage() {
        return ServerConfig.ServerMessage;
    }

    public final void setServerMessage(final String newMessage) {
        //ServerMessage = newMessage;
      //  broadcastPacket(CWvsContext.broadcastMsg(ServerMessage));
            ServerConfig.ServerMessage = newMessage;
  //  }
    }

    public final void broadcastPacket(final byte[] data) {
        getPlayerStorage().broadcastPacket(data);
    }

    public final void broadcastSmegaPacket(final byte[] data) {
        getPlayerStorage().broadcastSmegaPacket(data);
    }

    public final void broadcastGMPacket(final byte[] data) {
        getPlayerStorage().broadcastGMPacket(data);
    }

    public final int getCashRate() {
        return cashRate;
    }

    public final int getChannel() {
        return channel;
    }

    public final void setChannel(final int channel) {
        instances.put(channel, this);
        LoginServer.addChannel(channel);
    }

    public static ArrayList<ChannelServer> getAllInstances() {
        return new ArrayList<>(instances.values());
    }
    
    public int getStatLimit() {
        return this.statLimit;
    }

    public void setStatLimit(int limit) {
        this.statLimit = limit;
    }

    public final String getIP() {
        return ip;
    }

    public final boolean isShutdown() {
        return shutdown;
    }

    public final int getLoadedMaps() {
        return mapFactory.getLoadedMaps();
    }

    public final EventScriptManager getEventSM() {
        return eventSM;
    }

    public final void reloadEvents() {
        eventSM.cancel();
        eventSM = new EventScriptManager(this, ServerConfig.events.split(","));
        eventSM.init();
    }

    public final int getExpRate() {
        return expRate;
    }

    public void setExpRate(int expRate) {
        this.expRate = expRate;
    }

    public final int getMesoRate() {
        return mesoRate;
    }

    public void setMesoRate(int mesoRate) {
        this.mesoRate = mesoRate;
    }

    public final int getDropRate() {
        return dropRate;
    }

    public void setDropRate(int dropRate) {
        this.dropRate = dropRate;
    }

/*    public final int getBossDropRate() {
        return BossDropRate;
    }*/
    public final int getBossRate() {
        return ServerConfig.bossRate;
    }

    public final void setBossRate(final int bossRate) {
        ServerConfig.bossRate = bossRate;
    }
    
    public final int getPetRate() {
        return ServerConfig.petRate;
    }

    public final void setPetRate(final int petRate) {
        ServerConfig.petRate = petRate;
    }

    public static void startChannel_Main() {
        serverStartTime = System.currentTimeMillis();

        for (int i = 0; i < ServerConfig.channelCount; i++) {
            newInstance(i + 1).run_startup_configurations();
        }
    }

    public Map<MapleSquadType, MapleSquad> getAllSquads() {
        return Collections.unmodifiableMap(mapleSquads);
    }

    public final MapleSquad getMapleSquad(final String type) {
        return getMapleSquad(MapleSquadType.valueOf(type.toLowerCase()));
    }

    public final MapleSquad getMapleSquad(final MapleSquadType type) {
        return mapleSquads.get(type);
    }

    public final boolean addMapleSquad(final MapleSquad squad, final String type) {
        final MapleSquadType types = MapleSquadType.valueOf(type.toLowerCase());
        if (types != null && !mapleSquads.containsKey(types)) {
            mapleSquads.put(types, squad);
            squad.scheduleRemoval();
            return true;
        }
        return false;
    }

    public final boolean removeMapleSquad(final MapleSquadType types) {
        if (types != null && mapleSquads.containsKey(types)) {
            mapleSquads.remove(types);
            return true;
        }
        return false;
    }

    public final int closeAllMerchant() {
        int ret = 0;
        merchLock.writeLock().lock();
        try {
            final Iterator<Entry<Integer, HiredMerchant>> merchants_ = merchants.entrySet().iterator();
            while (merchants_.hasNext()) {
                HiredMerchant hm = merchants_.next().getValue();
                hm.closeShop(true, false);
                //HiredMerchantSave.QueueShopForSave(hm);
                hm.getMap().removeMapObject(hm);
                merchants_.remove();
                ret++;
            }
        } finally {
            merchLock.writeLock().unlock();
        }
        //hacky
        for (int i = 910000001; i <= 910000022; i++) {
            for (MapleMapObject mmo : mapFactory.getMap(i).getAllHiredMerchantsThreadsafe()) {
                ((HiredMerchant) mmo).closeShop(true, false);
                //HiredMerchantSave.QueueShopForSave((HiredMerchant) mmo);
                ret++;
            }
        }
        return ret;
    }

    public final int addMerchant(final HiredMerchant hMerchant) {
        merchLock.writeLock().lock();
        try {
            running_MerchantID++;
            merchants.put(running_MerchantID, hMerchant);
            return running_MerchantID;
        } finally {
            merchLock.writeLock().unlock();
        }
    }

    public final void removeMerchant(final HiredMerchant hMerchant) {
        merchLock.writeLock().lock();

        try {
            merchants.remove(hMerchant.getStoreId());
        } finally {
            merchLock.writeLock().unlock();
        }
    }

    public final boolean containsMerchant(final int accid, int cid) {
        boolean contains = false;

        merchLock.readLock().lock();
        try {
            final Iterator itr = merchants.values().iterator();

            while (itr.hasNext()) {
                HiredMerchant hm = (HiredMerchant) itr.next();
                if (hm.getOwnerAccId() == accid || hm.getOwnerId() == cid) {
                    contains = true;
                    break;
                }
            }
        } finally {
            merchLock.readLock().unlock();
        }
        return contains;
    }

    public final List<HiredMerchant> searchMerchant(final int itemSearch) {
        final List<HiredMerchant> list = new LinkedList<>();
        merchLock.readLock().lock();
        try {
            final Iterator itr = merchants.values().iterator();

            while (itr.hasNext()) {
                HiredMerchant hm = (HiredMerchant) itr.next();
                if (hm.searchItem(itemSearch).size() > 0) {
                    list.add(hm);
                }
            }
        } finally {
            merchLock.readLock().unlock();
        }
        return list;
    }

    public final void toggleMegaphoneMuteState() {
        this.MegaphoneMuteState = !this.MegaphoneMuteState;
    }

    public final boolean getMegaphoneMuteState() {
        return MegaphoneMuteState;
    }
    
   public boolean getMarketUseBoard() {
        return this.marketUseBoard;
    }
    
    public void setMarketUseBoard(boolean t) {
        this.marketUseBoard = t;
    }

    public int getEvent() {
        return eventmap;
    }

    public final void setEvent(final int ze) {
        this.eventmap = ze;
    }

    public MapleEvent getEvent(final MapleEventType t) {
        return events.get(t);
    }

    public final Collection<PlayerNPC> getAllPlayerNPC() {
        return playerNPCs;
    }

    public final void addPlayerNPC(final PlayerNPC npc) {
        if (playerNPCs.contains(npc)) {
            return;
        }
        playerNPCs.add(npc);
        getMapFactory().getMap(npc.getMapId()).addMapObject(npc);
    }

    public final void removePlayerNPC(final PlayerNPC npc) {
        if (playerNPCs.contains(npc)) {
            playerNPCs.remove(npc);
            getMapFactory().getMap(npc.getMapId()).removeMapObject(npc);
        }
    }

    public final String getServerName() {
        return serverName;
    }

    public final void setServerName(final String sn) {
        this.serverName = sn;
    }

    public final String getTrueServerName() {
        return serverName.substring(0, serverName.length() - 2);
    }

    public final int getPort() {
        return port;
    }

    public static final Set<Integer> getChannelServer() {
        return new HashSet<>(instances.keySet());
    }

    public final void setShutdown() {
        this.shutdown = true;
        System.out.println("Channel " + channel + " has set to shutdown and is closing Hired Merchants...");
    }

    public final void setFinishShutdown() {
        this.finishedShutdown = true;
        System.out.println("Channel " + channel + " has finished shutdown.");
    }

    public final boolean isAdminOnly() {
        return adminOnly;
    }

    public final static int getChannelCount() {
        return instances.size();
    }

    public final int getTempFlag() {
        return flags;
    }

    public static Map<Integer, Integer> getChannelLoad() {
        Map<Integer, Integer> ret = new HashMap<>();
        for (ChannelServer cs : instances.values()) {
            ret.put(cs.getChannel(), cs.getConnectedClients());
        }
        return ret;
    }

    public int getConnectedClients() {
      //  return getPlayerStorage().getConnectedClients();
        
        double bfb = LoginServer.getRSGS() / 100.0d * getPlayerStorage().getConnectedClients();
        return getPlayerStorage().getConnectedClients() + ((int) Math.ceil(bfb));
        
    }

    public List<CheaterData> getCheaters() {
        List<CheaterData> cheaters = getPlayerStorage().getCheaters();

        Collections.sort(cheaters);
        return cheaters;
    }

    public List<CheaterData> getReports() {
        List<CheaterData> cheaters = getPlayerStorage().getReports();

        Collections.sort(cheaters);
        return cheaters;
    }

    public void broadcastMessage(byte[] message) {
        broadcastPacket(message);
    }

    public void broadcastSmega(byte[] message) {
        broadcastSmegaPacket(message);
    }

    public void broadcastGMMessage(byte[] message) {
        broadcastGMPacket(message);
    }

    public AramiaFireWorks getFireWorks() {
        return works;
    }

    public int getTraitRate() {
        return traitRate;
    }

    public boolean manualEvent(MapleCharacter chr) {
        if (manualEvent) {
            manualEvent = false;
            manualEventMap = 0;
        } else {
            manualEvent = true;
            manualEventMap = chr.getMapId();
        }
        if (manualEvent) {
            chr.dropMessage(5, "Manual event has " + (manualEvent ? "began" : "begone") + ".");
        }
        return manualEvent;
    }

    public void warpToEvent(MapleCharacter chr) {
        if (!manualEvent || manualEventMap <= 0) {
            chr.dropMessage(5, "There is no event being hosted.");
            return;
        }
        chr.dropMessage(5, "You are being warped into the event map.");
        chr.changeMap(manualEventMap, 0);
    }

    public boolean bombermanActive() {
        return bomberman;
    }

    public void toggleBomberman(MapleCharacter chr) {
        bomberman = !bomberman;
        if (bomberman) {
            chr.dropMessage(5, "Bomberman Event is active.");
        } else {
            chr.dropMessage(5, "Bomberman Event is not active.");
        }
    }

    public final int getMerchantMap(MapleCharacter chr) {
        int ret = -1;
        for (int i = 910000001; i <= 910000022; i++) {
            for (MapleMapObject mmo : mapFactory.getMap(i).getAllHiredMerchantsThreadsafe()) {
                if (((HiredMerchant) mmo).getOwnerId() == chr.getId()) {
                    return mapFactory.getMap(i).getId();
                }
            }
        }
        return ret;
    }
}
