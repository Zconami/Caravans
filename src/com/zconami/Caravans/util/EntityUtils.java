package com.zconami.Caravans.util;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.google.common.collect.Maps;

public class EntityUtils {

	// ===================================
	// ATTRIBUTES
	// ===================================

	private static final Map<String, Entity> entityCache = Maps.newHashMap();

	// ===================================
	// CONSTRUCTORS
	// ===================================

	private EntityUtils() {
	}

	// ===================================
	// PUBLIC METHODS
	// ===================================

	public static Entity findBy(String key) {
		return findBy(key, null);
	}

	public static void removeFromCache(String key) {
		entityCache.remove(key);
	}

	public static <T extends Entity> T findBy(String key, Class<T> entityClass) {
		final Entity cached = entityCache.get(key);
		if (cached != null) {
			return (T) cached;
		}

		Entity searched = null;
		for (World world : Bukkit.getServer().getWorlds()) {
			if (entityClass != null) {
				searched = iterateForUUID((Iterator<Entity>) world.getEntitiesByClass(entityClass).iterator(), key);
				break;
			} else {
				searched = iterateForUUID(world.getEntities().iterator(), key);
				break;
			}
		}
		if (searched != null) {
			entityCache.put(key, searched);
		}
		return (T) searched;
	}

	// ===================================
	// PRIVATE METHODS
	// ===================================

	private static Entity iterateForUUID(Iterator<Entity> iterator, String key) {
		while (iterator.hasNext()) {
			final Entity next = iterator.next();
			if (next.getUniqueId().toString().equals(key)) {
				return next;
			}
		}
		return null;
	}

}
