package com.zconami.Caravans.repository;

import java.util.Map;

import com.google.common.collect.Maps;
import com.zconami.Caravans.CaravansPlugin;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.storage.DataKey;

public class BeneficiaryRepository extends Repository<Beneficiary> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final String NAME = "beneficiary";

    private static final Map<String, Beneficiary> nameLookup = Maps.newHashMap();

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

    public Beneficiary findByName(String name) {
        final Beneficiary cached = nameLookup.get(name.toLowerCase());
        if (cached != null) {
            return cached;
        }
        // FIXME This is so shit
        all();
        return nameLookup.get(name.toLowerCase());
    }

    // ===================================
    // IMPLEMENTATION OF Repository
    // ===================================

    @Override
    protected Beneficiary recreate(DataKey entityData) {
        return new Beneficiary(entityData);
    }

    @Override
    protected String getEntityName() {
        return NAME;
    }

    @Override
    protected void createLookups(Beneficiary beneficiary) {
        nameLookup.put(beneficiary.getName().toLowerCase(), beneficiary);
    }

    @Override
    protected void removeLookups(Beneficiary beneficiary) {
        nameLookup.remove(beneficiary.getName().toLowerCase());
    }

}
