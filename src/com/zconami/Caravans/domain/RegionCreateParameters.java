package com.zconami.Caravans.domain;

import org.bukkit.Location;

public class RegionCreateParameters extends BaseEntityCreateParameters {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final String name;
    private final Location center;
    private final int radius;

    private boolean isOrigin = true;
    private boolean isDestination = true;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionCreateParameters(String key, String name, Location center, int radius) {
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

    public boolean isOrigin() {
        return isOrigin;
    }

    public void setOrigin(boolean isOrigin) {
        this.isOrigin = isOrigin;
    }

    public boolean isDestination() {
        return isDestination;
    }

    public void setDestination(boolean isDestination) {
        this.isDestination = isDestination;
    }

}
