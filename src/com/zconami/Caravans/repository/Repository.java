package com.zconami.Caravans.repository;

import static com.zconami.Caravans.util.Utils.getLogger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    protected E save(E entity) {
        final String key = entity.getKey();
        final DataKey entityData = root.getRelative(key);
        entity.writeData(entityData);

        storage.save();

        loaded.put(key, entity);
        entity.setDirty(false);
        entity.addObserver(this);
        return entity;
    }

    // ===================================
    // PUBLIC METHODS
    // ===================================

    public void remove(E entity) {
        entity.willRemove();

        final String key = entity.getKey();
        root.removeKey(key);

        loaded.remove(key, entity);
        removeLookups(entity);

        storage.save();
        EntityUtils.removeFromCache(key);
    }

    public E find(String key) {

        final E alreadyLoaded = loaded.get(key);
        if (alreadyLoaded != null) {
            return alreadyLoaded;
        }

        this.storage.load();

        if (root.keyExists(key)) {
            getLogger().info("Key exists, loading from storage");
            // Not in cache, but exists in storage, just hasn't been loaded yet
            final DataKey entityData = root.getRelative(key);

            final E recreated = recreate(entityData);
            createLookups(recreated);
            loaded.put(key, recreated);
            return recreated;
        }
        getLogger().info("Key does not exist");
        return null;
    }

    public void saveAll() {
        for (E dirtyEntity : loaded.values().stream().filter(entity -> entity.isDirty()).collect(Collectors.toList())) {
            save(dirtyEntity);
        }
    }

    public List<E> all() {
        this.storage.load();
        final List<E> entities = Lists.newArrayList();
        root.getSubKeys().forEach(key -> entities.add(find(key.getPath())));
        return entities;
    }

    @Override
    public void entityChanged(E entity) {
        save(entity);
    }

    // ===================================
    // ABSTRACT METHODS
    // ===================================

    protected abstract E recreate(DataKey entityData);

    protected abstract String getEntityName();

    protected abstract void createLookups(E entity);

    protected abstract void removeLookups(E entity);

}
