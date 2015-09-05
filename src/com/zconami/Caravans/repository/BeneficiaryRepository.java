package com.zconami.Caravans.repository;

import org.bukkit.entity.Player;

import com.zconami.Caravans.CaravansPlugin;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.storage.DataKey;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public class BeneficiaryRepository extends LinkedRepository<Player, EntityPlayer, Beneficiary> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final String NAME = "beneficiary";

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public BeneficiaryRepository(CaravansPlugin plugin) {
        super(plugin);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public Beneficiary save(Beneficiary beneficiary) {
        return super.save(beneficiary);
    }

    // ===================================
    // IMPLEMENTATION OF Repository
    // ===================================

    @Override
    protected void createLookups(Beneficiary entity) {
    }

    @Override
    protected void removeLookups(Beneficiary entity) {
    }

    @Override
    protected String getEntityName() {
        return NAME;
    }

    // ===================================
    // IMPLEMENTATION OF LinkedRepository
    // ===================================

    @Override
    protected Class<Player> getBukkitEntityType() {
        return Player.class;
    }

    @Override
    protected Beneficiary recreate(Player player, DataKey extraData) {
        return new Beneficiary(player, extraData);
    }

}
