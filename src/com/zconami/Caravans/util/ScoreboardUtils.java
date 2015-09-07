package com.zconami.Caravans.util;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;

import java.util.Collection;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.gestern.gringotts.Configuration;

import com.google.common.collect.Maps;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.util.RelationUtil;
import com.zconami.Caravans.domain.Caravan;

public class ScoreboardUtils {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final Map<String, Integer> KEY_TASKS = Maps.newHashMap();
    private static final Map<String, Map<Rel, Scoreboard>> KEY_REL_SCOREBOARD = Maps.newHashMap();

    private static final int UPDATE_INTERVAL_TICKS = Utils.ticks(5);

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private ScoreboardUtils() {
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static void setUpScoreboardCaravanTask(Caravan caravan) {

        Map<Rel, Scoreboard> relScoreboard = Maps.newHashMap();
        for (Rel rel : Rel.values()) {
            relScoreboard.put(rel, scoreboardWithRelation(caravan, rel));
        }

        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final Integer taskId = Integer.valueOf(scheduler.scheduleSyncRepeatingTask(getCaravansPlugin(), new Runnable() {
            @Override
            public void run() {
                final Horse target = caravan.getBukkitEntity();
                if (target.isValid()) {
                    final Location location = target.getLocation();
                    for (Scoreboard scoreboard : relScoreboard.values()) {
                        final Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

                        final Score scoreX = objective.getScore("§6X§f");
                        scoreX.setScore(location.getBlockX());

                        final Score scoreZ = objective.getScore("§6Z§f");
                        scoreZ.setScore(location.getBlockZ());

                    }
                }
            }
        }, 0L, UPDATE_INTERVAL_TICKS));
        KEY_TASKS.put(caravan.getKey(), taskId);

        KEY_REL_SCOREBOARD.put(caravan.getKey(), relScoreboard);
    }

    public static void stopScoreboard(Caravan caravan) {
        cancelScrboardUpdateTask(caravan);

        final Map<Rel, Scoreboard> relScoreboard = KEY_REL_SCOREBOARD.get(caravan.getKey());
        if (relScoreboard != null) {
            for (Scoreboard scoreboard : relScoreboard.values()) {
                unregisterObjective(scoreboard);
            }
        }
        KEY_TASKS.remove(caravan.getKey());
        KEY_REL_SCOREBOARD.remove(caravan.getKey());
    }

    public static void stopAll() {
        KEY_TASKS.values().forEach(ScoreboardUtils::cancelTask);
        final Collection<Map<Rel, Scoreboard>> relMaps = KEY_REL_SCOREBOARD.values();
        relMaps.forEach(map -> map.values().forEach(ScoreboardUtils::unregisterObjective));
    }

    public static void showScoreboard(Player player, Caravan caravan) {
        final Rel relation = RelationUtil.getRelationOfThatToMe(caravan.getFaction(), MPlayer.get(player));

        if (!KEY_REL_SCOREBOARD.containsKey(caravan.getKey())) {
            // If scoreboard isn't setup (perhaps reload or restart), cleanup
            // and recreate
            cancelScrboardUpdateTask(caravan);
            setUpScoreboardCaravanTask(caravan);
        }

        final Scoreboard scoreboard = KEY_REL_SCOREBOARD.get(caravan.getKey()).get(relation);
        player.setScoreboard(scoreboard);
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private static String getName(Caravan caravan, Rel relation) {
        final String beneficiaryName = caravan.getBeneficiary().getName();
        final String factionName = relation.getColor() + caravan.getFaction().getName();
        if (beneficiaryName.length() > 30) {
            return beneficiaryName.substring(0, 27) + "...";
        } else if (beneficiaryName.length() + factionName.length() + 3 > 30) {
            if (factionName.length() < 3) {
                return beneficiaryName;
            } else {
                final String trimmedFactionName = factionName.substring(0, 30 - beneficiaryName.length() - 6) + "...";
                return beneficiaryName + " (" + trimmedFactionName + "§f)";
            }
        } else {
            return beneficiaryName + " (" + factionName + "§f)";
        }
    }

    private static void cancelScrboardUpdateTask(Caravan caravan) {
        final Integer entityTaskId = KEY_TASKS.get(caravan.getKey());
        cancelTask(entityTaskId);
    }

    private static void cancelTask(Integer taskId) {
        if (taskId != null) {
            final BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.cancelTask(taskId.intValue());
        }
    }

    private static void unregisterObjective(Scoreboard scoreboard) {
        scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
    }

    private static Scoreboard scoreboardWithRelation(Caravan caravan, Rel relation) {
        final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        final String objectiveKey = getKey(caravan);

        final Objective objective = scoreboard.registerNewObjective(objectiveKey, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(getName(caravan, relation));

        final Score scoreValue = objective.getScore("§a" + Configuration.CONF.currency.namePlural + "§f");
        scoreValue.setScore((int) caravan.getInvestment());

        return scoreboard;
    }

    private static String getKey(Caravan caravan) {
        return caravan.getKey().substring(0, 15);
    }

}
