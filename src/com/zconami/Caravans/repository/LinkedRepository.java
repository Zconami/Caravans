package com.zconami.Caravans.repository;

import com.zconami.Caravans.domain.LinkedEntity;
import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.util.EntityUtils;

public abstract class LinkedRepository<BE extends org.bukkit.entity.Entity, ME extends net.minecraft.server.v1_8_R3.Entity, E extends LinkedEntity<BE, ME>>
        extends Repository<E> {

    // ===================================
    // CONSTRUCTORS
    // ===================================

    protected LinkedRepository() {
        super();
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public E find(BE bukkitEntity) {
        return super.find(bukkitEntity.getUniqueId().toString());
    }

    public E recreate(DataKey entityData) {
        final String key = entityData.getPath();
        final BE bukkitEntity = EntityUtils.findBy(key, getBukkitEntityType());
        return recreate(bukkitEntity, entityData);
    }

    // ===================================
    // ABSTRACT METHODS
    // ===================================

    protected abstract Class<BE> getBukkitEntityType();

    protected abstract E recreate(BE bukkitEntity, DataKey entityData);

}
