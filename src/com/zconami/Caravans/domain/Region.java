package com.zconami.Caravans.domain;

import java.util.UUID;

import org.apache.logging.log4j.core.helpers.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.zconami.Caravans.storage.DataKey;

public class Region extends Entity {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final int DEFAULT_RADIUS = 5;

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final String CENTER = "center";
    public static final String CENTER_WORLD = CENTER + ".world";
    public static final String CENTER_X = CENTER + ".x";
    public static final String CENTER_Y = CENTER + ".y";
    public static final String CENTER_Z = CENTER + ".z";
    private Location center;

    public static final String RADIUS = "radius";
    private int radius;

    public static final String NAME = "name";
    private String name;

    public static final String IS_ORIGIN = "isOrigin";
    private boolean isOrigin;

    public static final String IS_DESTINATION = "isDestination";
    private boolean isDestination;

    public static final String REMOVE_AFTER_LAST_CARAVAN = "removeAfterLastCaravan";
    private boolean removeAfterLastCaravan = false;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Region(DataKey entityData) {
        super(entityData.getPath(), entityData);
    }

    private Region(RegionCreateParameters params) {
        super(params);
        apply(params);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static Region create(RegionCreateParameters params) {
        if (Strings.isEmpty(params.getName())) {
            return null;
        }
        return new Region(params);
    }

    public Location getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    public String getName() {
        return name;
    }

    public boolean isOrigin() {
        return isOrigin;
    }

    public boolean isDestination() {
        return isDestination;
    }

    public boolean isRemoveAfterLastCaravan() {
        return removeAfterLastCaravan;
    }

    public void setRemoveAfterLastCaravan(boolean removeAfterLastCaravan) {
        this.removeAfterLastCaravan = removeAfterLastCaravan;
        this.setDirty(true);
    }

    public boolean contains(Location location) {
        final double x = location.getX();
        final double z = location.getZ();
        return x < (center.getBlockX() + radius) && x > (center.getBlockX() - radius)
                && z < (center.getBlockZ() + radius) && z > (center.getBlockZ() - radius);
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private void apply(RegionCreateParameters params) {
        this.center = params.getCenter();
        this.radius = params.getRadius();
        this.name = params.getName();
        this.isOrigin = params.isOrigin();
        this.isDestination = params.isDestination();
    }

    // ===================================
    // IMPLEMENTATION OF Entity
    // ===================================

    @Override
    public void readData(DataKey dataKey) {
        final UUID worldUUID = UUID.fromString(dataKey.getString(CENTER_WORLD));
        final double centerX = dataKey.getDouble(CENTER_X);
        final double centerY = dataKey.getDouble(CENTER_Y);
        final double centerZ = dataKey.getDouble(CENTER_Z);

        this.center = new Location(Bukkit.getWorld(worldUUID), centerX, centerY, centerZ);
        this.radius = dataKey.getInt(RADIUS);
        this.name = dataKey.getString(NAME);
        this.isOrigin = dataKey.getBoolean(IS_ORIGIN);
        this.isDestination = dataKey.getBoolean(IS_DESTINATION);
        this.removeAfterLastCaravan = dataKey.getBoolean(REMOVE_AFTER_LAST_CARAVAN);
    }

    @Override
    public void writeData(DataKey dataKey) {
        dataKey.setString(CENTER_WORLD, center.getWorld().getUID().toString());
        dataKey.setDouble(CENTER_X, center.getX());
        dataKey.setDouble(CENTER_Y, center.getY());
        dataKey.setDouble(CENTER_Z, center.getZ());

        dataKey.setInt(RADIUS, radius);

        dataKey.setString(NAME, name);

        dataKey.setBoolean(IS_ORIGIN, isOrigin);
        dataKey.setBoolean(IS_DESTINATION, isDestination);
        dataKey.setBoolean(REMOVE_AFTER_LAST_CARAVAN, removeAfterLastCaravan);
    }

}
