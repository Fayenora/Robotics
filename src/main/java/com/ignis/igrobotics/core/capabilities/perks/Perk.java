package com.ignis.igrobotics.core.capabilities.perks;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ignis.igrobotics.core.SimpleDataManager;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.Tuple;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Perk {
	
	/** Perks that do not stack should have a lower max level for efficiency reasons. 
	 * Since these obviously cannot stack, it just limits components to have a maximum level of this value */
	public static final int UNSTACKABLE_MAX_LEVEL = 20;
	
	private String unlocalizedName;
	private int maxLevel = Integer.MAX_VALUE;
	private TextColor displayColor = TextColor.fromLegacyFormat(ChatFormatting.GOLD);
	private boolean visible = true;
	private boolean stackable = false;
	
	/**
	 * AttributeModifiers to be later applied f.e. to a robot. Maps {@link Attribute#getDescriptionId()}  Attribute Names} to modifiers affecting this attribute
	 */
	private Multimap<String, AttributeModifier> modifiers = MultimapBuilder.hashKeys().arrayListValues().build();
	
	/** 
	 * Maps attribute+operation to an array of scalars. There are the following possibilities: <p>
	 * <i> 1. The array contains 1 element: </i> The element is understood as a multiplier to the level of the perk <br>
	 * <i> 2. The array contains less than {@link #maxLevel} elements: </i><b> Invalid state </b><br>
	 * <i> 3. The array contains equal or more than {@link #maxLevel} elements: </i> The elements are understood as absolute values 
	 *    replacing the values in {@link #modifiers} with the according level 
	 **/
	private Map<Tuple<String, Integer>, Double[]> scalars = new HashMap<>();
	
	public Perk(String name, int maxLevel) {
		this.unlocalizedName = name;
		this.maxLevel = maxLevel;
	}
	
	public void onEntityUpdate(int level, Entity entity, SimpleDataManager values) {
		//NO-OP
	}
	
	public void onEntityJump(int level, Entity entity, SimpleDataManager values) {
		//NO-OP
	}
	
	/**
	 * Executed when a robot with this perk is attacking another entity
	 * @param level of the perk
	 * @param toAttack entity that is attacked
	 * @return Knockback to add
	 */
	public int attackEntityAsMob(int level, Entity attacker, Entity toAttack, SimpleDataManager values) {
		return 0;
	}
	
	/**
	 * Executed when a robot with this perk gets damaged
	 * @param level
	 * @param dmgSource
	 * @param damage
	 * @return adjusted damage
	 */
	public float damageEntity(int level, Entity robot, DamageSource dmgSource, float damage, SimpleDataManager values) {
		return damage;
	}
	
	//////////////////////////////////
	// Relevant Getters & Setters
	//////////////////////////////////
	
	public Multimap<String, AttributeModifier> getAttributeModifiers(int level) {
		Multimap<String, AttributeModifier> scaledModifiers = MultimapBuilder.hashKeys().arrayListValues().build();
		modifiers.forEach(new BiConsumer<String, AttributeModifier>() {
			@Override
			public void accept(String attribute, AttributeModifier u) {
				Double[] sc = getScalars(attribute, u.getOperation().toValue());
				double attributeValue = u.getAmount();
				
				if(sc != null && sc.length > 0) {
					if(sc.length == 1) {
						attributeValue = u.getAmount() + level * sc[0];
					} else {
						//Use scalars directly. Do not extend over the length of the provided array
						attributeValue = sc[Math.min(level, sc.length) - 1];
					}
				}
				
				scaledModifiers.put(attribute, new AttributeModifier(u.getId(), u.getName(), attributeValue, u.getOperation()));
			}
		});
		return scaledModifiers;
	}
	
	public Double[] getScalars(String attribute, int operation) {
		return scalars.get(new Tuple<String, Integer>(attribute, operation));
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
		for(String attribute : perk.modifiers.keys()) {
			JsonObject attr_obj = new JsonObject();
			JsonArray mod_list = new JsonArray();
			for(AttributeModifier modifier : perk.modifiers.get(attribute)) {
				JsonObject mod_obj = new JsonObject();
				mod_obj.addProperty("operation", modifier.getOperation().toValue());
				
				//Values and/or Scalars
				Double[] scalars = perk.getScalars(attribute, modifier.getOperation().toValue());
				if(scalars == null) {
					mod_obj.addProperty("value", modifier.getAmount());
				}
				if(scalars.length == 1) {
					mod_obj.addProperty("value", modifier.getAmount());
					mod_obj.addProperty("scalar", scalars[0]);
				} 
				if(scalars.length > 1) {
					JsonArray arr = new JsonArray();
					for(double d : scalars) {
						arr.add(d);
					}
					mod_obj.add("value", arr);
				}
				
				
				mod_list.add(mod_obj);
			}
			attr_obj.addProperty("name", attribute);
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

		/* Retrieve the perk from the config if its name already has been defined
		RoboticsConfig config = RoboticsConfig.current();
		if(config.perks.PERKS.containsKey(unlocalizedName)) {
			result = config.perks.PERKS.get(unlocalizedName);
		} */
		
		if(obj.has("visible")) result.visible = obj.get("visible").getAsBoolean();
		if(obj.has("stackable")) result.stackable = obj.get("stackable").getAsBoolean();
		if(!result.stackable) result.maxLevel = Math.min(UNSTACKABLE_MAX_LEVEL, result.maxLevel);
		if(obj.has("displayColor")) result.displayColor = TextColor.parseColor(obj.get("displayColor").getAsString());
		
		int i = 0;
		if(obj.has("attributes")) {
			for(JsonElement attribute : obj.get("attributes").getAsJsonArray()) {
				String attributeName = ((JsonObject) attribute).get("name").getAsString();
				for(JsonElement modifier : ((JsonObject) attribute).get("modifiers").getAsJsonArray()) {
					JsonObject jsonModifier = ((JsonObject) modifier);
					
					int operation = jsonModifier.get("operation").getAsInt();
					double amount;
					if(!jsonModifier.get("value").isJsonArray()) {
						amount = jsonModifier.get("value").getAsDouble();
						if(jsonModifier.has("scalar")) {
							double scalar = jsonModifier.get("scalar").getAsDouble();
							result.scalars.put(new Tuple(attributeName, operation), new Double[] {scalar});
						}
					} else {
						JsonArray jsonScalars = jsonModifier.get("value").getAsJsonArray();
						Double[] scalars = new Double[jsonScalars.size()];
						for(int j = 0; j < jsonScalars.size(); j++) {
							scalars[j] = jsonScalars.get(j).getAsDouble();
						}
						result.scalars.put(new Tuple(attributeName, operation), scalars);
						amount = scalars[0];
					}
					
					AttributeModifier mod = new AttributeModifier("modifier_" + (i++), amount, AttributeModifier.Operation.fromValue(operation));
					result.modifiers.put(attributeName, mod);
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
