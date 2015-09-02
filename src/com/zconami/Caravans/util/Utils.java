package com.zconami.Caravans.util;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.zconami.Caravans.CaravansPlugin;

public class Utils {

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static final int TICKS_PER_SECOND = 20;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private Utils() {
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static int ticks(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    public static Logger getLogger() {
        return getJavaPlugin().getLogger();
    }

    public static JavaPlugin getJavaPlugin() {
        return (JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin(CaravansPlugin.PLUGIN_NAME);
    }

}
