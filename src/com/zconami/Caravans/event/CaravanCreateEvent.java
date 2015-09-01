package com.zconami.Caravans.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.zconami.Caravans.domain.Caravan;

public class CaravanCreateEvent extends Event {

	// ===================================
	// CONSTANTS
	// ===================================

	private static final HandlerList handlers = new HandlerList();

	// ===================================
	// ATTRIBUTES
	// ===================================

	private final Caravan caravan;

	// ===================================
	// CONSTRUCTORS
	// ===================================

	public CaravanCreateEvent(Caravan caravan) {
		this.caravan = caravan;
	}

	// ===================================
	// PUBLIC METHODS
	// ===================================

	public Caravan getCaravan() {
		return caravan;
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
