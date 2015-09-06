package com.zconami.Caravans.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Horse;

import com.google.common.collect.Maps;
import com.massivecraft.factions.entity.Faction;
import com.zconami.Caravans.CaravansPlugin;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.exception.CaravanCreateBeneficiaryPlayerOfflineException;
import com.zconami.Caravans.storage.DataKey;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class CaravanRepository extends LinkedRepository<Horse, EntityHorse, Caravan> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final String NAME = "caravan";

    private static final Map<Beneficiary, Caravan> beneficiaryLookup = Maps.newHashMap();

    private static final Map<Faction, Caravan> factionLookup = Maps.newHashMap();

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravanRepository(CaravansPlugin plugin) {
        super(plugin);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public Caravan save(Caravan caravan) {
        return super.save(caravan);
    }

    public List<Caravan> activeFrom(Region origin) {
        return all().stream().filter(caravan -> caravan.getOrigin().equals(origin)).collect(Collectors.toList());
    }

    public Caravan findByBeneficiary(Beneficiary beneficiary) {
        final Caravan cached = beneficiaryLookup.get(beneficiary);
        if (cached != null) {
            return cached;
        }
        // FIXME This is so shit
        all();
        return beneficiaryLookup.get(beneficiary);
    }

    public Caravan findByFaction(Faction faction) {
        final Caravan cached = factionLookup.get(faction);
        if (cached != null) {
            return cached;
        }
        // FIXME This is so shit
        all();
        return factionLookup.get(faction);
    }

    // ===================================
    // IMPLEMENTATION OF Repository
    // ===================================

    @Override
    protected void createLookups(Caravan entity) {
        beneficiaryLookup.put(entity.getBeneficiary(), entity);
        factionLookup.put(entity.getFaction(), entity);
    }

    @Override
    protected void removeLookups(Caravan entity) {
        beneficiaryLookup.remove(entity.getBeneficiary());
        factionLookup.remove(entity.getFaction());
    }

    @Override
    protected String getEntityName() {
        return NAME;
    }

    // ===================================
    // IMPLEMENTATION OF LinkedRepository
    // ===================================

    @Override
    protected Class<Horse> getBukkitEntityType() {
        return Horse.class;
    }

    @Override
    protected Caravan recreate(Horse horse, DataKey entityData) {
        try {
            return new Caravan(horse, entityData);
        } catch (CaravanCreateBeneficiaryPlayerOfflineException ex) {
            return null;
        }
    }

}
