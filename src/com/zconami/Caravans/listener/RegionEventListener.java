package com.zconami.Caravans.listener;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;

import java.util.List;
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

public class RegionEventListener implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public RegionEventListener() {
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

        getCaravansPlugin().DB.save(Region.create(params));

    }

    @EventHandler
    public void onRegionInteract(RegionInteractEvent event) {
        final Region origin = getCaravansPlugin().DB.find(Region.class).where().eq(Region.NAME, event.getName())
                .findUnique();
        if (origin == null || !origin.isOrigin()) {
            event.getPlayer().sendMessage("Given region does not exist or is not a valid origin!");
            return;
        }

        if (MPlayer.get(event.getPlayer()).getFaction().getId().equals(Factions.ID_NONE)) {
            event.getPlayer().sendMessage("Beneficiary must be in a faction to have a caravan!");
            return;
        }

        final Beneficiary beneficiary = findOrCreate(event.getPlayer());

        final Caravan caravan = Caravan.muleCaravan(beneficiary, origin);
        getCaravansPlugin().DB.save(caravan);

        final CaravanPostCreateEvent caravanCreateEvent = new CaravanPostCreateEvent(caravan);
        Bukkit.getServer().getPluginManager().callEvent(caravanCreateEvent);
    }

    @EventHandler
    public void onRegionDestroy(RegionDestroyEvent event) {
        final Region origin = getCaravansPlugin().DB.find(Region.class).where().eq(Region.NAME, event.getName())
                .findUnique();
        final List<Caravan> activeCaravansFromOrigin = getCaravansPlugin().DB.find(Caravan.class).where()
                .eq(Caravan.ORIGIN, origin).findList();
        if (activeCaravansFromOrigin.isEmpty()) {
            getCaravansPlugin().DB.delete(origin);
        } else {
            origin.setRemoveAfterLastCaravan(true);
        }
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private Beneficiary findOrCreate(Player player) {
        final Beneficiary existing = getCaravansPlugin().DB.find(Beneficiary.class).where()
                .eq(Beneficiary.BUKKIT_ENTITY_ID, player.getUniqueId()).findUnique();
        if (existing != null) {
            return existing;
        }

        final BeneficiaryCreateParameters params = new BeneficiaryCreateParameters(player);
        final Beneficiary newBeneficiary = Beneficiary.create(params);
        getCaravansPlugin().DB.save(newBeneficiary);
        return newBeneficiary;
    }

}
