package com.ignis.igrobotics.core.capabilities.perks;

import com.google.gson.*;
import com.ignis.igrobotics.core.util.Tuple;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Iterator;

public class PerkMap implements IPerkMap {
	
	HashMap<String, Perk> perks = new HashMap<String, Perk>();
	HashMap<String, Integer> levels = new HashMap<String, Integer>();
	
	@Override
	public void add(Perk perk, int level) {
		int currLevel = levels.getOrDefault(perk.getUnlocalizedName(), 0);
		perks.put(perk.getUnlocalizedName(), perk);
		levels.put(perk.getUnlocalizedName(), currLevel + level);
	}
	
	@Override
	public void remove(Perk perk, int level) {
		int currLevel = levels.getOrDefault(perk.getUnlocalizedName(), 0);
		if(currLevel - level <= 0) {
			perks.remove(perk.getUnlocalizedName());
			levels.remove(perk.getUnlocalizedName());
		} else {
			levels.put(perk.getUnlocalizedName(), currLevel - level);
		}
	}
	
	@Override
	public void merge(IPerkMap other) {
		for(Tuple<Perk, Integer> tup : other) {
			add(tup.getFirst(), tup.getSecond());
		}
	}
	
	@Override
	public void diff(IPerkMap toRemove) {
		for(Tuple<Perk, Integer> tup : toRemove) {
			remove(tup.getFirst(), tup.getSecond());
		}
	}
	
	@Override
	public boolean contains(Perk perk) {
		return perks.containsValue(perk);
	}

	@Override
	public Iterator<Tuple<Perk, Integer>> iterator() {
		Iterator<Tuple<Perk, Integer>> it = new Iterator<Tuple<Perk, Integer>>() {
			Iterator<Perk> perkIt = perks.values().iterator();
			Iterator<Integer> levelIt = levels.values().iterator();

			@Override
			public boolean hasNext() {
				return perkIt.hasNext();
			}

			@Override
			public Tuple<Perk, Integer> next() {
				Perk perk = perkIt.next();
				int effectiveLevel = Math.min(perk.getMaxLevel(), levelIt.next());
				return new Tuple<Perk, Integer>(perk, effectiveLevel);
			}
		};
		return it;
	}
	
	public static JsonElement serialize(IPerkMap src) {
		JsonArray arr = new JsonArray();
		for(Tuple<Perk, Integer> tup : src) {
			JsonObject jsonPerk = Perk.serialize(tup.getFirst()).getAsJsonObject();
			jsonPerk.addProperty("level", tup.getSecond());
			arr.add(jsonPerk);
		}
		return arr;
	}

	public static IPerkMap deserialize(JsonElement json) {
		PerkMap perkMap = new PerkMap();
		perkMap.deserializeNBT(json);
		return perkMap;
	}

	public void deserializeNBT(JsonElement json) {
		JsonArray arr = json.getAsJsonArray();
		
		for(JsonElement el : arr) {
			Perk perk = Perk.deserialize(el);
			int level = el.getAsJsonObject().get("level").getAsInt();
			this.add(perk, level);
		}
	}

	public JsonElement serializeNBT() {
		return serialize(this);
	}
}
