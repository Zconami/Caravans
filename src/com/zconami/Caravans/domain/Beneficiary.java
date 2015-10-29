package com.zconami.Caravans.domain;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.zconami.Core.domain.Entity;
import com.zconami.Core.storage.DataKey;

public class Beneficiary extends Entity {

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final String NAME = "name";
    private String name;

    public static final String LAST_SUCCESSFUL_CARAVAN = "lastSuccessfulCaravan";
    private long lastSuccessfulCaravan = 0l;

    public static final String FACTION = "faction";
    private Faction faction;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Beneficiary(DataKey entityData) {
        super(entityData.getPath(), entityData);
    }

    private Beneficiary(BeneficiaryCreateParameters params) {
        super(params);
        this.name = params.getName();
        this.faction = params.getFaction();
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

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
        this.setDirty(true);
    }

    public String getName() {
        return name;
    }

    // ===================================
    // IMPLEMENTATION OF Entity
    // ===================================

    @Override
    public void readData(DataKey dataKey) {

        this.lastSuccessfulCaravan = dataKey.getLong(LAST_SUCCESSFUL_CARAVAN);
        this.name = dataKey.getString(NAME);

        this.faction = FactionColl.get().getFixed(dataKey.getString(FACTION));
        if (this.faction == null) {
            final UUID playerUUID = UUID.fromString(dataKey.getPath());
            final Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                this.faction = MPlayer.get(player).getFaction();
            } else {
                this.faction = FactionColl.get().getFixed(Factions.ID_NONE);
            }
            this.setDirty(true);
        }
    }

    @Override
    public void writeData(DataKey dataKey) {
        dataKey.setLong(LAST_SUCCESSFUL_CARAVAN, lastSuccessfulCaravan);
        dataKey.setString(NAME, name);
        dataKey.setString(FACTION, faction.getId());
    }

}
