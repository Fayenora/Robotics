package com.io.norabotics.definitions.robotics;

import com.google.common.base.Functions;
import com.io.norabotics.Robotics;
import com.io.norabotics.common.content.actions.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.*;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModActions {

    public static final ResourceKey<Registry<Codec<? extends IAction>>> KEY = ResourceKey.createRegistryKey(Robotics.rl("actions"));
    public static final DeferredRegister<Codec<? extends IAction>> ACTION_SERIALIZERS = DeferredRegister.create(KEY, Robotics.MODID);
    public static final Supplier<IForgeRegistry<Codec<? extends IAction>>> REGISTRY = ACTION_SERIALIZERS.makeRegistry(() -> new RegistryBuilder<Codec<? extends IAction>>().disableSaving().setDefaultKey(Robotics.rl("none")));

    public static final Codec<MobEffectInstance> EFFECT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("type").forGetter(MobEffectInstance::getEffect),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("duration", 30 * 20).forGetter(MobEffectInstance::getDuration),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier),
            MapCodec.unit(false).forGetter(MobEffectInstance::isAmbient),
            Codec.BOOL.optionalFieldOf("visible", true).forGetter(MobEffectInstance::isVisible)
    ).apply(builder, MobEffectInstance::new));
    private static final Codec<IAction> DETAILED_CODEC = ExtraCodecs.lazyInitializedCodec(() -> REGISTRY.get().getCodec().dispatch(IAction::codec, Function.identity()));
    /*
     * Allow for type specification in a compound (specified by the detailed codec) as well as just referencing the type itself:
     * {
     *    ...
     *    "action": "norabotics:dash"
     *    ...
     * }
     * is just as valid as
     * {
     *    ...
     *    "action": {
     *        "type": "norabotics:dash"
     *        ...
     *     } ...
     * }
     */
    public static final Codec<IAction> CODEC = ExtraCodecs.lazyInitializedCodec(() -> Codec.either(REGISTRY.get().getCodec(), DETAILED_CODEC).
            xmap(either -> either.map(c -> c.decode(NbtOps.INSTANCE, new CompoundTag()).getOrThrow(false, s -> {}).getFirst(), Functions.identity()),
            Either::right));

    public static final RegistryObject<Codec<IAction>> NONE = register("none", Codec.unit(IAction.NO_ACTION));
    public static final RegistryObject<Codec<IAction>> TELEPORT = registerDefault("teleport", TeleportAction::new, ChatFormatting.DARK_PURPLE);
    public static final RegistryObject<Codec<IAction>> SHIELD = registerDefault("shield", ShieldAction::new, ChatFormatting.AQUA);
    public static final RegistryObject<Codec<ExplodeAction>> SELF_DESTRUCT = register("self-destruct", RecordCodecBuilder.create(builder -> builder.group(
            name("self-destruct"),
            color(ChatFormatting.RED),
            Codec.FLOAT.optionalFieldOf("radius", 8f).forGetter(ExplodeAction::getRadius),
            Codec.FLOAT.optionalFieldOf("damage", 10f).forGetter(ExplodeAction::getDamage)
    ).apply(builder, ExplodeAction::new)));
    public static final RegistryObject<Codec<DashAction>> DASH = register("dash", RecordCodecBuilder.create(builder -> builder.group(
            name("dash"),
            color(ChatFormatting.GREEN),
            ExtraCodecs.intRange(0, 20).optionalFieldOf("impact", 0).forGetter(DashAction::getImpactStrength),
            Codec.FLOAT.optionalFieldOf("force", 0.5f).forGetter(DashAction::getForce),
            StringRepresentable.fromEnum(DashAction.DashDirection::values).optionalFieldOf("direction", DashAction.DashDirection.LOOK).forGetter(DashAction::getDirection)
    ).apply(builder, DashAction::new)));
    public static final RegistryObject<Codec<MobEffectAction>> MOB_EFFECT = register("effect", RecordCodecBuilder.create(builder -> builder.group(
            name("effect"),
            color(ChatFormatting.WHITE),
            Codec.list(EFFECT_CODEC).optionalFieldOf("effects", List.of()).forGetter(MobEffectAction::getEffects)
    ).apply(builder, MobEffectAction::new)));

    private static <A extends IAction> RegistryObject<Codec<A>> register(String name, Codec<A> actionSerializer) {
        return ACTION_SERIALIZERS.register(name, () -> actionSerializer);
    }

    private static <A extends IAction> RegistryObject<Codec<A>> registerDefault(String name, BiFunction<String, TextColor, A> constructor, ChatFormatting defaultColor) {
        Codec<A> codec = RecordCodecBuilder.create(builder -> builder.group(
                name(name),
                color(defaultColor)
        ).apply(builder, constructor));
        return register(name, codec);
    }

    private static <O extends IAction> RecordCodecBuilder<O, String> name(String defaultName) {
        return Codec.STRING.optionalFieldOf("name", defaultName).forGetter(IAction::toString);
    }

    private static <O extends IAction> RecordCodecBuilder<O, TextColor> color(ChatFormatting defaultColor) {
        return TextColor.CODEC.optionalFieldOf("displayColor", TextColor.fromLegacyFormat(defaultColor)).forGetter(IAction::getColor);
    }
}