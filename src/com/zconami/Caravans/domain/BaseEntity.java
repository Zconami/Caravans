package com.zconami.Caravans.domain;

import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final String TABLE_PREFIX = "zcaravans_";

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final String ID = "id";
    @Id
    private UUID id;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public BaseEntity() {
    }

    public BaseEntity(BaseEntityCreateParameters params) {
        apply(params);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdAsString() {
        return id.toString();
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private void apply(BaseEntityCreateParameters params) {
    }

}
