package com.zconami.Caravans.domain;

import static com.zconami.Caravans.util.Utils.getLogger;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.util.EntityUtils;
import com.zconami.Caravans.util.NMSUtils;

public abstract class LinkedEntity<BE extends org.bukkit.entity.Entity, ME extends net.minecraft.server.v1_8_R3.Entity>
        extends Entity {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private BE bukkitEntity;

    private ME minecraftEntity;

    public static final String CHUNK = "chunk";
    public static final String CHUNK_WORLD = CHUNK + ".world";
    public static final String CHUNK_X = CHUNK + ".x";
    public static final String CHUNK_Z = CHUNK + ".z";
    private Chunk chunk;

    // ===================================
    // CONSTRUCTORS
    // ===================================

    public LinkedEntity(BE bukkitEntity, DataKey dataKey) {
        super(bukkitEntity.getUniqueId().toString(), dataKey);
        this.bukkitEntity = bukkitEntity;
        this.minecraftEntity = (ME) NMSUtils.getMinecraftEntity(bukkitEntity);
        this.chunk = bukkitEntity.getLocation().getChunk();
    }

    public LinkedEntity(LinkedEntityCreateParameters<BE, ME> params) {
        super(params);
        apply(params);
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public BE getBukkitEntity() {
        if (!bukkitEntity.isValid()) {
            final BE foundEntity = (BE) EntityUtils.findBy(getKey(), chunk);
            if (foundEntity != null) {
                this.bukkitEntity = foundEntity;
            } else {
                getLogger().info("Couldn't find entity! This will cause bugs.");
            }
        }
        return bukkitEntity;
    }

    public ME getNMSEntity() {
        return minecraftEntity;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        if (!this.chunk.equals(chunk)) {
            this.chunk = chunk;
            this.setDirty(true);
        }
    }

    public static Chunk getChunkFromData(DataKey dataKey) {
        final UUID worldUUID = UUID.fromString(dataKey.getString(CHUNK_WORLD));
        final int chunkX = dataKey.getInt(CHUNK_X);
        final int chunkZ = dataKey.getInt(CHUNK_Z);
        final World world = Bukkit.getWorld(worldUUID);
        if (world == null) {
            getLogger().info("Can't find chunk's world for persisted entity " + dataKey.getPath());
            return null;
        }
        return world.getChunkAt(chunkX, chunkZ);
    }

    // ===================================
    // PRIVATE METHODS
    // ===================================

    @SuppressWarnings("unchecked")
    private void apply(LinkedEntityCreateParameters<BE, ME> params) {
        this.bukkitEntity = params.getBukkitEntity();
        this.chunk = bukkitEntity.getLocation().getChunk();
        final ME pMinecraftEntity = params.getMinecraftEntity();
        this.minecraftEntity = pMinecraftEntity == null ? (ME) NMSUtils.getMinecraftEntity(bukkitEntity)
                : pMinecraftEntity;
    }

    // ===================================
    // IMPLEMENTATION OF Entity
    // ===================================

    @Override
    public void readData(DataKey dataKey) {
        this.chunk = getChunkFromData(dataKey);
    }

    @Override
    public void writeData(DataKey dataKey) {
        dataKey.setString(CHUNK_WORLD, chunk.getWorld().getUID().toString());
        dataKey.setInt(CHUNK_X, chunk.getX());
        dataKey.setInt(CHUNK_Z, chunk.getZ());
    }

    // ===================================
    // ABSTRACT METHODS
    // ===================================

    public abstract Class<BE> getBukkitEntityType();

}
