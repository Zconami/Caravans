package com.zconami.Caravans.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegionInteractEvent extends Event {

    // ===================================
    // CONSTANTS
    // ===================================

    private static final HandlerList handlers = new HandlerList();

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final String name;
    private final Location location;
    private final Player player;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionInteractEvent(Block signBlock, String name, Player player) {
        this.name = name;
        this.location = signBlock.getLocation();
        this.player = player;
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

    public Player getPlayer() {
        return player;
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
