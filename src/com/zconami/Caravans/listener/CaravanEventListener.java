package com.zconami.Caravans.listener;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;
import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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

import com.zconami.Caravans.CaravansPlugin;
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
import com.zconami.Caravans.util.ScoreboardUtils;
import com.zconami.Caravans.util.Utils;

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
        final String playerName = caravan.getBeneficiary().getBukkitEntity().getName();
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
                    resetToOriginIfNotStarted(caravan, false);
                }
                caravan.caravanHasStarted();
                final CaravansPlugin plugin = getCaravansPlugin();
                final boolean announceStart = plugin.getConfig().getBoolean("broadcasts.announceStart");
                final int announceLocationDelay = plugin.getConfig().getInt("broadcasts.announceLocationDelay");
                if (announceStart) {
                    final Location location = horse.getLocation();
                    final String beneficiaryName = caravan.getBeneficiary().getBukkitEntity().getName();
                    StringBuilder announcementBuilder = new StringBuilder(
                            String.format("A trade caravan with an investment of §a%s§f has started for %s",
                                    Util.format(caravan.getInvestment()), beneficiaryName));
                    if (announceLocationDelay <= 0) {
                        announcementBuilder
                                .append(String.format(" @ %d,%d!", location.getBlockX(), location.getBlockZ()));
                        ScoreboardUtils.setUpScoreboardCaravanTask(caravan);
                        caravan.locationIsPublic();
                    } else {
                        announcementBuilder.append("!");
                        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                            @Override
                            public void run() {
                                if (caravan.getBukkitEntity().isValid()) {
                                    plugin.getServer()
                                            .broadcastMessage(String.format("The location of %s's caravan is %d,%d",
                                                    beneficiaryName, location.getBlockX(), location.getBlockZ()));
                                    ScoreboardUtils.setUpScoreboardCaravanTask(caravan);
                                    caravan.locationIsPublic();
                                }
                            }
                        }, Utils.ticks(announceLocationDelay));
                    }
                    Bukkit.getServer().broadcastMessage(announcementBuilder.toString());
                }
            }
        }
    }

    @EventHandler
    public void onCaravanMove(CaravanMoveEvent event) {
        final Caravan caravan = event.getCaravan();
        final Horse horse = caravan.getBukkitEntity();
        final Location location = horse.getLocation();
        final Region origin = caravan.getOrigin();
        if (!origin.contains(location)) {
            if (!resetToOriginIfNotStarted(caravan)) {
                final List<Region> regions = regionRepository.all();
                for (Region region : regions) {
                    if (region.isDestination() && region.contains(location)) {
                        final Beneficiary beneficiary = caravan.getBeneficiary();
                        final String beneficiaryName = beneficiary.getBukkitEntity().getName();
                        final long beneficiaryReturn = caravan.getReturn(region);
                        final GringottsAccount beneficiaryAccount = Gringotts.G.accounting
                                .getAccount(new PlayerAccountHolder(beneficiary.getBukkitEntity()));
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
            final String beneficiaryName = caravan.getBeneficiary().getBukkitEntity().getName();
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
            if (!event.getPlayer().getUniqueId().equals(caravan.getBeneficiary().getBukkitEntity().getUniqueId())) {
                event.getPlayer().sendMessage("Only " + caravan.getBeneficiary().getBukkitEntity().getName()
                        + ", the beneficiary, can access this cargo");
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

    private boolean resetToOriginIfNotStarted(Caravan caravan) {
        return resetToOriginIfNotStarted(caravan, true);
    }

    private boolean resetToOriginIfNotStarted(Caravan caravan, boolean remount) {
        final Horse horse = caravan.getBukkitEntity();
        if (!caravan.isCaravanStarted()) {
            final Entity passenger = horse.getPassenger();
            final Location teleportLocation = caravan.getOrigin().getCenter();
            horse.eject();
            horse.teleport(teleportLocation);
            if (passenger != null) {
                passenger.sendMessage("You must start the caravan before leaving the region!");
                passenger.teleport(teleportLocation);
                if (remount) {
                    horse.setPassenger(passenger);
                }
            }
            return true;
        }
        return false;
    }

}
