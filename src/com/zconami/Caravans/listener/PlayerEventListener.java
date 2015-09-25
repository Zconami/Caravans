package com.zconami.Caravans.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.util.CaravansUtils;

public class PlayerEventListener implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final CaravanRepository caravanRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public PlayerEventListener(CaravanRepository caravanRepository) {
        this.caravanRepository = caravanRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Entity vehicle = player.getVehicle();
        if (player.isInsideVehicle() && CaravansUtils.isCaravan(vehicle)) {
            final Caravan caravan = caravanRepository.find((Horse) vehicle);
            caravan.dismount();
        }
    }

}
