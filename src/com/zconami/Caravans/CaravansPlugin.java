package com.zconami.Caravans;

import java.util.List;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.EbeanServer;
import com.google.common.collect.Lists;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.listener.CaravanEventListener;
import com.zconami.Caravans.listener.EventTranslator;
import com.zconami.Caravans.listener.RegionEventListener;

public class CaravansPlugin extends JavaPlugin {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final String PLUGIN_NAME = "Caravans";

    // ===================================
    // ATTRIBUTES
    // ===================================

    public EbeanServer DB;

    private final CaravansCommandExecutor commandExecutor;
    private final EventTranslator eventTranslator;
    private final CaravanEventListener caravanEventListener;
    private final RegionEventListener regionEventListener;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravansPlugin() {
        super();

        this.commandExecutor = new CaravansCommandExecutor();
        this.eventTranslator = new EventTranslator();
        this.caravanEventListener = new CaravanEventListener();
        this.regionEventListener = new RegionEventListener();
    }

    // ===================================
    // IMPLEMENTATION OF JavaPlugin
    // ===================================

    @Override
    public void onEnable() {
        getLogger().info("=== ENABLE START ===");
        getLogger().info("Checking for config...");
        this.saveDefaultConfig();
        getLogger().info("Setting up database...");
        this.setupDatabase();
        getLogger().info("Registering command executors...");
        this.getCommand("caravan").setExecutor(commandExecutor);
        getLogger().info("Registering listeners...");
        getServer().getPluginManager().registerEvents(eventTranslator, this);
        getServer().getPluginManager().registerEvents(caravanEventListener, this);
        getServer().getPluginManager().registerEvents(regionEventListener, this);
        getLogger().info("=== ENABLE COMPLETE ===");
    }

    @Override
    public void onDisable() {
        getLogger().info("=== DISABLE START ===");
        getLogger().info("Unregistering listeners...");
        HandlerList.unregisterAll(eventTranslator);
        HandlerList.unregisterAll(caravanEventListener);
        HandlerList.unregisterAll(regionEventListener);
        getLogger().info("=== DISABLE COMPLETE ===");
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = Lists.newArrayList();
        list.add(Region.class);
        list.add(Caravan.class);
        list.add(Beneficiary.class);
        return list;
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private void setupDatabase() {
        try {
            DB = getDatabase();
            for (Class<?> c : getDatabaseClasses())
                DB.find(c).findRowCount();
        } catch (Exception ignored) {
            getLogger().info("Initializing database tables.");
            installDDL();
        }
    }

}
