package com.ignis.igrobotics.core.capabilities.perks;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.Tuple;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Perk implements PerkHooks {
	
	/** Perks that do not stack should have a lower max level for efficiency reasons. 
	 * Since these obviously cannot stack, it just limits components to have a maximum level of this value */
	public static final int UNSTACKABLE_MAX_LEVEL = 20;
	
	private final String unlocalizedName;
	private int maxLevel;
	protected TextColor displayColor = TextColor.fromLegacyFormat(ChatFormatting.GOLD);
	private boolean visible = true;
	private boolean stackable = false;
	
	/**
	 * AttributeModifiers to be later applied f.e. to a robot. Maps {@link Attribute#getDescriptionId()}  Attribute Names} to modifiers affecting this attribute
	 */
	private final Multimap<Attribute, AttributeModifier> modifiers = MultimapBuilder.hashKeys().arrayListValues().build();
	
	/** 
	 * Maps attribute+operation to an array of scalars. There are the following possibilities: <p>
	 * <i> 1. The array contains 1 element: </i> The element is understood as a multiplier to the level of the perk <br>
	 * <i> 2. The array contains less than {@link #maxLevel} elements: </i><b> Invalid state </b><br>
	 * <i> 3. The array contains equal or more than {@link #maxLevel} elements: </i> The elements are understood as absolute values 
	 *    replacing the values in {@link #modifiers} with the according level 
	 **/
	private final Map<Tuple<Attribute, Integer>, double[]> scalars = new HashMap<>();
	
	public Perk(String name, int maxLevel) {
		this.unlocalizedName = name;
		this.maxLevel = maxLevel;
	}
	
	//////////////////////////////////
	// Relevant Getters & Setters
	//////////////////////////////////
	
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(int level) {
		Multimap<Attribute, AttributeModifier> scaledModifiers = MultimapBuilder.hashKeys().arrayListValues().build();
		modifiers.forEach((attribute, u) -> {
			double[] sc = getScalars(attribute, u.getOperation().toValue());
			double attributeValue = u.getAmount();

			if (sc != null && sc.length > 0) {
				if (sc.length == 1) {
					attributeValue = u.getAmount() + level * sc[0];
				} else {
					//Use scalars directly. Do not extend over the length of the provided array
					attributeValue = sc[Math.min(level, sc.length) - 1];
				}
			}

			scaledModifiers.put(attribute, new AttributeModifier(u.getId(), u.getName(), attributeValue, u.getOperation()));
		});
		return scaledModifiers;
	}
	
	public double[] getScalars(Attribute attribute, int operation) {
		return scalars.get(new Tuple<>(attribute, operation));
	}
	
	/**
	 * Whether the perk name should be drawn. If not, and the perk is visible, the attributes will be shown
	 * @return whether the raw name should be drawn
	 */
	public boolean showPerk() {
		return maxLevel != Integer.MAX_VALUE || !stackable;
	}
	
	/**
	 * Only has an effect if the perk is actually {@link #showPerk() shown}. 
	 * Otherwise it's attributes are simply displayed
	 * @return the text to be displayed for this perk
	 */
	public Component getDisplayText(int level) {
		MutableComponent display;
		if(getMaxLevel() == 1) {
			display = Lang.localise(unlocalizedName);
		} else {
			List<Component> text = List.of(Lang.localise(unlocalizedName), Component.literal(" "), Component.translatable("enchantment.level." + level));
			display = ComponentUtils.formatList(text, CommonComponents.EMPTY, Function.identity());
		}
		display.setStyle(display.getStyle().withColor(displayColor));
		return display;
	}
	
	@Override
	public String toString() {
		return Lang.localise(unlocalizedName).getString();
	}
	
	//////////////////////////////////
	// Serialization
	//////////////////////////////////
	
	public static JsonElement serialize(Perk perk) {
		JsonObject obj = new JsonObject();
		obj.addProperty("name", perk.unlocalizedName);
		obj.addProperty("maxLevel", perk.maxLevel);
		obj.addProperty("displayColor", perk.displayColor.serialize());
		obj.addProperty("visible", perk.visible);
		obj.addProperty("stackable", perk.stackable);
		
		JsonArray attr = new JsonArray();
		for(Attribute attribute : perk.modifiers.keys()) {
			JsonObject attr_obj = new JsonObject();
			JsonArray mod_list = new JsonArray();
			for(AttributeModifier modifier : perk.modifiers.get(attribute)) {
				JsonObject mod_obj = new JsonObject();
				mod_obj.addProperty("operation", modifier.getOperation().toValue());
				
				//Values and/or Scalars
				double[] scalars = perk.getScalars(attribute, modifier.getOperation().toValue());
				if(scalars == null) {
					mod_obj.addProperty("value", modifier.getAmount());
				} else if(scalars.length == 1) {
					mod_obj.addProperty("value", modifier.getAmount());
					mod_obj.addProperty("scalar", scalars[0]);
				} else if(scalars.length > 1) {
					JsonArray arr = new JsonArray();
					for(double d : scalars) {
						arr.add(d);
					}
					mod_obj.add("value", arr);
				}

				mod_list.add(mod_obj);
			}
			attr_obj.addProperty("name", attribute.getDescriptionId());
			attr_obj.add("modifiers", mod_list);
			
			attr.add(attr_obj);
		}
		
		obj.add("attributes", attr);
		
		return obj;
	}
	
	public static Perk deserialize(JsonElement json) {
		JsonObject obj = json.getAsJsonObject();
		String unlocalizedName = obj.get("name").getAsString();

		int maxLevel = obj.has("maxLevel") ? obj.get("maxLevel").getAsInt() : Integer.MAX_VALUE;
		Perk result = new Perk(unlocalizedName, maxLevel);

		// Retrieve the perk from the config if its name already has been defined
		RoboticsConfig config = RoboticsConfig.current();
		if(config.perks.PERKS.containsKey(unlocalizedName)) {
			result = config.perks.PERKS.get(unlocalizedName);
		}
		
		if(obj.has("visible")) result.visible = obj.get("visible").getAsBoolean();
		if(obj.has("stackable")) result.stackable = obj.get("stackable").getAsBoolean();
		if(!result.stackable) result.maxLevel = Math.min(UNSTACKABLE_MAX_LEVEL, result.maxLevel);
		if(obj.has("displayColor")) result.displayColor = TextColor.parseColor(obj.get("displayColor").getAsString());
		
		int i = 0;
		if(obj.has("attributes")) {
			for(JsonElement attribute : obj.get("attributes").getAsJsonArray()) {
				String attributeName = ((JsonObject) attribute).get("name").getAsString();
				ResourceLocation attributeLoc = ResourceLocation.tryParse(attributeName);
				if(attributeLoc == null) {
					throw new JsonSyntaxException("The specified attribute " + attributeName + " could not be found");
				}
				Attribute attributeType = ForgeRegistries.ATTRIBUTES.getValue(attributeLoc);
				if(attributeType == null) {
					throw new JsonSyntaxException("The specified attribute " + attributeName + " could not be found");
				}
				for(JsonElement modifier : ((JsonObject) attribute).get("modifiers").getAsJsonArray()) {
					JsonObject jsonModifier = ((JsonObject) modifier);
					
					int operation = jsonModifier.get("operation").getAsInt();
					double amount;
					if(!jsonModifier.get("value").isJsonArray()) {
						amount = jsonModifier.get("value").getAsDouble();
						if(jsonModifier.has("scalar")) {
							double scalar = jsonModifier.get("scalar").getAsDouble();
							result.scalars.put(new Tuple<>(attributeType, operation), new double[] {scalar});
						}
					} else {
						JsonArray jsonScalars = jsonModifier.get("value").getAsJsonArray();
						double[] scalars = new double[jsonScalars.size()];
						for(int j = 0; j < jsonScalars.size(); j++) {
							scalars[j] = jsonScalars.get(j).getAsDouble();
						}
						result.scalars.put(new Tuple<>(attributeType, operation), scalars);
						amount = scalars[0];
					}
					
					AttributeModifier mod = new AttributeModifier("modifier_" + (i++), amount, AttributeModifier.Operation.fromValue(operation));
					result.modifiers.put(attributeType, mod);
				}
				
			}
		}
		
		return result;
	}
	
	//////////////////////////////////
	// Simple Getters & Setters
	//////////////////////////////////
	
	public boolean isStackable() {
		return stackable;
	}
	
	protected void setStackable(boolean stackable) {
		this.stackable = stackable;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public String getUnlocalizedName() {
		return unlocalizedName;
	}
	
	public int getMaxLevel() {
		return maxLevel;
	}
	
	public Perk setDisplayColor(TextColor displayColor) {
		this.displayColor = displayColor;
		return this;
	}

}
