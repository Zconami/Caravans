package com.zconami.Caravans.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityPlayer;

@Entity
@Table(name = Beneficiary.TABLE)
public class Beneficiary extends LinkedEntity<Player, EntityPlayer> {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final String TABLE = TABLE_PREFIX + "beneficiary";

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final String LAST_SUCCESSFUL_CARAVAN = "lastSuccessfulCaravan";
    @Column(nullable = false)
    private long lastSuccessfulCaravan = 0l;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Beneficiary() {
    }

    private Beneficiary(BeneficiaryCreateParameters params) {
        super(params);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public void hadSuccessfulCaravan() {
        this.lastSuccessfulCaravan = System.currentTimeMillis();
    }

    public void setLastSuccessfulCaravan(long lastSuccessfulCaravan) {
        this.lastSuccessfulCaravan = lastSuccessfulCaravan;
    }

    public long getLastSuccessfulCaravan() {
        return lastSuccessfulCaravan;
    }

    public static Beneficiary create(BeneficiaryCreateParameters params) {
        return new Beneficiary(params);
    }

    // ===================================
    // IMPLEMENTATION OF LinkedEntity
    // ===================================

    @Override
    public Class<Player> getBukkitEntityType() {
        return Player.class;
    }

}
