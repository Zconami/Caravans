package com.zconami.Caravans.util;

import static com.zconami.Caravans.util.Utils.getCaravansPlugin;
import static com.zconami.Caravans.util.Utils.getScheduler;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Horse;
import org.bukkit.scheduler.BukkitScheduler;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.PolyLineMarker;

import com.google.common.collect.Maps;
import com.zconami.Caravans.domain.Caravan;

public class DynmapUtils {

    // ===================================
    // CONSTANTS
    // ===================================

    private static final String CARAVAN_MARKER_ICON = "truck";

    // ===================================
    // ATTRIBUTES
    // ===================================

    private static final Map<String, Integer> KEY_TASKS = Maps.newHashMap();
    private static final Map<String, Pair<Marker, PolyLineMarker>> KEY_MARKERS = Maps.newHashMap();

    private static final int UPDATE_INTERVAL_TICKS = Utils.ticksFromSeconds(5);

    // ===================================
    // CONSTRUCTORS
    // ===================================

    private DynmapUtils() {
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public static boolean isCaravanTaskRunning(Caravan caravan) {
        return KEY_TASKS.containsKey(caravan.getKey());
    }

    public static void setupDynmapCaravanTask(Caravan caravan) {

        final BukkitScheduler scheduler = getScheduler();
        final Integer taskId = Integer.valueOf(scheduler.scheduleSyncRepeatingTask(getCaravansPlugin(), new Runnable() {
            @Override
            public void run() {
                final Horse target = caravan.getBukkitEntity();
                if (target.isValid()) {
                    createOrUpdateMarker(caravan);
                }
            }
        }, 0L, UPDATE_INTERVAL_TICKS));
        KEY_TASKS.put(caravan.getKey(), taskId);

    }

    public static void stopCaravanDynmap(Caravan caravan) {
        final Integer taskId = KEY_TASKS.get(caravan.getKey());
        cancelTask(taskId);
        KEY_TASKS.remove(caravan.getKey());
        removeMarker(caravan);
        KEY_MARKERS.remove(caravan.getKey());
    }

    public static void stopAllCaravanDynmaps() {
        KEY_TASKS.values().forEach(DynmapUtils::cancelTask);
        final Collection<Pair<Marker, PolyLineMarker>> markerPairs = KEY_MARKERS.values();
        markerPairs.forEach(markerPair -> {
            markerPair.getLeft().deleteMarker();
            markerPair.getRight().deleteMarker();
        });
        KEY_MARKERS.clear();
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private static void cancelTask(final Integer taskId) {
        if (taskId != null) {
            final BukkitScheduler scheduler = getScheduler();
            scheduler.cancelTask(taskId.intValue());
        }
    }

    private static String getName(Caravan caravan) {
        final String beneficiaryName = caravan.getBeneficiary().getName();
        final String factionName = caravan.getFaction().getName();

        return beneficiaryName + " (" + factionName + ")";
    }

    private static String getMarkerId(Caravan caravan) {
        return caravan.getKey().replace("-", "");
    }

    private static void createOrUpdateMarker(Caravan caravan) {
        final Horse target = caravan.getBukkitEntity();
        if (target.isValid()) {
            final Location location = target.getLocation();

            final Marker marker = getMarker(caravan);
            marker.setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
            marker.setDescription("<strong>Investment:</strong> " + caravan.getInvestment()
                    + "</br><strong>Estimated return:</strong> " + caravan.getReturn(location)
                    + "</br><strong>Type</strong>: " + caravan.getProfitStrategy().name());

            final PolyLineMarker polyLineMarker = getPolyLineMarker(caravan);
            polyLineMarker.setCornerLocation(1, location.getX(), location.getY(), location.getZ());

            KEY_MARKERS.put(caravan.getKey(), Pair.of(marker, polyLineMarker));
        }
    }

    private static void removeMarker(Caravan caravan) {
        final MarkerSet markerSet = getMarkerSet(caravan);

        final Marker foundMarker = markerSet.findMarker(getMarkerId(caravan));
        if (foundMarker != null) {
            foundMarker.deleteMarker();
        }
    }

    private static MarkerIcon getMarkerIcon() {
        final DynmapAPI dynmap = getDynmap();
        final MarkerAPI markerAPI = dynmap.getMarkerAPI();
        return markerAPI.getMarkerIcon(CARAVAN_MARKER_ICON);
    }

    private static Marker getMarker(Caravan caravan) {
        final Horse target = caravan.getBukkitEntity();
        final Location location = target.getLocation();

        final MarkerSet markerSet = getMarkerSet(caravan);
        final Marker foundMarker = markerSet.findMarker(getMarkerId(caravan));
        if (foundMarker != null) {
            return foundMarker;
        }
        return markerSet.createMarker(getMarkerId(caravan), getName(caravan), true, location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(), getMarkerIcon(), false);

    }

    private static PolyLineMarker getPolyLineMarker(Caravan caravan) {
        final Horse target = caravan.getBukkitEntity();
        final Location current = target.getLocation();
        final Location origin = caravan.getOrigin().getCenter();

        final MarkerSet markerSet = getMarkerSet(caravan);
        final PolyLineMarker foundMarker = markerSet.findPolyLineMarker(getMarkerId(caravan));
        if (foundMarker != null) {
            return foundMarker;
        }
        final double[] xs = new double[] {
                origin.getBlockX(),
                current.getBlockX()
        };
        final double[] ys = new double[] {
                origin.getBlockY(),
                current.getBlockY()
        };
        final double[] zs = new double[] {
                origin.getBlockZ(),
                current.getBlockZ()
        };
        return markerSet.createPolyLineMarker(getMarkerId(caravan), "", false, current.getWorld().getName(), xs, ys, zs,
                false);
    }

    private static MarkerSet getMarkerSet(Caravan caravan) {
        final DynmapAPI dynmap = getDynmap();
        final MarkerAPI markerAPI = dynmap.getMarkerAPI();
        final String setId = getMarkerId(caravan) + "set";
        final MarkerSet existingMarketSet = markerAPI.getMarkerSet(setId);
        if (existingMarketSet != null) {
            return existingMarketSet;
        } else {
            return markerAPI.createMarkerSet(setId, getName(caravan), null, false);
        }
    }

    private static DynmapAPI getDynmap() {
        return (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
    }

}
