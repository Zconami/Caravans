package com.zconami.Caravans.util;

import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

import com.google.common.collect.Maps;

public class EntityUtils {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final int EXPANDED_CHUNK_SEARCH_MAX_RADIUS = 5;

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

    public static void removeFromCache(String key) {
        entityCache.remove(key);
    }

    public static Entity findBy(String key, Chunk chunk) {
        return findBy(key, chunk, true);
    }

    public static Entity findBy(String key, Chunk chunk, boolean expandSearch) {
        if (expandSearch) {
            return searchChunk(key, chunk);
        } else {
            return checkChunk(key, chunk);
        }
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private static Entity searchChunk(String key, Chunk originChunk) {
        return searchChunk(key, originChunk, 0);
    }

    private static Entity searchChunk(String key, Chunk originChunk, int radius) {
        int currentX = originChunk.getX() - radius;
        int currentZ = originChunk.getZ() - radius;
        while (currentX <= originChunk.getX() + radius) {
            while (currentZ <= originChunk.getZ() + radius) {
                final Chunk current = originChunk.getWorld().getChunkAt(currentX, currentZ);
                final Entity searched = checkChunk(key, current);
                if (searched != null) {
                    return searched;
                }
                currentZ++;
            }
            currentZ = originChunk.getZ() - radius;
            currentX++;
        }
        if (radius < EXPANDED_CHUNK_SEARCH_MAX_RADIUS) {
            return searchChunk(key, originChunk, radius + 1);
        }
        return null;
    }

    private static Entity checkChunk(String key, Chunk chunk) {
        chunk.load(true);
        for (Entity entity : chunk.getEntities()) {
            if (entity.getUniqueId().toString().equals(key)) {
                return entity;
            }
        }
        return null;
    }

}
