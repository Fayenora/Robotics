package com.ignis.igrobotics.core.capabilities.perks;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.handlers.RobotBehavior;
import com.ignis.igrobotics.core.access.EnumPermission;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.robot.IRobot;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.MathUtil;
import com.ignis.igrobotics.definitions.ModPerks;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Perk implements PerkHooks {

	/** Perks that do not stack should have a lower max level for efficiency reasons.
	 * Since these obviously cannot stack, it just limits components to have a maximum level of this value */
	public static final int UNSTACKABLE_MAX_LEVEL = 20;
	public static final TextColor DEFAULT_COLOR = TextColor.fromLegacyFormat(ChatFormatting.GOLD);
	private static int modifierId = 0;

	private record AttributeScalar(int id, int operation, Either<Double, List<Double>> value, Optional<Double> scalar) {
		AttributeScalar(Either<Double, List<Double>> value, int operation, Optional<Double> scalar) {
			this(modifierId++, operation, value, scalar);
		}
		AttributeModifier getModifier(int level) {
			double attributeValue = value.map(Function.identity(), l -> l.get(MathUtil.restrict(0, level, l.size())));
			if (scalar.isPresent()) {
				attributeValue += level * scalar.get();
			}
			return new AttributeModifier("modifier_" + id, attributeValue, AttributeModifier.Operation.fromValue(operation));
		}
	}

	private record AttributeEntry(Attribute attribute, List<AttributeScalar> modifiers) {}

	public static final Codec<AttributeScalar> CODEC_SCALAR = RecordCodecBuilder.create(instance -> instance.group(
			Codec.either(Codec.DOUBLE, Codec.list(Codec.DOUBLE)).optionalFieldOf("value", Either.left(0d)).forGetter(c -> c.value),
			Codec.intRange(0, 2).fieldOf("operation").forGetter(c -> c.operation),
			Codec.DOUBLE.optionalFieldOf("scalar").forGetter(c -> c.scalar)
	).apply(instance, AttributeScalar::new));

	public static final Codec<AttributeEntry> CODEC_ATTRIBUTE = RecordCodecBuilder.create(instance -> instance.group(
			ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("name").forGetter(c -> c.attribute),
			Codec.list(CODEC_SCALAR).fieldOf("modifiers").forGetter(c -> c.modifiers)
	).apply(instance, AttributeEntry::new));

	public static final Codec<Perk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("name").forGetter(c -> c.name),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("maxLevel", Integer.MAX_VALUE).forGetter(Perk::getMaxLevel),
			Codec.BOOL.optionalFieldOf("internalLogic", false).forGetter(c -> ModPerks.REGISTRY.get().containsValue(c)),
			Codec.BOOL.optionalFieldOf("visible", true).forGetter(Perk::isVisible),
			Codec.BOOL.optionalFieldOf("stackable", false).forGetter(Perk::isStackable),
			TextColor.CODEC.optionalFieldOf("displayColor", DEFAULT_COLOR).forGetter(Perk::getDisplayColor),
			Codec.list(CODEC_ATTRIBUTE).optionalFieldOf("attributes", List.of()).forGetter(Perk::getModifiers)
	).apply(instance, Perk::initialize));

	private final ResourceLocation name, iconTexture;
	private int maxLevel;
	protected TextColor displayColor = DEFAULT_COLOR;
	private boolean visible = true;
	private boolean stackable = false;

	private final List<AttributeEntry> modifiers = new ArrayList<>();

	public Perk(String name) {
		this.name = Robotics.rl(name);
		this.iconTexture = Robotics.rl("textures/perk/" + name + ".png");
	}

	public Perk(String name, int maxLevel) {
		this(name);
		this.maxLevel = maxLevel;
	}

	public Perk(ResourceLocation name) {
		this.name = name;
		this.iconTexture = new ResourceLocation(name.getNamespace(), "textures/perk/" + name.getPath() + ".png");
	}

	//////////////////////////////////
	// Relevant Getters & Setters
	//////////////////////////////////

	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(int level) {
		Multimap<Attribute, AttributeModifier> scaledModifiers = MultimapBuilder.hashKeys().arrayListValues().build();
		for(AttributeEntry entry : modifiers) {
			for(AttributeScalar scalar : entry.modifiers) {
				scaledModifiers.put(entry.attribute, scalar.getModifier(level));
			}
		}
		return scaledModifiers;
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
	 * Otherwise, it's attributes are simply displayed
	 * @return the text to be displayed for this perk
	 */
	public Component getDisplayText(int level) {
		MutableComponent display;
		if(getMaxLevel() == 1) {
			display = localized();
		} else {
			List<Component> text = List.of(localized(), Component.literal(" "), Component.translatable("enchantment.level." + level));
			display = ComponentUtils.formatList(text, CommonComponents.EMPTY, Function.identity());
		}
		display.setStyle(display.getStyle().withColor(displayColor));
		return display;
	}

	public Component getDescriptionText() {
		if(modifiers.size() == 0) {
			return Lang.localiseExisting(getUnlocalizedName() + ".desc");
		}
		ArrayList<Component> tooltip = new ArrayList<>();
		tooltip.add(Lang.localise("perk.desc"));
		for(AttributeEntry entry : modifiers) {
			TextColor color = Reference.ATTRIBUTE_COLORS.getOrDefault(entry.attribute, TextColor.fromLegacyFormat(ChatFormatting.GRAY));
			Component attr_name = Component.translatable(entry.attribute.getDescriptionId()).withStyle(Style.EMPTY.withColor(color));
			StringBuilder literal = new StringBuilder();
			for(AttributeScalar scalar : entry.modifiers) {
				if(scalar.value.left().isPresent()) {
					double baseValue = scalar.value.left().get();
					double scaleValue = scalar.scalar.orElse(0d);
					if(baseValue != 0) {
						literal.append(Reference.FORMAT.format(baseValue));
					}
					if(scaleValue != 0) {
						literal.append(switch (scalar.operation) {
							case 0 -> Reference.FORMAT.format(scaleValue);
							case 1 -> Reference.FORMAT.format(scaleValue) + "%";
							case 2 -> "x" + String.format("%.2f", scaleValue);
							default -> throw new IllegalStateException("Unexpected value: " + scalar.operation);
						});
						literal.append("x").append(Lang.localise("level").getString());
					}
					literal.append(" ");
				} else if(scalar.value.right().isPresent()) {
					literal.append(scalar.operation == 2 ? "x" : "");
					for(int i = 0; i < scalar.value.right().get().size(); i++) {
						double s = scalar.value.right().get().get(i);
						if(i != 0) {
							literal.append("/");
						}
						literal.append(switch (scalar.operation) {
							case 0 -> Reference.FORMAT.format(s);
							case 1 -> Reference.FORMAT.format(s * 100);
							case 2 -> String.format("%.2f", s);
							default -> throw new IllegalStateException("Unexpected value: " + scalar.operation);
						});
					}
					literal.append(scalar.operation == 1 ? "% " : " ");
				}
			}
			tooltip.add(combine(Component.literal(literal.toString()), attr_name));
		}
		return ComponentUtils.formatList(tooltip, Component.literal("\n"));
	}

	static Component combine(Component prefix, Component comp) {
		return ComponentUtils.formatList(List.of(prefix, comp), Component.empty());
	}

	public MutableComponent localized() {
		return Lang.localiseExisting(getUnlocalizedName());
	}

	@Override
	public String toString() {
		return localized().getString();
	}

	@Override
	public Perk clone() {
		Perk otherPerk = new Perk(name);
		otherPerk.maxLevel = maxLevel;
		otherPerk.displayColor = displayColor;
		otherPerk.visible = visible;
		otherPerk.stackable = stackable;
		otherPerk.modifiers.addAll(modifiers);
		return otherPerk;
	}

	//////////////////////////////////
	// Serialization
	//////////////////////////////////

	private static Perk initialize(ResourceLocation key, int maxLevel, boolean internal, boolean visible, boolean stackable, TextColor displayColor, List<AttributeEntry> modifiers) {
		String name = key.getPath();
		IForgeRegistry<Perk> registry = ModPerks.REGISTRY.get();
		Perk perk = internal && registry.containsKey(key) ? registry.getValue(key) : new Perk(name, maxLevel);
		perk.maxLevel = maxLevel;
		perk.setVisible(visible);
		perk.setStackable(stackable);
		perk.setDisplayColor(displayColor);
		perk.setModifiers(modifiers);
		return perk;
	}

	//////////////////////////////////
	// Simple Getters & Setters
	//////////////////////////////////

	public boolean isStackable() {
		return stackable;
	}

	public Perk setStackable(boolean stackable) {
		this.stackable = stackable;
		if(!stackable) maxLevel = Math.min(UNSTACKABLE_MAX_LEVEL, maxLevel);
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	protected void setVisible(boolean visible) {
		this.visible = visible;
	}

	public ResourceLocation getKey() {
		return name;
	}

	public String getId() {
		return name.toString();
	}

	public String getUnlocalizedName() {
		return name.getNamespace() + ".perk." + name.getPath();
	}

	public int getMaxLevel() {
		return stackable ? maxLevel : UNSTACKABLE_MAX_LEVEL;
	}

	public Perk setDisplayColor(TextColor displayColor) {
		this.displayColor = displayColor;
		return this;
	}

	public Perk setDisplayColor(ChatFormatting displayColor) {
		return setDisplayColor(TextColor.fromLegacyFormat(displayColor));
	}

	public TextColor getDisplayColor() {
		return displayColor;
	}

	public ResourceLocation getIconTexture() {
		return iconTexture;
	}

	public List<AttributeEntry> getModifiers() {
		return modifiers;
	}

	private void setModifiers(List<AttributeEntry> modifiers) {
		this.modifiers.clear();
		this.modifiers.addAll(modifiers);
	}

	/////////////////////////////////////
	// Helper methods for subclasses
	/////////////////////////////////////

	public static Collection<Entity> entitiesInArea(Entity entity, int areaSize, Predicate<Entity> additionalRequisites) {
		BlockPos lower = entity.blockPosition().relative(Direction.DOWN, areaSize).relative(Direction.SOUTH, areaSize).relative(Direction.EAST, areaSize);
		BlockPos upper = entity.blockPosition().relative(Direction.UP, areaSize).relative(Direction.NORTH, areaSize).relative(Direction.WEST, areaSize);
		AABB area = new AABB(lower, upper);
		return entity.level().getEntities(entity, area, additionalRequisites);
	}

	public static Collection<Entity> alliesInArea(Entity entity, int areaSize, UUID owner, Predicate<Entity> additionalRequisites) {
		return entitiesInArea(entity, areaSize, ent -> {
			Optional<IRobot> otherRobot = ent.getCapability(ModCapabilities.ROBOT).resolve();
			if(otherRobot.isEmpty() || !otherRobot.get().isActive()) return false;
			if(!RobotBehavior.hasAccess(owner, ent, EnumPermission.ALLY)) return false;
			return additionalRequisites.test(ent);
		});
	}
}
