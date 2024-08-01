package com.ignis.igrobotics.core.capabilities.perks;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.definitions.ModPerks;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

public class PerkMap implements IPerkMap {

	public static final Codec<PerkMap> CODEC = Codec.list(Codec.pair(
			ResourceLocation.CODEC.fieldOf("name").codec(),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("level", 1).codec()
	)).xmap(PerkMap::new, PerkMap::toCodecFormat);

	HashMap<ResourceLocation, Integer> levels = new HashMap<>();
	private final SimpleDataManager values = new SimpleDataManager();

	public PerkMap() {}

	public PerkMap(List<Pair<ResourceLocation, Integer>> perks) {
		for(Pair<ResourceLocation, Integer> perk : perks) {
			add(perk.getFirst(), perk.getSecond());
		}
	}

	public static List<Pair<ResourceLocation, Integer>> toCodecFormat(IPerkMap perkMap) {
		return StreamSupport.stream(perkMap.spliterator(), false).map(Tuple::toPair).map(p -> p.mapFirst(Perk::getKey)).toList();
	}
	
	@Override
	public void add(Perk perk, int level) {
		add(perk.getKey(), level);
	}

	private void add(ResourceLocation perkKey, int level) {
		int currLevel = levels.getOrDefault(perkKey, 0);
		levels.put(perkKey, currLevel + level);
	}
	
	@Override
	public void remove(Perk perk, int level) {
		int currLevel = levels.getOrDefault(perk.getKey(), 0);
		if(currLevel - level <= 0) {
			levels.remove(perk.getKey());
		} else {
			levels.put(perk.getKey(), currLevel - level);
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
		levels.clear();
	}

	@Override
	public boolean contains(Perk perk) {
		return levels.containsKey(perk.getKey());
	}

	@Override
	public int getLevel(Perk perk) {
		return levels.getOrDefault(perk.getKey(), 0);
	}

	@Override
	public SimpleDataManager values() {
		return values;
	}

	@Override
	public Iterator<Tuple<Perk, Integer>> iterator() {
		return new Iterator<>() {
			final Iterator<ResourceLocation> perkIt = levels.keySet().iterator();

			@Override
			public boolean hasNext() {
				return perkIt.hasNext();
			}

			@Override
			public Tuple<Perk, Integer> next() {
				ResourceLocation perkKey = perkIt.next();
				Perk perk = Robotics.proxy.getRegistryAccess().registryOrThrow(ModPerks.KEY).get(perkKey);
				if(perk == null) {
					Robotics.LOGGER.warn("Perk " + perkKey + " does not exist!");
					return new Tuple<>(ModPerks.PERK_UNDEFINED.get(), 1);
				}
				int effectiveLevel = Math.min(perk.getMaxLevel(), levels.get(perkKey));
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
