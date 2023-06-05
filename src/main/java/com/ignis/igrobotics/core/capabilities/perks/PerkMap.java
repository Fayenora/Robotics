package com.ignis.igrobotics.core.capabilities.perks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ignis.igrobotics.core.util.Tuple;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Iterator;

public class PerkMap implements IPerkMap {
	
	HashMap<String, Perk> perks = new HashMap<>();
	HashMap<String, Integer> levels = new HashMap<>();
	
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
	public void clear() {
		perks.clear();
		levels.clear();
	}

	@Override
	public boolean contains(Perk perk) {
		return perks.containsValue(perk);
	}

	@Override
	public Iterator<Tuple<Perk, Integer>> iterator() {
		return new Iterator<>() {
			final Iterator<Perk> perkIt = perks.values().iterator();
			final Iterator<Integer> levelIt = levels.values().iterator();

			@Override
			public boolean hasNext() {
				return perkIt.hasNext();
			}

			@Override
			public Tuple<Perk, Integer> next() {
				Perk perk = perkIt.next();
				int effectiveLevel = Math.min(perk.getMaxLevel(), levelIt.next());
				return new Tuple<>(perk, effectiveLevel);
			}
		};
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

	public static void write(FriendlyByteBuf buffer, PerkMap perkMap) {
		buffer.writeShort(perkMap.perks.size());
		for(String perkName : perkMap.perks.keySet()) {
			Perk.write(buffer, perkMap.perks.get(perkName));
			buffer.writeInt(perkMap.levels.get(perkName));
		}
	}

	public static PerkMap read(FriendlyByteBuf buffer) {
		PerkMap perkMap = new PerkMap();
		int nPerks = buffer.readShort();
		for(int i = 0; i < nPerks; i++) {
			Perk perk = Perk.read(buffer);
			int level = buffer.readInt();
			perkMap.perks.put(perk.getUnlocalizedName(), perk);
			perkMap.levels.put(perk.getUnlocalizedName(), level);
		}
		return perkMap;
	}
}
