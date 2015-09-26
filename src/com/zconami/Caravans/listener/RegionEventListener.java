package com.zconami.Caravans.listener;

import static com.zconami.Caravans.util.Utils.getCaravansConfig;
import static com.zconami.Caravans.util.Utils.getGringottsNamePlural;
import static com.zconami.Caravans.util.Utils.getRemoteInventory;
import static com.zconami.Caravans.util.Utils.isNotCurrency;
import static com.zconami.Caravans.util.Utils.sendMessage;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.gestern.gringotts.AccountInventory;

import com.google.common.collect.Maps;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.domain.BeneficiaryCreateParameters;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.domain.EntityObserver;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.domain.RegionCreateParameters;
import com.zconami.Caravans.event.CaravanPostCreateEvent;
import com.zconami.Caravans.event.RegionDestroyEvent;
import com.zconami.Caravans.event.RegionInteractEvent;
import com.zconami.Caravans.event.RegionPreCreateEvent;
import com.zconami.Caravans.repository.BeneficiaryRepository;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.repository.RegionRepository;

public class RegionEventListener implements Listener, EntityObserver<Region> {

    // ===================================
    // CONSTANTS
    // ===================================

    private final static String REGION_MARKER_SET_ID = "caravansRegionMarkers";
    private final static String REGION_MARKER_SET_LABEL = "Trade Posts";
    private final static String REGION_MARKER_ICON = "coins";

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final DynmapAPI dynmap;
    private final CaravanRepository caravanRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final RegionRepository regionRepository;
    private final Map<Player, InvestmentInventoryWrapper> playerInvestmentInventories = Maps.newHashMap();

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionEventListener(DynmapAPI dynmap, CaravanRepository caravanRepository,
            BeneficiaryRepository beneficiaryRepository, RegionRepository regionRepository) {
        this.dynmap = dynmap;
        this.caravanRepository = caravanRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.regionRepository = regionRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onRegionCreate(RegionPreCreateEvent event) {
        final String name = event.getName();
        final int radius = event.getRadius();
        final Location location = event.getLocation();
        final RegionCreateParameters params = new RegionCreateParameters(UUID.randomUUID().toString(), name, location,
                radius);
        params.setTypeOfGood(event.getTypeOfGood());

        final Region savedRegion = regionRepository.save(Region.create(params));
        createMarker(savedRegion);
    }

    @EventHandler
    public void onRegionInteract(RegionInteractEvent event) {
        final Region origin = regionRepository.findByName(event.getName());
        final Player player = event.getPlayer();
        if (origin == null) {
            sendMessage(player, "Region not setup properly");
            return;
        }

        final Beneficiary beneficiary = findOrCreate(player);
        final Faction playerFaction = MPlayer.get(player).getFaction();
        if (!beneficiary.getFaction().getId().equals(playerFaction.getId())) {
            beneficiary.setFaction(playerFaction);
        }

        final boolean exisitingCaravan = caravanRepository.findByBeneficiary(beneficiary) != null;
        if (exisitingCaravan) {
            sendMessage(player, "You've already got an active caravan!");
            return;
        }

        final int cooldownSeconds = getCaravansConfig().getInt("caravans.cooldownAfterSuccess");
        final long cooldownFinishedMillis = beneficiary.getLastSuccessfulCaravan() + cooldownSeconds * 1000;
        if (System.currentTimeMillis() < cooldownFinishedMillis) {
            final java.util.Date cooldownFinisihedDate = new java.util.Date(cooldownFinishedMillis);
            sendMessage(player, "You've had a successful run recently! You can start another at "
                    + ChatColor.DARK_PURPLE + DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(cooldownFinisihedDate));
            return;
        }

        final boolean onePerFaction = getCaravansConfig().getBoolean("caravans.oneActivePerFaction");
        if (onePerFaction) {
            if (playerFaction.getId().equals(Factions.ID_NONE)) {
                sendMessage(player, "You must be in a faction to have a caravan!");
                return;
            }
            if (caravanRepository.findByFaction(playerFaction) != null) {
                sendMessage(player, "Your faction already has an active caravan!");
                return;
            }
        }

        final Inventory inventory = getRemoteInventory(origin.getTypeOfGood() + " Investment");
        player.openInventory(inventory);

        final InvestmentInventoryWrapper investmentInventory = new InvestmentInventoryWrapper(origin, inventory);
        playerInvestmentInventories.put(player, investmentInventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        final HumanEntity humanEntity = event.getPlayer();

        if (humanEntity instanceof Player) {
            final Player player = (Player) humanEntity;
            final InvestmentInventoryWrapper investmentInventory = playerInvestmentInventories.get(player);
            if (investmentInventory != null && investmentInventory.getInventory() != null) {
                final Inventory inventory = investmentInventory.getInventory();

                if (new AccountInventory(inventory).balance() > 0) {
                    final Beneficiary beneficiary = findOrCreate(player);
                    final Caravan caravan = caravanRepository
                            .save(Caravan.muleCaravan(beneficiary, investmentInventory.getRegion(), inventory));
                    final CaravanPostCreateEvent caravanCreateEvent = new CaravanPostCreateEvent(caravan);
                    Bukkit.getServer().getPluginManager().callEvent(caravanCreateEvent);
                } else {
                    sendMessage(player, "You must invest some " + getGringottsNamePlural()
                            + " in cargo before you can create a caravan!");
                }
                inventory.forEach(itemStack -> {
                    if (isNotCurrency(itemStack)) {
                        final Location location = investmentInventory.getRegion().getCenter();
                        location.getWorld().dropItem(location, itemStack);
                    }
                });
                inventory.clear();
                playerInvestmentInventories.remove(player);
            }
        }
    }

    @EventHandler
    public void onRegionDestroy(RegionDestroyEvent event) {
        final Region region = regionRepository.findByName(event.getName());
        region.addObserver(this);
        if (caravanRepository.activeFrom(region).isEmpty()) {
            region.remove();
        } else {
            region.setRemoveAfterLastCaravan(true);
        }
    }

    // ===================================
    // IMPLEMENTATION OF ENTITY OBSERVER
    // ===================================

    @Override
    public void entityChanged(Region region) {
    }

    @Override
    public void entityRemoved(Region region) {
        removeMarker(region);
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private String getMarkerId(Region region) {
        return region.getKey().replace("-", "");
    }

    private void createMarker(Region region) {
        final MarkerSet markerSet = getMarkerSet();
        final Location regionCenter = region.getCenter();

        final Marker createdMarker = markerSet.createMarker(getMarkerId(region), region.getName(), true,
                regionCenter.getWorld().getName(), regionCenter.getX(), regionCenter.getY(), regionCenter.getZ(),
                getMarkerIcon(), true);
        createdMarker.setDescription("Exports " + region.getTypeOfGood());
    }

    private void removeMarker(Region region) {
        final MarkerSet markerSet = getMarkerSet();

        final Marker foundMarker = markerSet.findMarker(getMarkerId(region));
        if (foundMarker != null) {
            foundMarker.deleteMarker();
        }
    }

    private MarkerIcon getMarkerIcon() {
        final MarkerAPI markerAPI = dynmap.getMarkerAPI();
        return markerAPI.getMarkerIcon(REGION_MARKER_ICON);
    }

    private MarkerSet getMarkerSet() {
        final MarkerAPI markerAPI = dynmap.getMarkerAPI();
        final MarkerSet existingMarketSet = markerAPI.getMarkerSet(REGION_MARKER_SET_ID);
        if (existingMarketSet != null) {
            return existingMarketSet;
        } else {
            return markerAPI.createMarkerSet(REGION_MARKER_SET_ID, REGION_MARKER_SET_LABEL, null, true);
        }
    }

    private Beneficiary findOrCreate(Player player) {
        final Beneficiary existing = beneficiaryRepository.find(player.getUniqueId().toString());
        if (existing != null) {
            return existing;
        }
        final BeneficiaryCreateParameters params = new BeneficiaryCreateParameters(player);
        return beneficiaryRepository.save(Beneficiary.create(params));
    }

    private boolean isInvestmentInventory(Inventory inventory) {
        for (InvestmentInventoryWrapper wrapper : playerInvestmentInventories.values()) {
            if (wrapper.getInventory().equals(inventory)) {
                return true;
            }
        }
        return false;
    }

    // ===================================
    // NESTED CLASSES
    // ===================================

    protected class InvestmentInventoryWrapper {

        private final Region region;
        private final Inventory inventory;

        protected InvestmentInventoryWrapper(Region region, Inventory inventory) {
            this.region = region;
            this.inventory = inventory;
        }

        public Region getRegion() {
            return region;
        }

        public Inventory getInventory() {
            return inventory;
        }

    }

}
