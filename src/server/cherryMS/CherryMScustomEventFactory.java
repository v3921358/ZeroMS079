/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.cherryMS;

import handling.channel.ChannelServer;
import server.maps.MapleMapFactory;

/**
 *
 * @author alienware
 */
public class CherryMScustomEventFactory {

    private static CherryMScustomEventFactory instance = null;
    private static boolean CANLOG;

    public boolean isCANLOG() {
        return CANLOG;
    }

    public void setCANLOG(boolean CANLOG) {
        CANLOG = CANLOG;
    }

    public static CherryMScustomEventFactory getInstance() {
        if (instance == null) {
            instance = new CherryMScustomEventFactory();
        }
        return instance;
    }

    public CherryMSLottery getCherryMSLottery() {
        return CherryMSLotteryImpl.getInstance();
    }

    public CherryMSLottery getCherryMSLottery(ChannelServer cserv, MapleMapFactory mapFactory) {
        return CherryMSLotteryImpl.getInstance(cserv, mapFactory);
    }
}
