package com.zconami.Caravans.util;

import java.util.Collection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.gestern.gringotts.Configuration;

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

    public static String getGringottsNamePlural() {
        return Configuration.CONF.currency.namePlural;
    }

    public static BukkitScheduler getScheduler() {
        return Bukkit.getScheduler();
    }

    public static Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    public static ScoreboardManager getScoreboardManager() {
        return Bukkit.getScoreboardManager();
    }

    public static FileConfiguration getCaravansConfig() {
        return getCaravansPlugin().getConfig();
    }

    public static CaravansPlugin getCaravansPlugin() {
        return (CaravansPlugin) Bukkit.getServer().getPluginManager().getPlugin(CaravansPlugin.PLUGIN_NAME);
    }

}
