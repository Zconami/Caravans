package com.zconami.Caravans.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Horse;

import com.zconami.Caravans.CaravansPlugin;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.storage.DataKey;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class CaravanRepository extends LinkedRepository<Horse, EntityHorse, Caravan> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final String NAME = "caravan";

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

    // ===================================
    // IMPLEMENTATION OF Repository
    // ===================================

    @Override
    protected void createLookups(Caravan entity) {
    }

    @Override
    protected String getEntityName() {
        return NAME;
    }

    @Override
    protected void removeLookups(Caravan entity) {
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
        return new Caravan(horse, entityData);
    }

}
