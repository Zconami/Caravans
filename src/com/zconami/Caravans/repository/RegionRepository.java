package com.zconami.Caravans.repository;

import java.util.Map;

import com.google.common.collect.Maps;
import com.zconami.Caravans.CaravansPlugin;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.storage.DataKey;

public class RegionRepository extends Repository<Region> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final String NAME = "region";

    private static final Map<String, Region> nameLookup = Maps.newHashMap();

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionRepository(CaravansPlugin plugin) {
        super(plugin);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public Region save(Region region) {
        return super.save(region);
    }

    public Region findByName(String name) {
        final Region cached = nameLookup.get(name);
        if (cached != null) {
            return cached;
        }
        // FIXME This is so shit
        all();
        return nameLookup.get(name);
    }

    // ===================================
    // IMPLEMENTATION OF Repository
    // ===================================

    @Override
    protected Region recreate(DataKey entityData) {
        return new Region(entityData);
    }

    @Override
    protected String getEntityName() {
        return NAME;
    }

    @Override
    protected void createLookups(Region region) {
        nameLookup.put(region.getName(), region);
    }

    @Override
    protected void removeLookups(Region region) {
        nameLookup.remove(region.getName());
    }

    @Override
    protected boolean shouldRecreate(DataKey entityData) {
        return true;
    }

}
