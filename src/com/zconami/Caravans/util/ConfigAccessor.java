package com.zconami.Caravans.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigAccessor {

	// ===================================
	// CONSTANTS
	// ===================================

	private static final int RELOAD_INTERVAL_MILLIS = 5000;

	// ===================================
	// ATTRIBUTES
	// ===================================

	private final String filename;
	private final JavaPlugin plugin;

	private File configFile;
	private FileConfiguration fileConfiguration;

	private long lastLoad;

	// ===================================
	// CONSTRUCTORS
	// ===================================

	public ConfigAccessor(JavaPlugin plugin, String filename) {
		if (plugin == null)
			throw new IllegalArgumentException("plugin cannot be null");
		this.plugin = plugin;
		this.filename = filename;
		File dataFolder = plugin.getDataFolder();
		if (dataFolder == null)
			throw new IllegalStateException();
		this.configFile = new File(plugin.getDataFolder(), filename);
	}

	// ===================================
	// PUBLIC METHODS
	// ===================================

	public synchronized void reloadConfig() {
		fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

		// Look for defaults in the jar
		InputStream defConfigStream = plugin.getResource(filename);
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
			fileConfiguration.setDefaults(defConfig);
		}
		lastLoad = System.currentTimeMillis();
	}

	public FileConfiguration getConfig() {
		if (fileConfiguration == null) {
			this.reloadConfig();
		}
		final long current = System.currentTimeMillis();
		if (current < lastLoad + RELOAD_INTERVAL_MILLIS) {
			new Runnable() {
				@Override
				public void run() {
					ConfigAccessor.this.reloadConfig();
				}
			}.run();
		}
		return fileConfiguration;
	}

	public void saveConfig() {
		if (fileConfiguration != null && configFile != null) {
			try {
				plugin.getLogger().info("Saving config:" + configFile);
				getConfig().save(configFile);
				plugin.getLogger().info("Config saved.");
			} catch (IOException ex) {
				plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
			}
		}
	}

	public void saveDefaultConfig() {
		if (!configFile.exists()) {
			this.plugin.saveResource(filename, false);
		}
	}

}