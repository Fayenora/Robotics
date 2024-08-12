package com.ignis.igrobotics.common.capabilities;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.capabilities.impl.perk.Perk;
import com.ignis.igrobotics.common.helpers.types.SimpleDataManager;
import com.ignis.igrobotics.common.helpers.types.Tuple;
import com.ignis.igrobotics.common.helpers.util.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterCapability
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

	void clear();

	boolean contains(Perk perk);

	int getLevel(Perk perk);

	SimpleDataManager values();

	default List<Component> getDisplayString() {
		List<Component> tooltip = new ArrayList<>();
		Multimap<Attribute, AttributeModifier> modifiers = MultimapBuilder.hashKeys().arrayListValues().build();
		for(Tuple<Perk, Integer> tup : this) {
			Perk perk = tup.getFirst();

			if(!perk.isVisible()) continue;
			if(perk.showPerk()) {
				tooltip.add(perk.getDisplayText(tup.getSecond()));
			} else {
				//NOTE: Convolution of all perks while displaying them is inefficient
				modifiers.putAll(perk.getAttributeModifiers(tup.getSecond()));
			}
		}
		tooltip.addAll(createAttributeTooltip(modifiers));
		return tooltip;
	}

	static List<Component> createAttributeTooltip(Multimap<Attribute, AttributeModifier> modifiers) {
		List<Component> tooltip = new ArrayList<>();
		for(Attribute attribute : modifiers.keySet()) {
			double amount = 0;
			double multiplier = 0;
			double exp = 0;
			for(AttributeModifier mod : modifiers.get(attribute)) {
				switch (mod.getOperation()) {
					case ADDITION -> amount += mod.getAmount();
					case MULTIPLY_BASE -> multiplier += mod.getAmount() * 100;
					case MULTIPLY_TOTAL -> exp += mod.getAmount();
				}
			}
			TextColor color = Reference.ATTRIBUTE_COLORS.getOrDefault(attribute, TextColor.fromLegacyFormat(ChatFormatting.GRAY));
			Component attr_name = Component.translatable(attribute.getDescriptionId()).withStyle(Style.EMPTY.withColor(color));

			if(amount != 0) tooltip.add(combine(Lang.literal(Reference.FORMAT.format(amount) + " ", color), attr_name));
			if(multiplier != 0) tooltip.add(combine(Lang.literal(Reference.FORMAT.format(multiplier) + "% ", color), attr_name));
			if(exp != 0) tooltip.add(combine(Lang.literal("x" + String.format("%.2f", exp) + " ", color), attr_name));
		}
		return tooltip;
	}

	static Component combine(Component prefix, Component comp) {
		return ComponentUtils.formatList(List.of(prefix, comp), Component.empty());
	}

	Iterable<Tuple<ResourceLocation, Integer>> baseIterator();

}
