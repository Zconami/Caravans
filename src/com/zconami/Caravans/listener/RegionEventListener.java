package com.zconami.Caravans.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.massivecraft.factions.Factions;
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
        if (origin == null || !origin.isOrigin()) {
            event.getPlayer().sendMessage("Given region does not exist or is not a valid origin!");
            return;
        }

        if (MPlayer.get(event.getPlayer()).getFaction().getId().equals(Factions.ID_NONE)) {
            event.getPlayer().sendMessage("Beneficiary must be in a faction to have a caravan!");
            return;
        }

        final Beneficiary beneficiary = findOrCreate(event.getPlayer());

        final Caravan caravan = caravanRepository.save(Caravan.muleCaravan(beneficiary, origin));
        final CaravanPostCreateEvent caravanCreateEvent = new CaravanPostCreateEvent(caravan);
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
        final Beneficiary existing = beneficiaryRepository.find(player);
        if (existing != null) {
            return existing;
        }
        final BeneficiaryCreateParameters params = new BeneficiaryCreateParameters(player);
        return beneficiaryRepository.save(Beneficiary.create(params));
    }

}
