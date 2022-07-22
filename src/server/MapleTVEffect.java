/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.MapleCharacter;
import handling.world.World;
import java.util.ArrayList;
import java.util.List;
import tools.packet.CSPacket;

/**
 *
 * @author alienware
 */
public class MapleTVEffect {

    private List<String> message = new ArrayList<>(5);
    private MapleCharacter user;
    private static boolean active;
    private int type;
    private MapleCharacter partner;

    public MapleTVEffect(MapleCharacter user_, MapleCharacter partner_, List<String> msg, int type_) {
        this.message = msg;
        this.user = user_;
        this.type = type_;
        this.partner = partner_;
        broadcastTV(true);
    }

    public void sendTV() {
        World.Broadcast.broadcastMessage(CSPacket.enableTV());
        World.Broadcast.broadcastMessage(CSPacket.sendTV(user, message, type <= 2 ? type : type - 3, partner, 60000));
    }

    public void removeTV() {
        World.Broadcast.broadcastMessage(CSPacket.removeTV());
    }

    public static boolean isActive() {
        return active;
    }

    private void setActive(boolean set) {
        active = set;
    }

    private void broadcastTV(boolean active_) {
        setActive(active_);
        if (active_) {
            int delay = 15000;
            if (type == 4) {
                delay = 30000;
            } else if (type == 5) {
                delay = 60000;
            }
            World.Broadcast.broadcastMessage(CSPacket.enableTV());
            World.Broadcast.broadcastMessage(CSPacket.sendTV(user, message, type <= 2 ? type : type - 3, partner, delay));

            Timer.EventTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    broadcastTV(false);
                }
            }, delay);
        } else {
            World.Broadcast.broadcastMessage(CSPacket.removeTV());
        }
    }
}
