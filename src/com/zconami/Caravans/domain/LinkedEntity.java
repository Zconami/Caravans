package com.zconami.Caravans.domain;

import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.util.NMSUtils;

public abstract class LinkedEntity<BE extends org.bukkit.entity.Entity, ME extends net.minecraft.server.v1_8_R3.Entity>
        extends Entity {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private BE bukkitEntity;

    private ME minecraftEntity;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public LinkedEntity(BE bukkitEntity, DataKey dataKey) {
        super(bukkitEntity.getUniqueId().toString(), dataKey);
        this.bukkitEntity = bukkitEntity;
    }

    public LinkedEntity(LinkedEntityCreateParameters<BE, ME> params) {
        super(params);
        apply(params);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public BE getBukkitEntity() {
        return bukkitEntity;
    }

    public ME getNMSEntity() {
        return minecraftEntity;
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    private void apply(LinkedEntityCreateParameters<BE, ME> params) {
        this.bukkitEntity = params.getBukkitEntity();
        final ME pMinecraftEntity = params.getMinecraftEntity();
        this.minecraftEntity = pMinecraftEntity == null ? (ME) NMSUtils.getHandle(bukkitEntity) : pMinecraftEntity;
    }

    // ===================================
    // ABSTRACT METHODS
    // ===================================

    public abstract Class<BE> getBukkitEntityType();

}
