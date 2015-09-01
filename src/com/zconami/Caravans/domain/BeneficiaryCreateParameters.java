package com.zconami.Caravans.domain;

import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public class BeneficiaryCreateParameters extends LinkedEntityCreateParameters<Player, EntityPlayer> {

	// ===================================
	// CONSTRUCTORS
	// ===================================

	public BeneficiaryCreateParameters(Player player) {
		super(player);
	}

}
