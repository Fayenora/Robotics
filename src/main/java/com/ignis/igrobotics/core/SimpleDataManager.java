package com.ignis.igrobotics.core;

import java.util.HashMap;

public class SimpleDataManager {
	
	private HashMap<String, Integer> values;
	
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

}
