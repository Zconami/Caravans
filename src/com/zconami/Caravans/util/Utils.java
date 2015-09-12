package com.zconami.Caravans.util;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

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

    public static int ticksFromSeconds(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    public static Logger getLogger() {
        return getCaravansPlugin().getLogger();
    }

    public static boolean isSignBlock(Block block) {
        return block.getState() instanceof Sign;
    }

    public static CaravansPlugin getCaravansPlugin() {
        return (CaravansPlugin) Bukkit.getServer().getPluginManager().getPlugin(CaravansPlugin.PLUGIN_NAME);
    }

}
