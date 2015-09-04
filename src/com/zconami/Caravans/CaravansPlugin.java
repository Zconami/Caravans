package com.zconami.Caravans;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.zconami.Caravans.listener.CaravanEventListener;
import com.zconami.Caravans.listener.EventTranslator;
import com.zconami.Caravans.listener.RegionEventListener;
import com.zconami.Caravans.repository.BeneficiaryRepository;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.repository.RegionRepository;

public class CaravansPlugin extends JavaPlugin {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final String PLUGIN_NAME = "Caravans";

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final CaravansCommandExecutor commandExecutor;
    private final EventTranslator eventTranslator;
    private final CaravanEventListener caravanEventListener;
    private final RegionEventListener regionEventListener;

    private final RegionRepository regionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CaravanRepository caravanRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravansPlugin() {
        super();

        this.regionRepository = new RegionRepository(this);
        this.beneficiaryRepository = new BeneficiaryRepository(this);
        this.caravanRepository = new CaravanRepository(this);

        this.commandExecutor = new CaravansCommandExecutor(regionRepository, beneficiaryRepository, caravanRepository);
        this.eventTranslator = new EventTranslator(caravanRepository);
        this.caravanEventListener = new CaravanEventListener(caravanRepository, regionRepository);
        this.regionEventListener = new RegionEventListener(caravanRepository, beneficiaryRepository, regionRepository);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public RegionRepository getRegionRepository() {
        return regionRepository;
    }

    public BeneficiaryRepository getBeneficiaryRepository() {
        return beneficiaryRepository;
    }

    public CaravanRepository getCaravanRepository() {
        return caravanRepository;
    }

    // ===================================
    // IMPLEMENTATION OF JavaPlugin
    // ===================================

    @Override
    public void onEnable() {
        getLogger().info("=== ENABLE START ===");
        this.saveDefaultConfig();
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

}
