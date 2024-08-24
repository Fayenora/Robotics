package com.ignis.norabotics.common.robot;

import com.google.gson.JsonSyntaxException;
import com.ignis.norabotics.common.capabilities.IPerkMap;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.capabilities.impl.perk.PerkMap;
import com.ignis.norabotics.common.content.actions.IAction;
import com.ignis.norabotics.common.content.events.ModuleActivationEvent;
import com.ignis.norabotics.definitions.ModActions;
import com.ignis.norabotics.definitions.ModAttributes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class RobotModule {

    public static final Codec<Ingredient> INGREDIENT_CODEC = ExtraCodecs.JSON.comapFlatMap(json -> {
        try {
            return DataResult.success(Ingredient.fromJson(json));
        } catch(JsonSyntaxException e) {
            return DataResult.success(Ingredient.EMPTY);
        }
    }, Ingredient::toJson);
    public static final Codec<Ingredient> NETWORK_INGREDIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ItemStack.CODEC).fieldOf("stacks").forGetter(i -> Arrays.asList(i.getItems()))
    ).apply(instance, l -> Ingredient.of(l.stream())));
    public static final Codec<RobotModule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            INGREDIENT_CODEC.fieldOf("items").forGetter(RobotModule::getItems),
            Codec.list(StringRepresentable.fromEnum(EnumModuleSlot::values)).optionalFieldOf("slots", List.of()).forGetter(c -> c.getViableSlots().stream().toList()),
            ModActions.CODEC.optionalFieldOf("action", IAction.NO_ACTION).forGetter(RobotModule::getAction),
            ExtraCodecs.intRange(0, Integer.MAX_VALUE).optionalFieldOf("cooldown", 0).forGetter(RobotModule::getCooldown),
            ExtraCodecs.intRange(0, Integer.MAX_VALUE).optionalFieldOf("duration", 0).forGetter(RobotModule::getDuration),
            Codec.INT.optionalFieldOf("energyCost", 0).forGetter(RobotModule::getEnergyCost),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(c -> Optional.ofNullable(c.overlay)),
            PerkMap.CODEC.optionalFieldOf("perks", new PerkMap()).forGetter(c -> (PerkMap) c.perks)
    ).apply(instance, RobotModule::initialize));
    public static final Codec<RobotModule> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NETWORK_INGREDIENT_CODEC.fieldOf("items").forGetter(RobotModule::getItems),
            Codec.list(StringRepresentable.fromEnum(EnumModuleSlot::values)).optionalFieldOf("slots", List.of()).forGetter(c -> c.getViableSlots().stream().toList()),
            ModActions.CODEC.optionalFieldOf("action", IAction.NO_ACTION).forGetter(RobotModule::getAction),
            ExtraCodecs.intRange(0, Integer.MAX_VALUE).optionalFieldOf("cooldown", 0).forGetter(RobotModule::getCooldown),
            ExtraCodecs.intRange(0, Integer.MAX_VALUE).optionalFieldOf("duration", 0).forGetter(RobotModule::getDuration),
            Codec.INT.optionalFieldOf("energyCost", 0).forGetter(RobotModule::getEnergyCost),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(c -> Optional.ofNullable(c.overlay)),
            PerkMap.CODEC.optionalFieldOf("perks", new PerkMap()).forGetter(c -> (PerkMap) c.perks)
    ).apply(instance, RobotModule::initialize));

    private final Ingredient item;
    private IPerkMap perks = new PerkMap();
    private EnumSet<EnumModuleSlot> viableSlots = EnumSet.noneOf(EnumModuleSlot.class);
    private IAction action;
    /** A cooldown of 0 indicates a passive module */
    private int cooldown = 0;
    /** How long this module applies an effect, if it is active */
    private int duration = 0;
    /** The cost to activate the module. If the module is passive this is the energy cost per tick */
    private int energyCost = 0;
    /** Overlay to be applied to the robot */
    private ResourceLocation overlay;

    protected RobotModule(Ingredient item) {
        this.item = item;
        action = IAction.NO_ACTION;
    }

    public boolean activate(LivingEntity caster) {
        if(action == IAction.NO_ACTION) return false;
        if(isOnCooldown(caster)) return false;
        IEnergyStorage energyStorage = null;
        int modEnergyCost = (int) (energyCost * caster.getAttributeValue(ModAttributes.MODULE_COST));
        if(modEnergyCost > 0) {
            if(!caster.getCapability(ForgeCapabilities.ENERGY).isPresent()) return false;
            energyStorage = caster.getCapability(ForgeCapabilities.ENERGY).resolve().get();
            if(energyStorage.getEnergyStored() < modEnergyCost) return false;
        }
        if(MinecraftForge.EVENT_BUS.post(new ModuleActivationEvent(caster, this))) return false;
        if(!action.execute(caster, (int) (duration * caster.getAttributeValue(ModAttributes.MODULE_DURATION)))) return false;
        if(modEnergyCost > 0) {
            energyStorage.extractEnergy(modEnergyCost, false);
        }
        setOnCooldown(caster);
        return true;
    }

    public boolean isOnCooldown(LivingEntity caster) {
        if(!caster.getCapability(ModCapabilities.PERKS).isPresent()) return true;
        IPerkMap perkMap = caster.getCapability(ModCapabilities.PERKS).resolve().get();
        if(!perkMap.values().contains(action.toString())) return false;
        if(caster.tickCount < perkMap.values().get(action.toString())) return false; // Server was loaded anew -> All perk cooldowns reset
        return caster.tickCount <= perkMap.values().get(action.toString()) + getCooldown() * caster.getAttributeValue(ModAttributes.MODULE_COOLDOWN);
    }

    private void setOnCooldown(LivingEntity caster) {
        caster.getCapability(ModCapabilities.PERKS).ifPresent(perks -> {
            perks.values().set(action.toString(), caster.tickCount);
        });
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RobotModule module) {
            return item.equals(module.item);
        }
        return false;
    }

    @LuaFunction
    public int getCooldown() {
        return cooldown;
    }

    @LuaFunction
    public int getEnergyCost() {
        return energyCost;
    }

    @LuaFunction
    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return item.getItems().length >= 1 ? item.getItems()[0].getItem().toString() : "";
    }

    public Ingredient getItems() {
        return item;
    }

    public IPerkMap getPerks() {
        return perks;
    }

    public ResourceLocation getOverlay() {
        return overlay;
    }

    public boolean hasOverlay() {
        return overlay != null;
    }

    public EnumSet<EnumModuleSlot> getViableSlots() {
        return viableSlots;
    }

    public IAction getAction() {
        return action;
    }

    private static RobotModule initialize(Ingredient items, List<EnumModuleSlot> slots, IAction action, int cooldown, int duration, int energyCost,
                                          Optional<ResourceLocation> overlay, IPerkMap perks) {
        RobotModule module = new RobotModule(items);
        module.viableSlots = slots.isEmpty() ? EnumSet.noneOf(EnumModuleSlot.class) : EnumSet.copyOf(slots);
        module.action = action;
        module.cooldown = cooldown;
        module.duration = duration;
        module.energyCost = energyCost;
        overlay.ifPresent(resourceLocation -> module.overlay = resourceLocation);
        module.perks = perks;
        return module;
    }

    @Override
    protected RobotModule clone() {
        RobotModule newModule = new RobotModule(item);
        newModule.viableSlots = this.viableSlots.clone();
        newModule.action = this.action;
        newModule.duration = this.duration;
        newModule.energyCost = this.energyCost;
        newModule.cooldown = this.cooldown;
        newModule.overlay = this.overlay;
        newModule.perks = PerkMap.copy(this.perks);
        return newModule;
    }

    public RobotModule merge(RobotModule other) {
        RobotModule thisModule = clone();
        if(thisModule.action == IAction.NO_ACTION) {
            thisModule.action = other.action;
            thisModule.duration = other.duration;
            thisModule.energyCost = other.energyCost;
            thisModule.cooldown = other.cooldown;
        }
        thisModule.overlay = overlay == null ? other.overlay : thisModule.overlay;
        thisModule.viableSlots.addAll(other.viableSlots);
        thisModule.perks.merge(other.getPerks());
        return thisModule;
    }

    public static class ModuleBuilder {
        private final RobotModule module;

        public ModuleBuilder(Ingredient ingredient) {
            this.module = new RobotModule(ingredient);
        }

        public ModuleBuilder addPerk(Perk perk, int level) {
            module.perks.add(perk, level);
            return this;
        }

        public RobotModule build() {
            return module;
        }
    }
}
