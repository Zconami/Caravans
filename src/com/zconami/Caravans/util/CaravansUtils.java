package com.zconami.Caravans.util;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;

import com.zconami.Caravans.domain.Caravan;

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
            return getCaravansPlugin().DB.find(Caravan.class).where().eq(Caravan.BUKKIT_ENTITY_ID, entity.getUniqueId())
                    .findUnique() != null;
        }
        return false;
    }

}
