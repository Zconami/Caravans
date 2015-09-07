package com.zconami.Caravans.util;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;

import com.google.common.collect.Lists;
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

    public static void removeFromCache(String key) {
        entityCache.remove(key);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T findBy(String key, Chunk chunk) {
        if (chunk == null) {
            return null;
        }

        chunk.load(true);
        return (T) iterateForUUID(Lists.newArrayList(chunk.getEntities()).iterator(), key);
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
