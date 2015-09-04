package com.zconami.Caravans.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.zconami.Caravans.util.EntityUtils;
import com.zconami.Caravans.util.NMSUtils;

@MappedSuperclass
public abstract class LinkedEntity<BE extends org.bukkit.entity.Entity, ME extends net.minecraft.server.v1_8_R3.Entity>
        extends BaseEntity {

    // ===================================
    // ATTRIBUTES
    // ===================================

    public static String BUKKIT_ENTITY_ID = "bukkitEntityId";
    @Column(nullable = false, unique = true)
    private UUID bukkitEntityId;

    @Transient
    private BE bukkitEntity;

    @Transient
    private ME minecraftEntity;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    @SuppressWarnings("unchecked")
    public LinkedEntity() {
        if (bukkitEntityId != null) {
            transientEntitiesFromId();
        }
    }

    public LinkedEntity(LinkedEntityCreateParameters<BE, ME> params) {
        super(params);
        apply(params);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public UUID getBukkitEntityId() {
        return bukkitEntityId;
    }

    public void setBukkitEntityId(UUID bukkitEntityId) {
        this.bukkitEntityId = bukkitEntityId;
        transientEntitiesFromId();
    }

    public BE getBukkitEntity() {
        return bukkitEntity;
    }

    public ME getMinecraftEntity() {
        return minecraftEntity;
    }

    public void setTransientFromBukkitEntity() {
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    @SuppressWarnings("unchecked")
    private void apply(LinkedEntityCreateParameters<BE, ME> params) {
        this.bukkitEntity = params.getBukkitEntity();
        this.bukkitEntityId = this.bukkitEntity.getUniqueId();
        final ME pMinecraftEntity = params.getMinecraftEntity();
        this.minecraftEntity = pMinecraftEntity == null ? (ME) NMSUtils.getMinecraftEntity(bukkitEntity)
                : pMinecraftEntity;
    }

    private void transientEntitiesFromId() {
        this.bukkitEntity = (BE) EntityUtils.findBy(bukkitEntityId.toString());
        this.minecraftEntity = (ME) NMSUtils.getMinecraftEntity(bukkitEntity);
        setTransientFromBukkitEntity();
    }

    // ===================================
    // ABSTRACT METHODS
    // ===================================

    public abstract Class<BE> getBukkitEntityType();

}
