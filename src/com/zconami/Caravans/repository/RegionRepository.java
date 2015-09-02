package com.zconami.Caravans.repository;

import java.util.Map;

import com.google.common.collect.Maps;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.storage.DataKey;

public class RegionRepository extends Repository<Region> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static RegionRepository instance;

    public static final String NAME = "region";

    private Map<String, Region> nameLookup = Maps.newHashMap();

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private RegionRepository() {
        super();
        RegionRepository.instance = this;
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

    public static RegionRepository getInstance() {
        if (instance == null) {
            return new RegionRepository();
        }
        return instance;
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
    public void saveChanges(Region region) {
        super.save(region);
    }

    @Override
    protected void createLookups(Region region) {
        nameLookup.put(region.getName(), region);
    }

    @Override
    protected void removeLookups(Region region) {
        nameLookup.remove(region.getName());
    }

}
