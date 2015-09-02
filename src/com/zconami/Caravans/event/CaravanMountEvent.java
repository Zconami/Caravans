package com.zconami.Caravans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.zconami.Caravans.domain.Caravan;

public class CaravanMountEvent extends Event {

    // ===================================
    // CONSTANTS
    // ===================================

    private static final HandlerList handlers = new HandlerList();

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final Caravan caravan;

    private final Player player;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravanMountEvent(Caravan caravan, Player player) {
        this.caravan = caravan;
        this.player = player;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public Caravan getCaravan() {
        return caravan;
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
