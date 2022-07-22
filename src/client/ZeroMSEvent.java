package client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import handling.world.World.Broadcast;
import tools.packet.CWvsContext;
import constants.ServerConfig;
import java.util.Iterator;
import handling.channel.ChannelServer;
import server.Timer.WorldTimer;

public class ZeroMSEvent
{
    public static void start() {
        if (ServerConfig.Danji_Guanggao) {
            WorldTimer.getInstance().register((Runnable)new Runnable() {
                @Override
                public void run() {
                    for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                        for (final MapleCharacter players : cserv.getPlayerStorage().getAllCharacters()) {
                            players.getMap().startMapEffect("[ZeroMS发布群]作者QQ:40074907 疯神技术交流群:6544394 怀旧单机群:542123915", 5120026);
                            Broadcast.broadcastMessage(CWvsContext.broadcastMsg(0, "广告时间到了:\r\n加入此广告是为了不让淘宝的人拿去贩卖\r\n请理解作者!\r\n\r\n如想商业开服\r\n请赞助[ZeroMS]\r\n可联系群主QQ:40074907!"));
                            Broadcast.broadcastMessage(CWvsContext.broadcastMsg(5, "广告时间到了:\r\n加入此广告是为了不让淘宝的人拿去贩卖\r\n请理解作者!\r\n\r\n如想商业开服\r\n请赞助[ZeroMS]\r\n可联系群主QQ:40074907!"));
                            Broadcast.broadcastMessage(CWvsContext.broadcastMsg(0, "广告时间到了:\r\n加入此广告是为了不让淘宝的人拿去贩卖\r\n请理解作者!\r\n\r\n如想商业开服\r\n请赞助[ZeroMS]\r\n可联系群主QQ:40074907!"));
                            Broadcast.broadcastMessage(CWvsContext.broadcastMsg(5, "广告时间到了:\r\n加入此广告是为了不让淘宝的人拿去贩卖\r\n请理解作者!\r\n\r\n如想商业开服\r\n请赞助[ZeroMS]\r\n可联系群主QQ:40074907!"));
                            Broadcast.broadcastMessage(CWvsContext.broadcastMsg(0, "广告时间到了:\r\n加入此广告是为了不让淘宝的人拿去贩卖\r\n请理解作者!\r\n\r\n如想商业开服\r\n请赞助[ZeroMS]\r\n可联系群主QQ:40074907!"));
                            Broadcast.broadcastMessage(CWvsContext.broadcastMsg(5, "广告时间到了:\r\n加入此广告是为了不让淘宝的人拿去贩卖\r\n请理解作者!\r\n\r\n如想商业开服\r\n请赞助[ZeroMS]\r\n可联系群主QQ:40074907!"));                            
                        }
                    }
                }
            }, 6000000L);
        }
    }

    private ZeroMSEvent() {
    }
}
