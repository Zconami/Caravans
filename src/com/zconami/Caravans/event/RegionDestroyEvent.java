package com.zconami.Caravans.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegionDestroyEvent extends Event {

    // ===================================
    // CONSTANTS
    // ===================================

    private static final HandlerList handlers = new HandlerList();

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final String name;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionDestroyEvent(String name) {
        this.name = name;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static HandlerList getHandlerList() {
        return handlers;
    }

    // ===================================
    // IMPLEMENTATION OF Event
    // ===================================

    public String getName() {
        return name;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
