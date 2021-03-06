package com.zconami.Caravans.listener;

import static com.zconami.Core.util.Utils.isSignBlock;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import com.google.common.collect.Lists;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.event.CaravanDestroyEvent;
import com.zconami.Caravans.event.CaravanMountEvent;
import com.zconami.Caravans.event.CaravanMoveEvent;
import com.zconami.Caravans.event.RegionDestroyEvent;
import com.zconami.Caravans.event.RegionInteractEvent;
import com.zconami.Caravans.event.RegionPreCreateEvent;
import com.zconami.Caravans.repository.BeneficiaryRepository;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.util.CaravansUtils;
import com.zconami.Caravans.util.GringottsUtils;

public class EventTranslator implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final BeneficiaryRepository beneficiaryRepository;
    private final CaravanRepository caravanRepository;

    private final Pattern[] signValidators = new Pattern[] {
            Pattern.compile("\\[Caravans\\]"),
            Pattern.compile("(?<regionName>[a-zA-Z0-9_]+)"),
            Pattern.compile("Exports"),
            Pattern.compile("(?<typeOfGood>[a-zA-Z0-9_]+)")
    };

    public EventTranslator(BeneficiaryRepository beneficiaryRepository, CaravanRepository caravanRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.caravanRepository = caravanRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        final Player player = event.getPlayer();
        final List<Matcher> matchers = regionSignMatchers(event.getLines());
        if (matchers.stream().allMatch(Matcher::find)) {
            if (player.hasPermission("caravans.region")) {
                final Block signBlock = event.getBlock();
                final RegionPreCreateEvent regionCreateEvent = new RegionPreCreateEvent(signBlock, matchers);
                Bukkit.getServer().getPluginManager().callEvent(regionCreateEvent);

                event.setLine(0, ChatColor.GREEN + "[Caravans]");
                event.setLine(1, regionCreateEvent.getName());
                event.setLine(3, regionCreateEvent.getTypeOfGood());
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isSignBlock(block)) {
            final Sign sign = (Sign) block.getState();
            final List<Matcher> matchers = regionSignMatchers(sign.getLines());
            if (matchers.stream().allMatch(Matcher::find)) {
                final String name = matchers.get(1).group("regionName");
                final RegionInteractEvent regionInteractEvent = new RegionInteractEvent(block, name, event.getPlayer());
                Bukkit.getServer().getPluginManager().callEvent(regionInteractEvent);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        if (isSignBlock(block)) {
            final Sign sign = (Sign) block.getState();
            final List<Matcher> matchers = regionSignMatchers(sign.getLines());
            if (matchers.stream().allMatch(Matcher::find)) {
                if (player.hasPermission("caravans.region")) {
                    final String name = matchers.get(1).group("regionName");
                    final RegionDestroyEvent regionDestroyEvent = new RegionDestroyEvent(name);
                    Bukkit.getServer().getPluginManager().callEvent(regionDestroyEvent);
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Entity vehicle = player.getVehicle();
        if (player.isInsideVehicle() && CaravansUtils.isCaravan(vehicle)) {
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
            event.getDrops().removeIf(GringottsUtils::isNotCurrency);
            final CaravanDestroyEvent caravanDestroyEvent = new CaravanDestroyEvent(caravan, entity.getKiller());
            Bukkit.getServer().getPluginManager().callEvent(caravanDestroyEvent);
        }
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private List<Matcher> regionSignMatchers(String[] lines) {
        final List<Matcher> matchers = Lists.newArrayList();
        for (int i = 0; i < lines.length; i++) {
            final String line = ChatColor.stripColor(lines[i]);
            final Pattern validator = signValidators[i];
            final Matcher matcher = validator.matcher(line);
            matcher.reset();
            matchers.add(matcher);
        }

        return matchers;
    }

}
