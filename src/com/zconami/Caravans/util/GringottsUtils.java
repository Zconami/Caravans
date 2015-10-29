package com.zconami.Caravans.util;

import org.bukkit.inventory.ItemStack;
import org.gestern.gringotts.Configuration;

public class GringottsUtils {

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private GringottsUtils() {
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static boolean isCurrency(ItemStack itemStack) {
        return Configuration.CONF.currency.value(itemStack) > 0;
    }

    public static String getGringottsName() {
        return Configuration.CONF.currency.name;
    }

    public static String getGringottsNamePlural() {
        return Configuration.CONF.currency.namePlural;
    }

    public static boolean isNotCurrency(ItemStack itemStack) {
        return !isCurrency(itemStack);
    }

}
