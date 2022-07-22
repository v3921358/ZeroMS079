/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import java.awt.Point;
import tools.packet.CField;

/**
 *
 * @author alienware
 */
public class MapleKite extends MapleMapObject {

    private Point pos;
    private final MapleCharacter owner;
    private final String text;
    private final int ft;
    private final int itemid;

    public MapleKite(MapleCharacter owner, Point pos, int ft, String text, int itemid) {
        this.owner = owner;
        this.pos = pos;
        this.text = text;
        this.ft = ft;
        this.itemid = itemid;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.KITE;
    }

    @Override
    public Point getPosition() {
        return this.pos.getLocation();
    }

    public MapleCharacter getOwner() {
        return this.owner;
    }

    public int getItemId() {
        return this.itemid;
    }

    @Override
    public void setPosition(Point position) {
        pos = position;
    }

    @Override
    public void sendSpawnData(MapleClient c) {
        c.getSession().write(CField.spawnKite(getObjectId(), this.itemid, this.owner.getName(), this.text, this.pos, this.ft));
    }

    @Override
    public void sendDestroyData(MapleClient c) {
        c.getSession().write(CField.destroyKite(getObjectId(), true));
    }
}
