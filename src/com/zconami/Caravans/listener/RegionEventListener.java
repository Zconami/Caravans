package com.zconami.Caravans.listener;

import static com.zconami.Caravans.util.Utils.getCaravansConfig;

import java.util.UUID;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.domain.BeneficiaryCreateParameters;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.domain.RegionCreateParameters;
import com.zconami.Caravans.event.CaravanPostCreateEvent;
import com.zconami.Caravans.event.RegionDestroyEvent;
import com.zconami.Caravans.event.RegionInteractEvent;
import com.zconami.Caravans.event.RegionPreCreateEvent;
import com.zconami.Caravans.repository.BeneficiaryRepository;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.repository.RegionRepository;

public class RegionEventListener implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final CaravanRepository caravanRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final RegionRepository regionRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionEventListener(CaravanRepository caravanRepository, BeneficiaryRepository beneficiaryRepository,
            RegionRepository regionRepository) {
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

        params.setOrigin(event.getIsOrigin());
        params.setDestination(event.getIsDestination());

        regionRepository.save(Region.create(params));

    }

    @EventHandler
    public void onRegionInteract(RegionInteractEvent event) {
        final Region origin = regionRepository.findByName(event.getName());
        if (origin == null) {
            event.getPlayer().sendMessage("Region not setup properly");
            return;
        }

        if (!origin.isOrigin()) {
            event.getPlayer().sendMessage("This region does not allow exports");
            return;
        }

        final Beneficiary beneficiary = findOrCreate(event.getPlayer());
        final Faction playerFaction = MPlayer.get(event.getPlayer()).getFaction();
        if (!beneficiary.getFaction().getId().equals(playerFaction.getId())) {
            beneficiary.setFaction(playerFaction);
        }

        final boolean exisitingCaravan = caravanRepository.findByBeneficiary(beneficiary) != null;
        if (exisitingCaravan) {
            event.getPlayer().sendMessage("You've already got an active caravan!");
            return;
        }

        final int cooldownSeconds = getCaravansConfig().getInt("caravans.cooldownAfterSuccess");
        final long cooldownFinishedMillis = beneficiary.getLastSuccessfulCaravan() + cooldownSeconds * 1000;
        if (System.currentTimeMillis() < cooldownFinishedMillis) {
            final java.util.Date cooldownFinisihedDate = new java.util.Date(cooldownFinishedMillis);
            event.getPlayer().sendMessage("You've had a successful run recently! You can start another at "
                    + ChatColor.DARK_PURPLE + DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(cooldownFinisihedDate));
            return;
        }

        final boolean onePerFaction = getCaravansConfig().getBoolean("caravans.oneActivePerFaction");
        if (onePerFaction) {
            if (playerFaction.getId().equals(Factions.ID_NONE)) {
                event.getPlayer().sendMessage("You must be in a faction to have a caravan!");
                return;
            }
            if (caravanRepository.findByFaction(playerFaction) != null) {
                event.getPlayer().sendMessage("Your faction already has an active caravan!");
                return;
            }
        }

        final Caravan caravan = caravanRepository.save(Caravan.muleCaravan(beneficiary, origin));
        final CaravanPostCreateEvent caravanCreateEvent = new CaravanPostCreateEvent(caravan);
        event.getPlayer().sendMessage("Carvan created at " + caravan.getProfitStrategy().name() + " profit rate");
        Bukkit.getServer().getPluginManager().callEvent(caravanCreateEvent);
    }

    @EventHandler
    public void onRegionDestroy(RegionDestroyEvent event) {
        final Region region = regionRepository.findByName(event.getName());
        if (caravanRepository.activeFrom(region).isEmpty()) {
            region.remove();
        } else {
            region.setRemoveAfterLastCaravan(true);
        }
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private Beneficiary findOrCreate(Player player) {
        final Beneficiary existing = beneficiaryRepository.find(player.getUniqueId().toString());
        if (existing != null) {
            return existing;
        }
        final BeneficiaryCreateParameters params = new BeneficiaryCreateParameters(player);
        return beneficiaryRepository.save(Beneficiary.create(params));
    }

}
