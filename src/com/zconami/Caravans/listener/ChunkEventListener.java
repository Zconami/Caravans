package com.zconami.Caravans.listener;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.zconami.Caravans.domain.Caravan;
import com.zconami.Caravans.repository.CaravanRepository;
import com.zconami.Caravans.util.CaravansUtils;

public class ChunkEventListener implements Listener {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final CaravanRepository caravanRepository;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public ChunkEventListener(CaravanRepository caravanRepository) {
        this.caravanRepository = caravanRepository;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();
        for (Entity entity : chunk.getEntities()) {
            if (CaravansUtils.isCaravan(entity)) {
                final Caravan caravan = caravanRepository.find((Horse) entity);
                caravan.setChunk(chunk);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
    }

}
