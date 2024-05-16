package com.ignis.igrobotics.core.capabilities.perks;

import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.definitions.ModModules;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

public class PerkMap implements IPerkMap {

	public static final Codec<PerkMap> LOADING_CODEC = Codec.list(Codec.pair(
			ResourceLocation.CODEC.fieldOf("name").codec(),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("level", 1).codec()
	)).xmap(PerkMap::init, PerkMap::toCodecFormat);
	
	HashMap<String, Perk> perks = new HashMap<>();
	HashMap<String, Integer> levels = new HashMap<>();

	public PerkMap() {}

	private static PerkMap init(List<Pair<ResourceLocation, Integer>> perks) {
		PerkMap map = new PerkMap();
		ModModules.queueInit(map, perks);
		return map;
	}

	public static List<Pair<ResourceLocation, Integer>> toCodecFormat(IPerkMap perkMap) {
		return StreamSupport.stream(perkMap.spliterator(), false).map(Tuple::toPair).map(p -> p.mapFirst(Perk::getKey)).toList();
	}
	
	@Override
	public void add(Perk perk, int level) {
		int currLevel = levels.getOrDefault(perk.getId(), 0);
		perks.put(perk.getId(), perk);
		levels.put(perk.getId(), currLevel + level);
	}
	
	@Override
	public void remove(Perk perk, int level) {
		int currLevel = levels.getOrDefault(perk.getId(), 0);
		if(currLevel - level <= 0) {
			perks.remove(perk.getId());
			levels.remove(perk.getId());
		} else {
			levels.put(perk.getId(), currLevel - level);
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
		return levels.getOrDefault(perk.getId(), 0);
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

	public static PerkMap copy(IPerkMap perkMap) {
		PerkMap clone = new PerkMap();
		for(Tuple<Perk, Integer> entry : perkMap) {
			clone.add(entry.first, entry.second);
		}
		return clone;
	}
}
