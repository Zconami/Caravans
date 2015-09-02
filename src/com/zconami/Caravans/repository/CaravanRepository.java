package com.zconami.Caravans.repository;

import org.bukkit.entity.Horse;

import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.storage.DataKey;

import net.minecraft.server.v1_8_R3.EntityHorse;

public class CaravanRepository extends LinkedRepository<Horse, EntityHorse, Caravan> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static CaravanRepository instance;

    public static final String NAME = "caravan";

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private CaravanRepository() {
        super();
        CaravanRepository.instance = this;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public Caravan save(Caravan caravan) {
        return super.save(caravan);
    }

    public static CaravanRepository getInstance() {
        if (instance == null) {
            return new CaravanRepository();
        }
        return instance;
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

    @Override
    public void saveChanges(Caravan caravan) {
        super.save(caravan);
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
