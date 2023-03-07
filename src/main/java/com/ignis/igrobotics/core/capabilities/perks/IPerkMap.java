package com.ignis.igrobotics.core.capabilities.perks;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.Tuple;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;

public interface IPerkMap extends Iterable<Tuple<Perk, Integer>> {
	
	/**
	 * Add levels to the map
	 * @param perk with a unique name which should not be present in the map
	 * @param level must be smaller than the maxLevel of respective perk
	 */
	void add(Perk perk, int level);
	
	/**
	 * Remove levels from the map
	 * @param perk any perk (not necessarily present in the map)
	 * @param level must be smaller than the maxLevel of respective perk
	 */
	void remove(Perk perk, int level);
	
	void merge(IPerkMap other);
	
	void diff(IPerkMap toRemove);
	
	boolean contains(Perk perk);
	
	default ArrayList<Component> getDisplayString() {
		ArrayList<Component> tooltip = new ArrayList<>();
		Multimap<String, AttributeModifier> modifiers = MultimapBuilder.hashKeys().arrayListValues().build();
		for(Tuple<Perk, Integer> tup : this) {
			Perk perk = tup.getFirst();
			
			if(!perk.isVisible()) continue;
			if(perk.showPerk()) {
				tooltip.add(perk.getDisplayText(tup.getSecond()));
			} else {
				//NOTE: Convoluting all perks while displaying them is inefficient
				modifiers.putAll(perk.getAttributeModifiers(tup.getSecond()));
			}
		}
		for(String attribute : modifiers.keySet()) {
			double amount = 0;
			double multiplier = 0;
			double exp = 0;
			for(AttributeModifier mod : modifiers.get(attribute)) {
				switch(mod.getOperation()) {
					case ADDITION: amount += mod.getAmount(); break;
					case MULTIPLY_BASE: multiplier += mod.getAmount() * 100; break;
					case MULTIPLY_TOTAL: exp += mod.getAmount(); break;
				}
			}
			TextColor color = Reference.ATTRIBUTE_COLORS.getOrDefault(attribute, TextColor.fromLegacyFormat(ChatFormatting.GRAY));
			String attr_name = Lang.localiseExisting("stats." + attribute).getString();

			if(amount != 0) tooltip.add(Lang.literal(Reference.FORMAT.format(amount) + " " + attr_name, color));
			if(multiplier != 0) tooltip.add(Lang.literal(Reference.FORMAT.format(multiplier) + "% " + attr_name, color));
			if(exp != 0) tooltip.add(Lang.literal("x" + String.format("%.2f", exp) + " " + attr_name, color));
		}
		return tooltip;
	}

}
