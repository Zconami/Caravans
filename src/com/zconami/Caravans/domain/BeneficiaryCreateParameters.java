package com.zconami.Caravans.domain;

import org.bukkit.entity.Player;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.zconami.Core.domain.EntityCreateParameters;

public class BeneficiaryCreateParameters extends EntityCreateParameters {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final String name;
    private final Faction faction;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public BeneficiaryCreateParameters(Player player) {
        super(player.getUniqueId().toString());
        this.name = player.getName();
        this.faction = MPlayer.get(player).getFaction();
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public String getName() {
        return name;
    }

    public Faction getFaction() {
        return faction;
    }

}
