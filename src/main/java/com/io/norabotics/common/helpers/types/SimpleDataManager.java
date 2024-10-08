package com.io.norabotics.common.helpers.types;

import java.util.HashMap;

/**
 * A Hash Map with a little less access
 */
public class SimpleDataManager {
	
	private final HashMap<String, Integer> values;
	
	public SimpleDataManager() {
		values = new HashMap<>();
	}
	
	public void set(String name, int value) {
		values.put(name, value);
	}
	
	public void increment(String name) {
		int val = get(name);
		values.put(name, val + 1);
	}
	
	public void decrement(String name) {
		int val = get(name);
		values.put(name, val - 1);
	}
	
	public int get(String name) {
		if(!values.containsKey(name)) return 0;
		return values.get(name);
	}

	public boolean contains(String name) {
		return values.containsKey(name);
	}

}
