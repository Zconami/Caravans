package com.zconami.Caravans;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.listener.CaravanEventListener;
import com.zconami.Caravans.listener.ChunkEventListener;
import com.zconami.Caravans.listener.EventTranslator;
import com.zconami.Caravans.listener.PlayerEventListener;
import com.zconami.Caravans.listener.RegionEventListener;
import com.zconami.Caravans.repository.BeneficiaryRepository;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.repository.RegionRepository;
import com.zconami.Caravans.util.ScoreboardUtils;

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
    private final ChunkEventListener chunkEventListner;
    private final CaravanEventListener caravanEventListener;
    private final RegionEventListener regionEventListener;
    private final PlayerEventListener playerEventListener;

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
        this.eventTranslator = new EventTranslator(beneficiaryRepository, caravanRepository);
        this.chunkEventListner = new ChunkEventListener(caravanRepository);
        this.caravanEventListener = new CaravanEventListener(caravanRepository, regionRepository);
        this.regionEventListener = new RegionEventListener(caravanRepository, beneficiaryRepository, regionRepository);
        this.playerEventListener = new PlayerEventListener(caravanRepository);
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
        this.getCommand("c").setExecutor(commandExecutor);
        getLogger().info("Registering listeners...");
        getServer().getPluginManager().registerEvents(eventTranslator, this);
        getServer().getPluginManager().registerEvents(chunkEventListner, this);
        getServer().getPluginManager().registerEvents(caravanEventListener, this);
        getServer().getPluginManager().registerEvents(regionEventListener, this);
        getServer().getPluginManager().registerEvents(playerEventListener, this);
        getLogger().info("=== ENABLE COMPLETE ===");
    }

    @Override
    public void onDisable() {
        getLogger().info("=== DISABLE START ===");
        getLogger().info("Setting logged off passenger flag for caravans currently mounted...");
        caravanRepository.all().stream().filter(Caravan::hasPassenger).forEach(Caravan::passengerLoggedOut);
        getLogger().info("Setting location public for limbo caravans...");
        caravanRepository.all().stream().filter(Caravan::locationAwaitingBroadcast).forEach(Caravan::locationIsPublic);
        getLogger().info("Stopping all scoreboards...");
        ScoreboardUtils.stopAll();
        getLogger().info("Unloading repositories...");
        caravanRepository.unload();
        beneficiaryRepository.unload();
        regionRepository.unload();
        getLogger().info("Unregistering listeners...");
        HandlerList.unregisterAll(eventTranslator);
        HandlerList.unregisterAll(chunkEventListner);
        HandlerList.unregisterAll(caravanEventListener);
        HandlerList.unregisterAll(regionEventListener);
        HandlerList.unregisterAll(playerEventListener);
        getLogger().info("=== DISABLE COMPLETE ===");
    }

}
