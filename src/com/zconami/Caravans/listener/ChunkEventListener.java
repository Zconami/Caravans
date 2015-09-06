package com.zconami.Caravans.listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.zconami.Caravans.util.CaravansUtils;

public class ChunkEventListener implements Listener {

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public ChunkEventListener() {
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            if (CaravansUtils.isCaravan(entity)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
    }

}
