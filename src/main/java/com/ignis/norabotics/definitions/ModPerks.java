package com.ignis.norabotics.definitions;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.impl.perk.Perk;
import com.ignis.norabotics.common.content.perks.*;
import com.ignis.norabotics.common.content.perks.modules.PerkFist;
import com.ignis.norabotics.common.content.perks.modules.PerkGenerator;
import com.ignis.norabotics.common.content.perks.modules.PerkSolarPanel;
import com.ignis.norabotics.common.content.perks.modules.PerkUnarmedAttack;
import com.ignis.norabotics.integration.config.RoboticsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModPerks {

    public static final ResourceKey<Registry<Perk>> KEY = ResourceKey.createRegistryKey(new ResourceLocation(Robotics.MODID, "perks"));
    public static final DeferredRegister<Perk> PERKS = DeferredRegister.create(KEY, Robotics.MODID);
    public static final Supplier<IForgeRegistry<Perk>> REGISTRY = PERKS.makeRegistry(() -> new RegistryBuilder<Perk>().allowModification().disableSaving().disableSync().setDefaultKey(new ResourceLocation(Robotics.MODID, "undefined")));

    public static final RegistryObject<Perk> PERK_UNDEFINED = register("undefined", s -> new Perk(s, 1).setDisplayColor(ChatFormatting.RED));
    public static final RegistryObject<Perk> PERK_HEALTH = register("health", Perk::new);
    public static final RegistryObject<Perk> PERK_ARMOR = register("durable", Perk::new);
    public static final RegistryObject<Perk> PERK_TOUGHNESS = register("persistent", Perk::new);
    public static final RegistryObject<Perk> PERK_MODULAR = register("modular", Perk::new);
    public static final RegistryObject<Perk> PERK_STEADY = register("steady", Perk::new);
    public static final RegistryObject<Perk> PERK_EFFICIENT = register("efficient", Perk::new);
    public static final RegistryObject<Perk> PERK_ACCELERATION = register("acceleration", PerkAcceleration::new);
    public static final RegistryObject<Perk> PERK_CHARGE = register("charge", PerkCharge::new);
    public static final RegistryObject<Perk> PERK_SOLAR_PANEL = register("solar_panel", PerkSolarPanel::new);
    public static final RegistryObject<Perk> PERK_UNRELIABLE = register("unreliable", PerkUnreliable::new);
    public static final RegistryObject<Perk> PERK_MASS_PRODUCED = register("mass_produced", PerkMassProduced::new);
    public static final RegistryObject<Perk> PERK_IMPACT = register("impact", PerkImpact::new);
    public static final RegistryObject<Perk> PERK_ROBUST = register("robust", PerkRobust::new);
    public static final RegistryObject<Perk> PERK_LUMINOUS = register("luminous", Perk::new);
    //public static final RegistryObject<Perk> PERK_GLOWING = register("glowing", PerkGlowing::new);
    public static final RegistryObject<Perk> PERK_COAL_GENERATOR = registerGenerator("coal_generator",RoboticsConfig.general.coalGeneratorRate, (stack, ent) -> stack.getItem().equals(Items.COAL) ? 8000 : 0);
    public static final RegistryObject<Perk> PERK_BIO_GENERATOR = registerGenerator("bio_generator", RoboticsConfig.general.bioGeneratorRate, (stack, ent) -> {
        FoodProperties stats = stack.getItem().getFoodProperties(stack, ent);
        if(stats == null) return 0;
        return (int) (stats.getNutrition() * 200 + Math.pow(stats.getSaturationModifier(), 1.5) * 100);
    });
    public static final RegistryObject<Perk> PERK_UNARMED_ATTACK = register("unarmed_attack", PerkUnarmedAttack::new);
    public static final RegistryObject<Perk> PERK_FIST = register("fist", PerkFist::new);
    public static final RegistryObject<Perk> PERK_SHIELD = register("shield", Perk::new);

    public static final RegistryObject<Perk> PERK_INVULNERABILITY = register("invulnerability", PerkInvulnerability::new);
    public static final RegistryObject<Perk> PERK_VULNERABILITY = register("vulnerability", PerkVulnerability::new);
    public static final RegistryObject<Perk> PERK_VOIDANT = register("voidant", PerkVoidant::new);
    public static final RegistryObject<Perk> PERK_THERMAL_CONDUCTIVITY = register("thermal_conductivity", PerkThermalConductivity::new);
    public static final RegistryObject<Perk> PERK_ELECTRICAL_CONDUCTIVITY = register("electrical_conductivity", PerkElectricalConductivity::new);
    public static final RegistryObject<Perk> PERK_STACKED = register("stacked", Perk::new);
    public static final RegistryObject<Perk> PERK_LOGISTICIAN = register("logistician", Perk::new);
    public static final RegistryObject<Perk> PERK_REFLECTIVE = register("reflective", PerkReflective::new);
    public static final RegistryObject<Perk> PERK_PRECIOUS = register("precious", PerkPrecious::new);
    public static final RegistryObject<Perk> PERK_MAGNETIC = register("magnetic", PerkMagnetic::new);
    public static final RegistryObject<Perk> PERK_CORRODABLE = register("corrodable", Perk::new);
    public static final RegistryObject<Perk> PERK_CONNEXIOM = register("connexiom", PerkConnexiom::new);
    public static final RegistryObject<Perk> PERK_LINKED = register("linked", PerkLinked::new);
    public static final RegistryObject<Perk> PERK_ATTRACTANT = register("attractant", PerkAttractant::new);
    public static final RegistryObject<Perk> PERK_INSPIRE = register("inspire", PerkInspire::new);
    public static final RegistryObject<Perk> PERK_MODULE_BUFF = register("module_buff", PerkModuleBuff::new);
    public static final RegistryObject<Perk> PERK_MODULE_EFFICIENCY = register("module_efficiency", Perk::new);
    public static final RegistryObject<Perk> PERK_MODULE_COOLDOWN = register("module_cooldown", Perk::new);
    public static final RegistryObject<Perk> PERK_ARMOR_SHRED = register("armor_shred", PerkArmorShred::new);

    private static RegistryObject<Perk> registerGenerator(String name, Supplier<Integer> generationRate, BiFunction<ItemStack, Mob, Integer> validInputs) {
        return PERKS.register(name, () -> new PerkGenerator(name, generationRate, validInputs));
    }

    private static RegistryObject<Perk> register(String name, Function<String, Perk> perk) {
        return PERKS.register(name, () -> perk.apply(name));
    }
}
