package com.zconami.Caravans.domain;

public abstract class EntityCreateParameters {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final String key;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public EntityCreateParameters(String key) {
        this.key = key;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public String getKey() {
        return key;
    }

}
