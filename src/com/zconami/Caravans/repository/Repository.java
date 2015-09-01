package com.zconami.Caravans.repository;

import static com.zconami.Caravans.util.Utils.getJavaPlugin;
import static com.zconami.Caravans.util.Utils.getLogger;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zconami.Caravans.domain.Entity;
import com.zconami.Caravans.storage.DataKey;
import com.zconami.Caravans.storage.Storage;
import com.zconami.Caravans.storage.YamlStorage;
import com.zconami.Caravans.util.EntityUtils;

public abstract class Repository<E extends Entity> {

	// ===================================
	// ATTRIBUTES
	// ===================================

	private final Storage storage;
	private final DataKey root;

	private final Map<String, E> cache = Maps.newHashMap();

	// ===================================
	// CONSTRUCTORS
	// ===================================

	protected Repository() {
		final File dataFolder = getJavaPlugin().getDataFolder();
		this.storage = new YamlStorage(new File(dataFolder, getEntityName() + ".yml"));
		this.storage.load();
		this.root = storage.getKey("");
	}

	// ===================================
	// PROTECTED METHODS
	// ===================================

	protected E save(E entity) {
		// Persist extra data
		final String key = entity.getKey();
		final DataKey entityData = root.getRelative(key);
		entity.writeData(entityData);

		cache.put(key, entity);
		storage.save();

		return entity;
	}

	// ===================================
	// PUBLIC METHODS
	// ===================================

	public void remove(E entity) {
		final String key = entity.getKey();
		cache.remove(key);
		root.removeKey(key);
		removeLookups(entity);

		storage.save();
		EntityUtils.removeFromCache(key);
	}

	public E find(String key) {
		final E cached = cache.get(key);
		if (cached != null) {
			return cached;
		}

		this.storage.load();

		if (root.keyExists(key)) {
			getLogger().info("Key exists, loading from storage");
			// Not in cache, but exists in storage, just hasn't been loaded yet
			final DataKey entityData = root.getRelative(key);

			final E recreated = recreate(entityData);
			cache.put(key, recreated);
			createLookups(recreated);
			return recreated;
		}
		getLogger().info("Key does not exist");
		return null;
	}

	public List<E> all() {
		this.storage.load();
		final List<E> entities = Lists.newArrayList();
		root.getSubKeys().forEach(key -> entities.add(find(key.getPath())));
		return entities;
	}

	// ===================================
	// ABSTRACT METHODS
	// ===================================

	public abstract void saveChanges(E entity);

	protected abstract E recreate(DataKey entityData);

	protected abstract String getEntityName();

	protected abstract void createLookups(E entity);

	protected abstract void removeLookups(E entity);

}
