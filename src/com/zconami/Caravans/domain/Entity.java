package com.zconami.Caravans.domain;

import java.util.Set;

import com.google.common.collect.Sets;
import com.zconami.Caravans.storage.DataKey;

public abstract class Entity {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private String key;

    private boolean dirty;

    private Set<EntityObserver> observers = Sets.newHashSet();

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

    protected Set<EntityObserver> getObservers() {
        return observers;
    }

    public void addObserver(EntityObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(EntityObserver observer) {
        observers.remove(observer);
    }

    public String getKey() {
        return key;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        if (dirty) {
            observers.forEach(observer -> observer.entityChanged(this));
        }
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

    public abstract void willRemove();

    public abstract void readData(DataKey dataKey);

    public abstract void writeData(DataKey dataKey);

}
