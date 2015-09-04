package com.zconami.Caravans.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.logging.log4j.core.helpers.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.avaje.ebean.validation.NotEmpty;

@Entity
@Table(name = Region.TABLE)
@UniqueConstraint(columnNames = {
        Region.NAME
})
public class Region extends BaseEntity {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final String TABLE = TABLE_PREFIX + "region";

    // ===================================
    // CONSTANTS
    // ===================================

    public static final int DEFAULT_RADIUS = 5;

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final String CENTER = "center";
    public static final String CENTER_X = CENTER + "X";
    @Column(name = CENTER_X, nullable = false)
    private double centerX;

    public static final String CENTER_Y = CENTER + "Y";
    @Column(name = CENTER_Y, nullable = false)
    private double centerY;

    public static final String CENTER_Z = CENTER + "Z";
    @Column(name = CENTER_Z, nullable = false)
    private double centerZ;

    public static final String CENTER_WORLD = CENTER + "World";
    @Column(name = CENTER_WORLD, nullable = false)
    @NotEmpty
    private String centerWorld;

    public static final String RADIUS = "radius";
    private int radius;

    public static final String NAME = "name";
    @NotEmpty
    private String name;

    public static final String IS_ORIGIN = "origin";
    private boolean origin;

    public static final String IS_DESTINATION = "destination";
    private boolean destination;

    public static final String REMOVE_AFTER_LAST_CARAVAN = "removeAfterLastCaravan";
    private boolean removeAfterLastCaravan = false;

    @Transient
    private Location center;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public Region() {
        setTransientLocation();
    }

    private Region(RegionCreateParameters params) {
        super(params);
        apply(params);
        setTransientLocation();
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
        return origin;
    }

    public boolean isDestination() {
        return destination;
    }

    public boolean isRemoveAfterLastCaravan() {
        return removeAfterLastCaravan;
    }

    public void setRemoveAfterLastCaravan(boolean removeAfterLastCaravan) {
        this.removeAfterLastCaravan = removeAfterLastCaravan;
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public double getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(double centerZ) {
        this.centerZ = centerZ;
    }

    public String getCenterWorld() {
        return centerWorld;
    }

    public void setCenterWorld(String centerWorld) {
        this.centerWorld = centerWorld;
        setTransientLocation();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrigin(boolean isOrigin) {
        this.origin = isOrigin;
    }

    public void setDestination(boolean isDestination) {
        this.destination = isDestination;
    }

    public void setCenter(Location location) {
        this.center = location;
        this.centerWorld = location.getWorld().getName();
        this.centerX = location.getX();
        this.centerY = location.getY();
        this.centerZ = location.getZ();
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

    private void setTransientLocation() {
        if (centerWorld != null) {
            final World world = Bukkit.getServer().getWorld(centerWorld);
            this.center = new Location(world, centerX, centerY, centerZ);
        }
    }

    private void apply(RegionCreateParameters params) {
        setCenter(params.getCenter());
        this.radius = params.getRadius();
        this.name = params.getName();
        this.origin = params.isOrigin();
        this.destination = params.isDestination();
    }

}
