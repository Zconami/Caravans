package com.zconami.Caravans.listener;

import static com.zconami.Caravans.util.Utils.broadcastMessage;
import static com.zconami.Caravans.util.Utils.getCaravansConfig;
import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.dynmap.DynmapAPI;
import org.gestern.gringotts.Gringotts;
import org.gestern.gringotts.GringottsAccount;
import org.gestern.gringotts.Util;
import org.gestern.gringotts.accountholder.PlayerAccountHolder;

import com.zconami.Caravans.domain.Beneficiary;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.domain.Region;
import com.zconami.Caravans.event.CaravanDestroyEvent;
import com.zconami.Caravans.event.CaravanMountEvent;
import com.zconami.Caravans.event.CaravanMoveEvent;
import com.zconami.Caravans.event.CaravanPostCreateEvent;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.repository.RegionRepository;
import com.zconami.Caravans.util.CaravansUtils;

public class CaravanEventListener implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final DynmapAPI dynmap;
    private final CaravanRepository caravanRepository;
    private final RegionRepository regionRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravanEventListener(DynmapAPI dynmap, CaravanRepository caravanRepository,
            RegionRepository regionRepository) {
        this.dynmap = dynmap;
        this.caravanRepository = caravanRepository;
        this.regionRepository = regionRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onCaravanCreate(CaravanPostCreateEvent event) {
        final Caravan caravan = event.getCaravan();
        final String playerName = caravan.getBeneficiary().getName();
        getLogger().info(
                "Caravan created for " + playerName + " with investment of " + Util.format(caravan.getInvestment()));
    }

    @EventHandler
    public void onCaravanMount(CaravanMountEvent event) {
    }

    @EventHandler
    public void onCaravanMove(CaravanMoveEvent event) {
        final Caravan caravan = event.getCaravan();
        final Horse horse = caravan.getBukkitEntity();
        final Location location = horse.getLocation();
        final Region origin = caravan.getOrigin();

        if (!location.getChunk().equals(caravan.getChunk())) {
            caravan.setChunk(location.getChunk());
        }

        final boolean hasLeftOrigin = !origin.contains(location);
        if (hasLeftOrigin) {

            final List<Region> regions = regionRepository.all();
            for (Region region : regions) {
                if (region.contains(location)) {
                    final Beneficiary beneficiary = caravan.getBeneficiary();
                    final String beneficiaryName = beneficiary.getName();
                    final long beneficiaryReturn = caravan.getReturn(region);

                    beneficiary.successfulCaravan();

                    final boolean announceSuccess = getCaravansConfig().getBoolean("broadcasts.announceSuccess");
                    if (announceSuccess) {
                        final String announcement = String.format(
                                "A trade caravan with an investment of §a%s§f completed successfully by %s returning §a%s§f!",
                                Util.format(caravan.getInvestment()), beneficiaryName, Util.format(beneficiaryReturn));
                        broadcastMessage(announcement);
                    }

                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(beneficiary.getKey()));
                    final GringottsAccount beneficiaryAccount = Gringotts.G.accounting
                            .getAccount(new PlayerAccountHolder(offlinePlayer));
                    beneficiaryAccount.add(beneficiaryReturn);
                    if (origin.isRemoveAfterLastCaravan() && caravanRepository.activeFrom(origin).size() == 1) {
                        origin.remove();
                    }
                    caravan.remove();
                    break;
                }
            }

        }
    }

    @EventHandler
    public void onCaravanDestroy(CaravanDestroyEvent event) {
        final boolean announceDestroy = getCaravansConfig().getBoolean("broadcasts.announceDestory");
        if (announceDestroy) {
            final Caravan caravan = event.getCaravan();
            final String beneficiaryName = caravan.getBeneficiary().getName();
            final StringBuilder announcementBuilder = new StringBuilder(
                    String.format("%s's trade caravan with an investment of §a%s§f was destroyed", beneficiaryName,
                            Util.format(caravan.getInvestment())));
            if (event.getDestroyer() != null) {
                announcementBuilder.append(String.format(" by %s!", event.getDestroyer().getName()));
            } else {
                announcementBuilder.append("!");
            }
            broadcastMessage(announcementBuilder.toString());

            final Region origin = caravan.getOrigin();
            if (origin.isRemoveAfterLastCaravan() && caravanRepository.activeFrom(origin).size() == 1) {
                origin.remove();
            }
            caravan.remove();
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Horse && CaravansUtils.isCaravan((Horse) holder)) {
            event.setCancelled(true);
        }
    }

}
