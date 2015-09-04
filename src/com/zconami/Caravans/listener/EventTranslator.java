package com.zconami.Caravans.listener;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import org.testng.collections.Lists;

import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.event.CaravanDestroyEvent;
import com.zconami.Caravans.event.CaravanMountEvent;
import com.zconami.Caravans.event.CaravanMoveEvent;
import com.zconami.Caravans.event.RegionCreateEvent;
import com.zconami.Caravans.event.RegionInteractEvent;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.util.CaravansUtils;

public class EventTranslator implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final CaravanRepository caravanRepository;

    private final Pattern[] signValidators = new Pattern[] {
            Pattern.compile("^\\[Caravans\\]$"),
            Pattern.compile("^.*$"),
            Pattern.compile("^Exports: (Yes|No)$"),
            Pattern.compile("^Imports: (Yes|No)$")
    };

    public EventTranslator(CaravanRepository caravanRepository) {
        this.caravanRepository = caravanRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        final Sign sign = (Sign) event.getBlock();
        final List<Matcher> matchers = regionSignMatchers(sign);
        if (matchers.stream().allMatch(Matcher::matches)) {
            final RegionCreateEvent regionCreateEvent = new RegionCreateEvent(sign, matchers);
            Bukkit.getServer().getPluginManager().callEvent(regionCreateEvent);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getMaterial() == Material.SIGN) {
            final Sign sign = (Sign) event.getClickedBlock();
            final List<Matcher> matchers = regionSignMatchers(sign);
            if (matchers.stream().allMatch(Matcher::matches)) {
                final String name = matchers.get(1).group(0);
                final RegionInteractEvent regionInteractEvent = new RegionInteractEvent(sign, name, event.getPlayer());
                Bukkit.getServer().getPluginManager().callEvent(regionInteractEvent);
            }
        }
    }

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

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private List<Matcher> regionSignMatchers(Sign sign) {
        final String[] lines = sign.getLines();

        final List<Matcher> matchers = Lists.newArrayList(lines.length);
        for (int i = 0; i < lines.length; i++) {
            final String line = ChatColor.stripColor(lines[i]);
            final Pattern validator = signValidators[i];
            final Matcher matcher = validator.matcher(line);
            matchers.add(matcher);
        }

        return matchers;
    }

}
