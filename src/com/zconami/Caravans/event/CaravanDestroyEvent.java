package com.zconami.Caravans.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.zconami.Caravans.domain.Caravan;

public class CaravanDestroyEvent extends Event {

	// ===================================
	// CONSTANTS
	// ===================================

	private static final HandlerList handlers = new HandlerList();

	// ===================================
	// ATTRIBUTES
	// ===================================

	private final Caravan caravan;

	private final Player destroyer;

	// ===================================
	// CONSTRUCTORS
	// ===================================

	public CaravanDestroyEvent(Caravan caravan, Player destroyer) {
		this.caravan = caravan;
		this.destroyer = destroyer;
	}

	// ===================================
	// PUBLIC METHODS
	// ===================================

	public Caravan getCaravan() {
		return caravan;
	}

	public Player getDestroyer() {
		return destroyer;
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
