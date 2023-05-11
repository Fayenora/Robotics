package com.ignis.igrobotics.core.capabilities.perks;

import com.ignis.igrobotics.core.util.Tuple;

import java.util.HashMap;
import java.util.Iterator;

/**
 * An {@link IPerkMap} implementation that can handle perks that stack as well as non-stacking perks
 * @author Ignis
 */
public class AdvancedPerkMap implements IPerkMap {

	private final HashMap<String, int[]> levelCounts = new HashMap<>();
	HashMap<String, Integer> levels = new HashMap<>();
	HashMap<String, Perk> perks = new HashMap<>();

	@Override
	public void add(Perk perk, int level) {
		if(level == 0) return;
		int currLevel = levels.getOrDefault(perk.getUnlocalizedName(), 0);
		int updatedLevel = (perk.isStackable() ? currLevel + level : Math.max(currLevel, level));
		perks.put(perk.getUnlocalizedName(), perk);
		levels.put(perk.getUnlocalizedName(), updatedLevel);
		
		//If the perk is not stackable, we also need to keep track of how often which level was added
		if(!perk.isStackable()) {
			if(!levelCounts.containsKey(perk.getUnlocalizedName())) {
				levelCounts.put(perk.getUnlocalizedName(), new int[perk.getMaxLevel()]);
			}
			levelCounts.get(perk.getUnlocalizedName())[level - 1]++;
		}
	}

	@Override
	public void remove(Perk perk, int level) {
		if(level == 0) return;
		String key = perk.getUnlocalizedName();
		if(!perks.containsKey(key)) return;
		
		int updatedLevel;
		if(perk.isStackable()) {
			updatedLevel = levels.getOrDefault(perk.getUnlocalizedName(), 0) - level;
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
			perks.remove(key);
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
				String key = perk.getUnlocalizedName();
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
				String key = perk.getUnlocalizedName();
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
		perks.clear();
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

}
