package com.zconami.Caravans.event;

import java.util.List;
import java.util.regex.Matcher;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.zconami.Caravans.domain.Region;

public class RegionPreCreateEvent extends Event {

    // ===================================
    // CONSTANTS
    // ===================================

    private static final HandlerList handlers = new HandlerList();

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final String name;
    private final Location location;
    private final int radius;
    private final String typeOfGood;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionPreCreateEvent(Block signBlock, List<Matcher> matchers) {
        this(signBlock, matchers, Region.DEFAULT_RADIUS);
    }

    public RegionPreCreateEvent(Block signBlock, List<Matcher> matchers, int radius) {
        this.name = matchers.get(1).group("regionName");
        this.location = signBlock.getLocation();
        this.typeOfGood = matchers.get(3).group("typeOfGood");
        this.radius = radius;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public String getTypeOfGood() {
        return typeOfGood;
    }

    public int getRadius() {
        return radius;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    // ===================================
    // IMPLEMENTATION OF Event
    // ===================================

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
