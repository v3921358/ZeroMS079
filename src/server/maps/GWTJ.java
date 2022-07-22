/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.maps;

/**
 *
 * @author XM
 */
public class GWTJ {
    public String 通缉时间,击杀玩家名称,怪物所在地图;
    public int 怪物ID;
    public boolean 怪物击杀状态 = false;
    
    public GWTJ(String 參_通缉时间, int 參_ID ,String 參_怪物所在地图,String 參_击杀玩家名称 ,boolean 參_怪物击杀状态) {
        this.通缉时间 = 參_通缉时间;
        this.怪物ID = 參_ID;
        this.怪物所在地图 = 參_怪物所在地图;
        this.击杀玩家名称 = 參_击杀玩家名称;
        this.怪物击杀状态 = 參_怪物击杀状态;
    }
}
