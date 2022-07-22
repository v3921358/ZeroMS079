/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.inventory;

/**
 *
 * @author alienware
 */
public enum MapleWeaponType2 {
    NOT_A_WEAPON(0f),
    GENERAL1H_SWING(4.4f),
    GENERAL1H_STAB(3.2f),
    GENERAL2H_SWING(4.8f),
    GENERAL2H_STAB(3.4f),
    BOW(3.4f),
    CLAW(3.6f),
    CROSSBOW(3.6f),
    DAGGER_THIEVES(3.6f),
    DAGGER_OTHER(4f),
    GUN(3.6f),
    KNUCKLE(4.8f),
    POLE_ARM_SWING(5.0f),
    POLE_ARM_STAB(3.0f),
    SPEAR_STAB(5.0f),
    SPEAR_SWING(3.0f),
    STAFF(4.4f),//长杖
    SWORD1H(4.0f),
    SWORD2H(4.6f),
    WAND(4.4f);//短杖
    private final float damageMultiplier;

    private MapleWeaponType2(float maxDamageMultiplier) {
        this.damageMultiplier = maxDamageMultiplier;
    }

    public double getMaxDamageMultiplier() {
        return damageMultiplier;
    }
}
