package com.zconami.Caravans;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.zconami.Caravans.listener.CaravanEventListener;
import com.zconami.Caravans.listener.EventTranslator;

public class CaravansPlugin extends JavaPlugin {

    public static final String PLUGIN_NAME = "Caravans";

    private final CaravansCommandExecutor commandExecutor;
    private final EventTranslator eventTranslator;
    private final CaravanEventListener caravanEventListener;

    public CaravansPlugin() {
        super();
        this.commandExecutor = new CaravansCommandExecutor();
        this.eventTranslator = new EventTranslator();
        this.caravanEventListener = new CaravanEventListener();
    }

    @Override
    public void onEnable() {
        getLogger().info("=== ENABLE START ===");
        this.saveDefaultConfig();
        getLogger().info("Registering command executors...");
        this.getCommand("caravan").setExecutor(commandExecutor);
        getLogger().info("Registering listeners...");
        getServer().getPluginManager().registerEvents(eventTranslator, this);
        getServer().getPluginManager().registerEvents(caravanEventListener, this);
        getLogger().info("=== ENABLE COMPLETE ===");
    }

    @Override
    public void onDisable() {
        getLogger().info("=== DISABLE START ===");
        getLogger().info("Unregistering listeners...");
        HandlerList.unregisterAll(eventTranslator);
        HandlerList.unregisterAll(caravanEventListener);
        getLogger().info("=== DISABLE COMPLETE ===");
    }

}
