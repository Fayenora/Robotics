package com.io.norabotics.common.capabilities.impl.perk;

import com.io.norabotics.Robotics;
import com.io.norabotics.common.capabilities.IPerkMap;
import com.io.norabotics.common.helpers.types.SimpleDataManager;
import com.io.norabotics.common.helpers.types.Tuple;
import com.io.norabotics.definitions.robotics.ModPerks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;

/**
 * An {@link IPerkMap} implementation that can handle perks that stack as well as non-stacking perks
 * @author Ignis
 */
@MethodsReturnNonnullByDefault
public class AdvancedPerkMap implements IPerkMap {

	private final HashMap<ResourceLocation, int[]> levelCounts = new HashMap<>();
	HashMap<ResourceLocation, Integer> levels = new HashMap<>();
	private final SimpleDataManager values = new SimpleDataManager();

	@Override
	public void add(Perk perk, int level) {
		if(level == 0) return;
		int currLevel = levels.getOrDefault(perk.getKey(), 0);
		int updatedLevel = (perk.isStackable() ? Math.min(currLevel + level, perk.getMaxLevel()) : Math.max(currLevel, level));
		levels.put(perk.getKey(), updatedLevel);
		
		//If the perk is not stackable, we also need to keep track of how often which level was added
		if(!perk.isStackable()) {
			if(!levelCounts.containsKey(perk.getKey())) {
				levelCounts.put(perk.getKey(), new int[perk.getMaxLevel()]);
			}
			levelCounts.get(perk.getKey())[level - 1]++;
		}
	}

	@Override
	public void remove(Perk perk, int level) {
		if(level == 0) return;
		ResourceLocation key = perk.getKey();
		if(!levels.containsKey(key)) return;
		
		int updatedLevel;
		if(perk.isStackable()) {
			updatedLevel = levels.getOrDefault(perk.getKey(), 0) - level;
		} else {
			//If the perk is not stackable, removing is more complicated...
			int[] arr = levelCounts.get(key);
			arr[level - 1] = Math.max(arr[level - 1] - 1, 0);
			
			//Maybe the perk was added as an equal or lower level from another source?
			updatedLevel = level;
			while(updatedLevel > 0 && arr[updatedLevel - 1] == 0) {
				updatedLevel--;
			}
		}
		
		if(updatedLevel <= 0) {
			levelCounts.remove(key);
			levels.remove(key);
			return;
		}
		levels.put(key, updatedLevel);
	}

	@Override
	public void merge(IPerkMap other) {
		//Just... please don't merge advanced perk maps
		if(other instanceof AdvancedPerkMap otherMap) {
			for(Tuple<Perk, Integer> tup : other) {
				Perk perk = tup.getFirst();
				ResourceLocation key = perk.getKey();
				if(!tup.getFirst().isStackable()) {
					for(int i = 0; i < perk.getMaxLevel(); i++) {
						for(int j = 0; j < otherMap.levelCounts.get(key)[i]; j++) {
							add(perk, i);
						}
					}
				} else add(perk, tup.getSecond());
			}
			return;
		}
		for(Tuple<Perk, Integer> tup : other) {
			add(tup.getFirst(), tup.getSecond());
		}
	}

	@Override
	public void diff(IPerkMap toRemove) {
		if(toRemove instanceof AdvancedPerkMap otherMap) {
			for(Tuple<Perk, Integer> tup : toRemove) {
				Perk perk = tup.getFirst();
				ResourceLocation key = perk.getKey();
				if(!tup.getFirst().isStackable()) {
					for(int i = 0; i < perk.getMaxLevel(); i++) {
						for(int j = 0; j < otherMap.levelCounts.get(key)[i]; j++) {
							remove(perk, i);
						}
					}
				} else remove(perk, tup.getSecond());
			}
			return;
		}
		for(Tuple<Perk, Integer> tup : toRemove) {
			remove(tup.getFirst(), tup.getSecond());
		}
	}

	@Override
	public void clear() {
		levelCounts.clear();
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

	@Override
	public Iterable<Tuple<ResourceLocation, Integer>> baseIterator() {
		return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<Tuple<ResourceLocation, Integer>> iterator() {
                return new Iterator<>() {
                    final Iterator<ResourceLocation> perkIt = levels.keySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return perkIt.hasNext();
                    }

                    @Override
                    public Tuple<ResourceLocation, Integer> next() {
                        ResourceLocation perkKey = perkIt.next();
                        return new Tuple<>(perkKey, levels.get(perkKey));
                    }
                };
            }
        };
	}

}
