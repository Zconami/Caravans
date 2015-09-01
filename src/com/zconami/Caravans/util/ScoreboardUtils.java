package com.zconami.Caravans.util;

import static com.zconami.Caravans.util.Utils.getJavaPlugin;

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
import com.zconami.Caravans.domain.Caravan;

public class ScoreboardUtils {

	// ===================================
	// ATTRIBUTES
	// ===================================

	private static final Map<String, Integer> KEY_TASKS = Maps.newHashMap();
	private static final Map<String, Scoreboard> KEY_SCOREBOARD = Maps.newHashMap();

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
		final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		final String objectiveKey = getKey(caravan);

		final Objective objective = scoreboard.registerNewObjective(objectiveKey, "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(getName(caravan));

		final Score scoreX = objective.getScore("§6X§f");
		final Score scoreZ = objective.getScore("§6Z§f");
		final Score scoreValue = objective.getScore("§a" + Configuration.CONF.currency.namePlural + "§f");
		scoreValue.setScore((int) caravan.getInvestment());

		final Horse target = caravan.getBukkitEntity();
		final BukkitScheduler scheduler = Bukkit.getScheduler();
		final Integer taskId = Integer.valueOf(scheduler.scheduleSyncRepeatingTask(getJavaPlugin(), new Runnable() {
			@Override
			public void run() {
				if (target.isValid()) {
					final Location location = target.getLocation();
					scoreX.setScore(location.getBlockX());
					scoreZ.setScore(location.getBlockZ());
				}
			}
		}, 0L, UPDATE_INTERVAL_TICKS));
		KEY_TASKS.put(caravan.getKey(), taskId);

		KEY_SCOREBOARD.put(caravan.getKey(), scoreboard);
	}

	public static void stopScoreboard(Caravan caravan) {
		final Integer entityTaskId = KEY_TASKS.get(caravan.getKey());
		if (entityTaskId != null) {
			final BukkitScheduler scheduler = Bukkit.getScheduler();
			scheduler.cancelTask(entityTaskId.intValue());
		}
		final Scoreboard scoreboard = KEY_SCOREBOARD.get(caravan.getKey());
		scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
		KEY_TASKS.remove(caravan.getKey());
		KEY_SCOREBOARD.remove(caravan.getKey());
	}

	public static void showScoreboard(Player player, Caravan caravan) {
		final Scoreboard scoreboard = KEY_SCOREBOARD.get(caravan.getKey());
		player.setScoreboard(scoreboard);
	}

	private static String getName(Caravan caravan) {
		final String beneficiaryName = caravan.getBeneficiary().getBukkitEntity().getName();
		final String factionName = caravan.getFaction().getName();
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

	// ===================================
	// PRIVATE METHODS
	// ===================================

	private static String getKey(Caravan caravan) {
		return caravan.getKey().substring(0, 15);
	}

}
