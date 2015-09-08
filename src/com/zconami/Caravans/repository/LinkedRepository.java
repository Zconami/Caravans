package com.zconami.Caravans.repository;

import static com.zconami.Caravans.util.Utils.getLogger;

import org.bukkit.Chunk;

import com.zconami.Caravans.CaravansPlugin;
import com.zconami.Caravans.domain.LinkedEntity;
import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.util.EntityUtils;

public abstract class LinkedRepository<BE extends org.bukkit.entity.Entity, ME extends net.minecraft.server.v1_8_R3.Entity, E extends LinkedEntity<BE, ME>>
        extends Repository<E> {

    // ===================================
    // CONSTRUCTORS
    // ===================================

    protected LinkedRepository(CaravansPlugin plugin) {
        super(plugin);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public E find(BE bukkitEntity) {
        return super.find(bukkitEntity.getUniqueId().toString());
    }

    public E recreate(DataKey entityData) {
        final String key = entityData.getPath();
        final Chunk chunkFromData = LinkedEntity.getChunkFromData(entityData);
        if (!chunkFromData.isLoaded()) {
            chunkFromData.load(true);
        }
        final BE bukkitEntity = EntityUtils.findBy(key, getBukkitEntityType());
        if (bukkitEntity == null) {
            getLogger().info("Failed to find bukkitEntity for [" + getEntityName() + ":" + key + "], chunk "
                    + (chunkFromData.isLoaded() ? "is (world probably isn't)" : "is NOT")
                    + " loaded. Can't recreate entity as a result!");
            return null;
        }
        return recreate(bukkitEntity, entityData);
    }

    // ===================================
    // ABSTRACT METHODS
    // ===================================

    protected abstract Class<BE> getBukkitEntityType();

    protected abstract E recreate(BE bukkitEntity, DataKey entityData);

}
