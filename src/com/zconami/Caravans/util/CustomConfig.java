package com.zconami.Caravans.util;

public enum CustomConfig {
	DATA("data.yml");

	// ===================================
	// ATTRIBUTES
	// ===================================

	private final String filename;

	// ===================================
	// CONSTRUCTORS
	// ===================================

	CustomConfig(String filename) {
		this.filename = filename;
	}

	// ===================================
	// PUBLIC METHODS
	// ===================================

	public String getFilename() {
		return filename;
	}

}
