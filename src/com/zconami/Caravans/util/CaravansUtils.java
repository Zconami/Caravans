package com.zconami.Caravans.util;

import static com.zconami.Caravans.CaravansPlugin.getCaravansPlugin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;

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
            return getCaravansPlugin().getCaravanRepository().find((Horse) entity) != null;
        }
        return false;
    }

}
