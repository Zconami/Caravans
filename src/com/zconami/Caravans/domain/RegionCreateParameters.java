package com.zconami.Caravans.domain;

import org.bukkit.Location;

import com.zconami.Core.domain.EntityCreateParameters;

public class RegionCreateParameters extends EntityCreateParameters {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final String name;
    private final Location center;
    private final int radius;

    private String typeOfGood;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionCreateParameters(String key, String name, Location center, int radius) {
        super(key);
        this.name = name;
        this.center = center;
        this.radius = radius;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public String getName() {
        return name;
    }

    public Location getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    public String getTypeOfGood() {
        return typeOfGood;
    }

    public void setTypeOfGood(String typeOfGood) {
        this.typeOfGood = typeOfGood;
    }

}
