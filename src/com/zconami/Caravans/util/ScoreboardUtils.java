package com.zconami.Caravans.util;

import static com.zconami.Caravans.CaravansPlugin.getCaravansPlugin;
import static com.zconami.Caravans.util.GringottsUtils.getGringottsName;
import static com.zconami.Core.util.Utils.getScheduler;
import static com.zconami.Core.util.Utils.getScoreboardManager;

import java.util.Collection;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Maps;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.util.RelationUtil;
import com.zconami.Caravans.domain.Caravan;
import com.zconami.Core.util.Utils;

public class ScoreboardUtils {

    // ===================================
    // CONSTANTS
    // ===================================

    public static final int DISPLAY_NAME_MAX_LENGTH = 32;

    // Includes 1st letter of faction name: <playerName> ($xF...$f)
    private static final int DISPLAY_NAME_FACTION_FORMATTING_LENGTH_MIN = 11;

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final Map<String, Integer> KEY_TASKS = Maps.newHashMap();
    private static final Map<String, Map<Rel, Scoreboard>> KEY_REL_SCOREBOARD = Maps.newHashMap();

    private static final int UPDATE_INTERVAL_TICKS = Utils.ticksFromSeconds(5);

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

        final BukkitScheduler scheduler = getScheduler();
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

                        final Score estimatedReturn = objective.getScore("Estimated Return");
                        estimatedReturn.setScore((int) caravan.getReturn(location));
                    }
                }
            }
        }, 0L, UPDATE_INTERVAL_TICKS));
        KEY_TASKS.put(caravan.getKey(), taskId);

        KEY_REL_SCOREBOARD.put(caravan.getKey(), relScoreboard);
    }

    public static void stopScoreboard(Caravan caravan) {
        cancelScoreboardUpdateTask(caravan);

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

    public static Scoreboard showScoreboard(Player player, Caravan caravan) {
        final Rel relation = RelationUtil.getRelationOfThatToMe(caravan.getFaction(), MPlayer.get(player));

        if (!KEY_REL_SCOREBOARD.containsKey(caravan.getKey())) {
            // If scoreboard isn't setup (perhaps reload or restart), cleanup
            // and recreate
            cancelScoreboardUpdateTask(caravan);
            setUpScoreboardCaravanTask(caravan);
        }

        final Scoreboard scoreboard = KEY_REL_SCOREBOARD.get(caravan.getKey()).get(relation);
        player.setScoreboard(scoreboard);

        return scoreboard;
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private static String getName(Caravan caravan, Rel relation) {

        final String beneficiaryName = caravan.getBeneficiary().getName();
        final String factionName = caravan.getFaction().getName();

        if (DISPLAY_NAME_MAX_LENGTH - DISPLAY_NAME_FACTION_FORMATTING_LENGTH_MIN < beneficiaryName.length()) {
            if (beneficiaryName.length() > DISPLAY_NAME_MAX_LENGTH) {
                return beneficiaryName.substring(0, DISPLAY_NAME_MAX_LENGTH - 3) + "...";
            } else {
                return beneficiaryName;
            }
        }

        final boolean factionNeedsTrimmed = beneficiaryName.length() + (DISPLAY_NAME_FACTION_FORMATTING_LENGTH_MIN - 4)
                + factionName.length() >= DISPLAY_NAME_MAX_LENGTH;
        if (factionNeedsTrimmed) {
            final int spaceRemaining = DISPLAY_NAME_MAX_LENGTH - DISPLAY_NAME_FACTION_FORMATTING_LENGTH_MIN
                    - beneficiaryName.length() + 1;
            final String trimmedFactionName = factionName.substring(0, spaceRemaining);
            return beneficiaryName + " (" + relation.getColor() + trimmedFactionName + "...§f)";
        } else {
            return beneficiaryName + " (" + relation.getColor() + factionName + "§f)";
        }

    }

    private static void cancelScoreboardUpdateTask(Caravan caravan) {
        final Integer entityTaskId = KEY_TASKS.get(caravan.getKey());
        cancelTask(entityTaskId);
    }

    private static void cancelTask(Integer taskId) {
        if (taskId != null) {
            final BukkitScheduler scheduler = getScheduler();
            scheduler.cancelTask(taskId.intValue());
        }
    }

    private static void unregisterObjective(Scoreboard scoreboard) {
        scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
    }

    private static Scoreboard scoreboardWithRelation(Caravan caravan, Rel relation) {
        final Scoreboard scoreboard = getScoreboardManager().getNewScoreboard();

        final String objectiveKey = getKey(caravan);

        final Objective objective = scoreboard.registerNewObjective(objectiveKey, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(getName(caravan, relation));

        final Score scoreValue = objective
                .getScore(caravan.getOrigin().getTypeOfGood() + " §a" + getGringottsName() + "§f Value");
        scoreValue.setScore((int) caravan.getInvestment());

        return scoreboard;
    }

    private static String getKey(Caravan caravan) {
        return caravan.getKey().substring(0, 15);
    }

}
