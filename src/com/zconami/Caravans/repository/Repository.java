package com.zconami.Caravans.repository;

import static com.zconami.Caravans.util.Utils.getLogger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zconami.Caravans.CaravansPlugin;
import com.zconami.Caravans.domain.Entity;
import com.zconami.Caravans.domain.EntityObserver;
import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.storage.Storage;
import com.zconami.Caravans.storage.YamlStorage;
import com.zconami.Caravans.util.EntityUtils;

public abstract class Repository<E extends Entity> implements EntityObserver<E> {

    // ===================================
    // ATTRIBUTES
    // ===================================

    private final Storage storage;
    private final DataKey root;

    private final Map<String, E> loaded = Maps.newHashMap();

    // ===================================
    // CONSTRUCTORS
    // ===================================

    protected Repository(CaravansPlugin plugin) {
        final File dataFolder = plugin.getDataFolder();
        this.storage = new YamlStorage(new File(dataFolder, getEntityName() + ".yml"));
        this.storage.load();
        this.root = storage.getKey("");
    }

    // ===================================
    // PROTECTED METHODS
    // ===================================

    public void save() {
        storage.save();
    }

    protected E save(E entity) {
        final String key = entity.getKey();
        final DataKey entityData = root.getRelative(key);
        entity.writeData(entityData);

        storage.save();

        loaded.put(key, entity);
        entity.setDirty(false);

        entityLoaded(entity);
        return entity;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public E find(String key) {

        E entity = loaded.get(key);
        if (entity == null) {

            this.storage.load();

            if (root.keyExists(key)) {
                getLogger().log(Level.FINE, "Key " + key + " exists, loading from storage");
                // Not in cache, but exists in storage, just hasn't been loaded
                // yet
                final DataKey entityData = root.getRelative(key);

                if (!shouldRecreate(entityData)) {
                    return null;
                }

                entity = recreate(entityData);
                if (entity == null) {
                    getLogger().log(Level.FINE, "Failed to load bukkit entity for " + key);
                    return null;
                }
                loaded.put(key, entity);
            }
            getLogger().log(Level.FINE, "Key " + key + " does not exist");
        }
        if (entity != null) {
            entityLoaded(entity);
        }
        return entity;
    }

    public List<E> all() {
        this.storage.load();
        final List<E> entities = Lists.newArrayList();
        root.getSubKeys().forEach(key -> {
            final E entity = find(key.getPath());
            if (entity != null) {
                entities.add(entity);
            }
        });
        return entities;
    }

    public void unload() {
        loaded.values().forEach(this::removeLookups);
        loaded.clear();
    }

    public void unload(E entity) {
        removeLookups(entity);
        loaded.remove(entity.getKey());
    }

    public DataKey getDataForKey(String key) {
        if (root.keyExists(key)) {
            return root.getRelative(key);
        }
        return null;
    }

    // ===================================
    // Private Methods
    // ===================================

    private void entityLoaded(E entity) {
        createLookups(entity);
        entity.addObserver(this);
    }

    // ===================================
    // IMPLEMENTATION OF EntityObserver
    // ===================================

    @Override
    public void entityChanged(E entity) {
        save(entity);
    }

    @Override
    public void entityRemoved(E entity) {
        final String key = entity.getKey();
        root.removeKey(key);

        loaded.remove(key, entity);
        removeLookups(entity);

        storage.save();
        EntityUtils.removeFromCache(key);
    }

    // ===================================
    // ABSTRACT METHODS
    // ===================================

    protected abstract boolean shouldRecreate(DataKey entityData);

    protected abstract E recreate(DataKey entityData);

    protected abstract String getEntityName();

    protected abstract void createLookups(E entity);

    protected abstract void removeLookups(E entity);

}
