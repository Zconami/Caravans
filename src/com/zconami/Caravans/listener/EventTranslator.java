package com.zconami.Caravans.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.event.CaravanDestroyEvent;
import com.zconami.Caravans.event.CaravanMountEvent;
import com.zconami.Caravans.event.CaravanMoveEvent;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.util.CaravansUtils;

public class EventTranslator implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final CaravanRepository caravanRepository;

    public EventTranslator(CaravanRepository caravanRepository) {
        this.caravanRepository = caravanRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Entity vehicle = event.getPlayer().getVehicle();
        if (event.getPlayer().isInsideVehicle() && CaravansUtils.isCaravan(vehicle)) {
            final Caravan caravan = caravanRepository.find((Horse) vehicle);
            final CaravanMoveEvent caravanMoveEvent = new CaravanMoveEvent(caravan);
            Bukkit.getServer().getPluginManager().callEvent(caravanMoveEvent);
        }
    }

    @EventHandler
    public void onEntityMount(EntityMountEvent event) {
        final Entity mount = event.getMount();
        if (mount instanceof Horse && CaravansUtils.isCaravan((Horse) mount)) {
            final Caravan caravan = caravanRepository.find((Horse) mount);
            final CaravanMountEvent caravanMountEvent = new CaravanMountEvent(caravan, (Player) event.getEntity());
            Bukkit.getServer().getPluginManager().callEvent(caravanMountEvent);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        if (CaravansUtils.isCaravan(entity)) {
            final Caravan caravan = caravanRepository.find((Horse) entity);
            final CaravanDestroyEvent caravanDestroyEvent = new CaravanDestroyEvent(caravan, entity.getKiller());
            Bukkit.getServer().getPluginManager().callEvent(caravanDestroyEvent);
        }
    }

}
