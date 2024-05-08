package com.ignis.igrobotics.core.capabilities.perks;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonElement;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.util.Lang;
import com.ignis.igrobotics.core.util.MathUtil;
import com.ignis.igrobotics.integration.config.RoboticsConfig;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

	private static final Codec<Perk> DEFINITION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(Perk::getUnlocalizedName),
			Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("maxLevel", Integer.MAX_VALUE).forGetter(Perk::getMaxLevel),
			Codec.BOOL.optionalFieldOf("visible", true).forGetter(Perk::isVisible),
			Codec.BOOL.optionalFieldOf("stackable", false).forGetter(Perk::isStackable),
			TextColor.CODEC.optionalFieldOf("displayColor", DEFAULT_COLOR).forGetter(Perk::getDisplayColor),
			Codec.list(CODEC_ATTRIBUTE).optionalFieldOf("attributes", List.of()).forGetter(Perk::getModifiers)
	).apply(instance, Perk::initialize));

	public static final Codec<Perk> CODEC = Codec.either(ResourceLocation.CODEC, DEFINITION_CODEC).comapFlatMap(
			e -> e.map(key ->
				Optional.ofNullable(RoboticsConfig.current().perks.PERKS.get(key.getPath()))
						.map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "No perk named '" + key + "'")),
					DataResult::success
			),
			Either::right
	);

	private final String unlocalizedName;
	private int maxLevel;
	protected TextColor displayColor = DEFAULT_COLOR;
	private boolean visible = true;
	private boolean stackable = false;
	private final ResourceLocation iconTexture;

	private final List<AttributeEntry> modifiers = new ArrayList<>();

	public Perk(String name, int maxLevel) {
		this.unlocalizedName = name;
		this.maxLevel = maxLevel;
		iconTexture = new ResourceLocation(Robotics.MODID, "textures/perk/" + unlocalizedName.split("\\.")[1].toLowerCase() + ".png");
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
			display = Lang.localise(unlocalizedName);
		} else {
			List<Component> text = List.of(Lang.localise(unlocalizedName), Component.literal(" "), Component.translatable("enchantment.level." + level));
			display = ComponentUtils.formatList(text, CommonComponents.EMPTY, Function.identity());
		}
		display.setStyle(display.getStyle().withColor(displayColor));
		return display;
	}

	public Component getDescriptionText() {
		if(modifiers.size() == 0) {
			return Lang.localise(getUnlocalizedName() + ".desc");
		}
		ArrayList<Component> tooltip = new ArrayList<>();
		tooltip.add(Lang.localise("perk.desc"));
		for(AttributeEntry entry : modifiers) {
			TextColor color = Reference.ATTRIBUTE_COLORS.getOrDefault(entry.attribute, TextColor.fromLegacyFormat(ChatFormatting.GRAY));
			Component attr_name = Component.translatable(entry.attribute.getDescriptionId()).withStyle(Style.EMPTY.withColor(color));
			StringBuilder literal = new StringBuilder();
			for(AttributeScalar scalar : entry.modifiers) {
				if(scalar.value.left().isPresent()) {
					double value = scalar.value.left().get();
					literal.append(switch (scalar.operation) {
						case 0 -> Reference.FORMAT.format(value);
						case 1 -> Reference.FORMAT.format(value) + "%";
						case 2 -> "x" + String.format("%.2f", value);
						default -> throw new IllegalStateException("Unexpected value: " + scalar.operation);
					});
					literal.append("x").append(Lang.localise("level").getString()).append(" ");
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

	@Override
	public String toString() {
		return Lang.localise(unlocalizedName).getString();
	}

	@Override
	public Perk clone() {
		Perk otherPerk = new Perk(unlocalizedName, maxLevel);
		otherPerk.displayColor = displayColor;
		otherPerk.visible = visible;
		otherPerk.stackable = stackable;
		otherPerk.modifiers.addAll(modifiers);
		return otherPerk;
	}

	//////////////////////////////////
	// Serialization
	//////////////////////////////////

	private static Perk initialize(String name, int maxLevel, boolean visible, boolean stackable, TextColor displayColor, List<AttributeEntry> modifiers) {
		Perk perk = new Perk(name, maxLevel);
		perk.setVisible(visible);
		perk.setStackable(stackable);
		perk.setDisplayColor(displayColor);
		perk.setModifiers(modifiers);
		return perk;
	}

	public static JsonElement serialize(Perk perk) {
		return CODEC.encodeStart(JsonOps.INSTANCE, perk).getOrThrow(false, s -> {
			throw new RuntimeException(s);
		});
	}

	public static Perk deserialize(JsonElement json) {
		return CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {
			throw new RuntimeException(s);
		});
	}

	public static void write(FriendlyByteBuf buffer, Perk perk) {
		/*
		buffer.writeUtf(perk.unlocalizedName);
		buffer.writeInt(perk.maxLevel);
		buffer.writeBoolean(perk.visible);
		buffer.writeBoolean(perk.stackable);
		buffer.writeInt(perk.displayColor.getValue());

		buffer.writeShort(perk.modifiers.keys().size());
		for(Attribute attr : perk.modifiers.keySet()) {
			buffer.writeRegistryId(ForgeRegistries.ATTRIBUTES, attr);
			buffer.writeShort(perk.modifiers.get(attr).size());
			for(AttributeModifier modifier : perk.modifiers.get(attr)) {
				buffer.writeByte(modifier.getOperation().toValue());
				buffer.writeDouble(modifier.getAmount());
			}
		}

		buffer.writeShort(perk.scalars.size());
		for(Tuple<Attribute, Integer> key : perk.scalars.keySet()) {
			buffer.writeRegistryId(ForgeRegistries.ATTRIBUTES, key.first);
			buffer.writeByte(key.second);
			buffer.writeShort(perk.scalars.get(key).length);
			for(double d : perk.scalars.get(key)) {
				buffer.writeDouble(d);
			}
		}

		 */
	}

	public static Perk read(FriendlyByteBuf buffer) {
		/*
		String name = buffer.readUtf();
		int maxLevel = buffer.readInt();
		Perk result = new Perk(name, maxLevel);

		result.setVisible(buffer.readBoolean());
		result.setStackable(buffer.readBoolean());
		result.setDisplayColor(TextColor.fromRgb(buffer.readInt()));

		short nAttributes = buffer.readShort();
		for(int i = 0; i < nAttributes; i++) {
			Attribute attribute = buffer.readRegistryIdSafe(Attribute.class);
			short nModifiers = buffer.readShort();
			for(int j = 0; j < nModifiers; j++) {
				byte operation = buffer.readByte();
				double amount = buffer.readDouble();
				AttributeModifier modifier = new AttributeModifier("modifier_" + (j++), amount, AttributeModifier.Operation.fromValue(operation));
				result.modifiers.put(attribute, modifier);
			}
		}

		short nScalars = buffer.readShort();
		for(int i = 0; i < nScalars; i++) {
			Attribute attribute = buffer.readRegistryIdSafe(Attribute.class);
			int operation = buffer.readByte();
			short arrSize = buffer.readShort();
			double[] arr = new double[arrSize];
			for(int j = 0; j < arrSize; j++) {
				arr[j] = buffer.readDouble();
			}
			result.scalars.put(new Tuple<>(attribute, operation), arr);
		}

		return result;

		 */
		return new Perk("dummy", 0);
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

	public String getUnlocalizedName() {
		return unlocalizedName;
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
}
