package com.zconami.Caravans.listener;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;
import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.gestern.gringotts.Configuration;
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

    private final CaravanRepository caravanRepository;
    private final RegionRepository regionRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public CaravanEventListener(CaravanRepository caravanRepository, RegionRepository regionRepository) {
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
        final Caravan caravan = event.getCaravan();
        final Horse horse = caravan.getBukkitEntity();
        if (caravan.getInvestment() <= 0) {
            event.getPlayer().sendMessage("You must invest some §a" + Configuration.CONF.currency.namePlural
                    + "§f before starting a trade caravan!");
            horse.eject();
        } else {
            if (!caravan.isCaravanStarted()) {
                if (!caravan.getOrigin().contains(horse.getLocation())) {
                    // Tried to start a caravan outside of origin, probably
                    // abuse attempt by pushing horse
                    resetToOrigin(caravan, false);
                }
                caravan.caravanHasStarted();
            }
        }
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
            if (!caravan.isCaravanStarted()) {
                if (caravan.getInvestment() <= 0) {
                    // Hasn't started yet, bring back to origin
                    resetToOrigin(caravan, true);
                } else {
                    // Has investment, so start caravan
                    caravan.caravanHasStarted();
                }
            } else {
                final List<Region> regions = regionRepository.all();
                for (Region region : regions) {
                    if (region.isDestination() && region.contains(location)) {
                        final Beneficiary beneficiary = caravan.getBeneficiary();
                        final String beneficiaryName = beneficiary.getName();
                        final long beneficiaryReturn = caravan.getReturn(region);

                        beneficiary.successfulCaravan();

                        final boolean announceSuccess = getCaravansPlugin().getConfig()
                                .getBoolean("broadcasts.announceSuccess");
                        if (announceSuccess) {
                            final String announcement = String.format(
                                    "A trade caravan with an investment of §a%s§f completed successfully by %s returning §a%s§f!",
                                    Util.format(caravan.getInvestment()), beneficiaryName,
                                    Util.format(beneficiaryReturn));
                            Bukkit.getServer().broadcastMessage(announcement);
                        }

                        final OfflinePlayer offlinePlayer = Bukkit
                                .getOfflinePlayer(UUID.fromString(beneficiary.getKey()));
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
    }

    @EventHandler
    public void onCaravanDestroy(CaravanDestroyEvent event) {
        final boolean announceDestroy = getCaravansPlugin().getConfig().getBoolean("broadcasts.announceDestory");
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
            Bukkit.getServer().broadcastMessage(announcementBuilder.toString());

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
            final Caravan caravan = caravanRepository.find((Horse) holder);
            if (!event.getPlayer().getUniqueId().toString().equals(caravan.getBeneficiary().getKey())) {
                event.getPlayer().sendMessage(
                        "Only " + caravan.getBeneficiary().getName() + ", the beneficiary, can access this cargo");
                event.setCancelled(true);
            } else if (caravan.isCaravanStarted()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("This trade caravan has already started with a value of §a"
                        + Util.format(caravan.getInvestment()) + "§f");
            }
        }
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private void resetToOrigin(Caravan caravan, boolean remount) {
        final Horse horse = caravan.getBukkitEntity();
        final Entity passenger = horse.getPassenger();
        final Location teleportLocation = caravan.getOrigin().getCenter();
        teleportLocation.setPitch(horse.getLocation().getPitch());
        teleportLocation.setYaw(horse.getLocation().getYaw());
        horse.eject();
        horse.teleport(teleportLocation);
        if (passenger != null) {
            passenger.sendMessage("You must start the caravan before leaving the region!");
            passenger.teleport(teleportLocation);
            if (remount) {
                horse.setPassenger(passenger);
            }
        }
    }

}
