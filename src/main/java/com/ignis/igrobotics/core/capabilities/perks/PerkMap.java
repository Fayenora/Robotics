package com.ignis.igrobotics.core.capabilities.perks;

import com.google.gson.JsonElement;
import com.ignis.igrobotics.core.util.Tuple;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

public class PerkMap implements IPerkMap {

	public static final Codec<PerkMap> CODEC = Codec.list(Codec.pair(
			Perk.CODEC.fieldOf("name").codec(),
			Codec.INT.optionalFieldOf("level", 0).codec()
	)).xmap(PerkMap::new, map -> StreamSupport.stream(map.spliterator(), false).map(Tuple::toPair).toList());
	
	HashMap<String, Perk> perks = new HashMap<>();
	HashMap<String, Integer> levels = new HashMap<>();

	public PerkMap() {}

	private PerkMap(List<Pair<Perk, Integer>> perks) {
		for(Pair<Perk, Integer> p : perks) {
			add(p.getFirst(), p.getSecond());
		}
	}
	
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
	public int getLevel(Perk perk) {
		return levels.getOrDefault(perk.getUnlocalizedName(), 0);
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

	public static JsonElement serialize(PerkMap perkMap) {
		return CODEC.encodeStart(JsonOps.INSTANCE, perkMap).getOrThrow(false, s -> {
			throw new RuntimeException(s);
		});
	}

	public static IPerkMap deserialize(JsonElement json) {
		return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {
			throw new RuntimeException(s);
		});
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

	public static PerkMap copy(IPerkMap perkMap) {
		PerkMap clone = new PerkMap();
		for(Tuple<Perk, Integer> entry : perkMap) {
			clone.add(entry.first, entry.second);
		}
		return clone;
	}
}
