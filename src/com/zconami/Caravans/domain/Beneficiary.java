package com.zconami.Caravans.domain;

import org.bukkit.entity.Player;

import com.zconami.Caravans.storage.DataKey;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public class Beneficiary extends LinkedEntity<Player, EntityPlayer> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final String LAST_SUCCESSFUL_CARAVAN = "lastSuccessfulCaravan";
    private long lastSuccessfulCaravan = 0l;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Beneficiary(Player player, DataKey extraData) {
        super(player, extraData);
    }

    private Beneficiary(BeneficiaryCreateParameters params) {
        super(params);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public void successfulCaravan() {
        this.lastSuccessfulCaravan = System.currentTimeMillis();
        this.setDirty(true);
    }

    public long getLastSuccessfulCaravan() {
        return lastSuccessfulCaravan;
    }

    public static Beneficiary create(BeneficiaryCreateParameters params) {
        return new Beneficiary(params);
    }

    // ===================================
    // IMPLEMENTATION OF Entity
    // ===================================

    @Override
    public void readData(DataKey dataKey) {
        this.lastSuccessfulCaravan = dataKey.getLong(LAST_SUCCESSFUL_CARAVAN);
    }

    @Override
    public void writeData(DataKey dataKey) {
        dataKey.setLong(LAST_SUCCESSFUL_CARAVAN, lastSuccessfulCaravan);
    }

    // ===================================
    // IMPLEMENTATION OF LinkedEntity
    // ===================================

    @Override
    public Class<Player> getBukkitEntityType() {
        return Player.class;
    }

}
