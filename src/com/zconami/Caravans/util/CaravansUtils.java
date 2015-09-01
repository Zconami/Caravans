package com.zconami.Caravans.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;

import com.zconami.Caravans.repository.CaravanRepository;

public class CaravansUtils {

	// ===================================
	// CONSTRUCTORS
	// ===================================

	private CaravansUtils() {
	}

	// ===================================
	// PUBLIC METHODS
	// ===================================

	public static boolean isCaravan(Entity entity) {
		if (entity instanceof Horse) {
			return CaravanRepository.getInstance().find((Horse) entity) != null;
		}
		return false;
	}

}
