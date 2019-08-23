package com.songoda.kingdoms.api;

import org.apache.commons.lang.Validate;

import com.songoda.kingdoms.Kingdoms;

public class KingdomsAPI {

	private final static Invasions invasions = new Invasions();
	private static Kingdoms instance;

	public static void setInstance(Kingdoms instance) {
		Validate.isTrue(KingdomsAPI.instance == null, "The API instance can not be set twice!");
		KingdomsAPI.instance = instance;
	}

	/**
	 * Grab the Kingdoms Invasions API.
	 * 
	 * @return Invasions.
	 */
	public static Invasions getInvasions() {
		return invasions;
	}

}
