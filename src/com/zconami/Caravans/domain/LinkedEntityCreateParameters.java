package com.zconami.Caravans.domain;

import com.zconami.Caravans.util.NMSUtils;

public abstract class LinkedEntityCreateParameters<BE extends org.bukkit.entity.Entity, ME extends net.minecraft.server.v1_8_R3.Entity>
        extends EntityCreateParameters {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final BE bukkitEntity;
    private final ME minecraftEntity;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    @SuppressWarnings("unchecked")
    public LinkedEntityCreateParameters(BE bukkitEntity) {
        this(bukkitEntity, (ME) NMSUtils.getMinecraftEntity(bukkitEntity));
    }

    public LinkedEntityCreateParameters(BE bukkitEntity, ME minecraftEntity) {
        super(bukkitEntity.getUniqueId().toString());
        this.bukkitEntity = bukkitEntity;
        this.minecraftEntity = minecraftEntity;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public BE getBukkitEntity() {
        return bukkitEntity;
    }

    public ME getMinecraftEntity() {
        return minecraftEntity;
    }

}
