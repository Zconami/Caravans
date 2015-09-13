package com.zconami.Caravans.listener;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;
import static com.zconami.Caravans.util.Utils.getScheduler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.util.CaravansUtils;
import com.zconami.Caravans.util.ScoreboardUtils;
import com.zconami.Caravans.util.Utils;

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
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Have to run later for player to have attributes set properly
        getScheduler().runTaskLater(getCaravansPlugin(), new Runnable() {
            @Override
            public void run() {
                final Player player = event.getPlayer();
                final Entity vehicle = player.getVehicle();
                if (player.isInsideVehicle()) {
                    final DataKey caravanData = caravanRepository.getDataForKey(vehicle.getUniqueId().toString());
                    if (caravanData != null) {
                        caravanData.setBoolean(Caravan.PASSENGER_LOGGED_OUT, false);
                        caravanRepository.save();
                    }
                }
            }
        }, Utils.ticksFromSeconds(3));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Entity vehicle = player.getVehicle();
        if (player.isInsideVehicle() && CaravansUtils.isCaravan(vehicle)) {
            final Caravan caravan = caravanRepository.find((Horse) vehicle);
            caravan.passengerLoggedOut();
            ScoreboardUtils.stopScoreboard(caravan);
            caravanRepository.unload(caravan);
        }
    }

}
