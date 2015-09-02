package com.zconami.Caravans.repository;

import org.bukkit.entity.Player;

import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.storage.DataKey;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public class BeneficiaryRepository extends LinkedRepository<Player, EntityPlayer, Beneficiary> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static BeneficiaryRepository instance;

    public static final String NAME = "beneficiary";

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private BeneficiaryRepository() {
        super();
        BeneficiaryRepository.instance = this;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public Beneficiary save(Beneficiary beneficiary) {
        return super.save(beneficiary);
    }

    public static BeneficiaryRepository getInstance() {
        if (instance == null) {
            return new BeneficiaryRepository();
        }
        return instance;
    }

    // ===================================
    // IMPLEMENTATION OF Repository
    // ===================================

    @Override
    protected void createLookups(Beneficiary entity) {
    }

    @Override
    protected String getEntityName() {
        return NAME;
    }

    @Override
    protected void removeLookups(Beneficiary entity) {
    }

    @Override
    public void saveChanges(Beneficiary beneficiary) {
        super.save(beneficiary);
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
