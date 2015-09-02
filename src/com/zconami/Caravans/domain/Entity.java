package com.zconami.Caravans.domain;

import com.zconami.Caravans.storage.DataKey;

public abstract class Entity {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private String key;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Entity(String key, DataKey entityData) {
        this.key = key;
        readData(entityData);
    }

    public Entity(EntityCreateParameters params) {
        apply(params);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public String getKey() {
        return key;
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private void apply(EntityCreateParameters params) {
        this.key = params.getKey();
    }

    // ===================================
    // ABSTRACT METHODS
    // ===================================

    public abstract void readData(DataKey dataKey);

    public abstract void writeData(DataKey dataKey);

    protected abstract void saveChanges();

}
