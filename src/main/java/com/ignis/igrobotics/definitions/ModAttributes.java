package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.core.capabilities.ModCapabilities;
import com.ignis.igrobotics.core.capabilities.energy.ModifiableEnergyStorage;
import com.ignis.igrobotics.core.robot.EnumModuleSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributes {

    public static final Attribute ENERGY_CAPACITY = register("robot.energy_capacity", 1000000, 0, Double.MAX_VALUE, true);
    public static final Attribute ENERGY_CONSUMPTION = register("robot.energy_consumption", 100, -Double.MAX_VALUE, Double.MAX_VALUE, false);
    public static final Attribute MODIFIER_SLOTS = register("robot.module_slots", 2, 0, Reference.MAX_MODULES, false);
    public static final Attribute INVENTORY_SLOTS = register("robot.inventory_slots", 12, 0, Reference.MAX_INVENTORY_SIZE, true);

    private static Attribute register(String name, double defaultValue, double min, double max, boolean syncable) {
        Attribute attr = new RangedAttribute("attribute.name." + name, defaultValue, min, max).setSyncable(syncable);
        ForgeRegistries.ATTRIBUTES.register(new ResourceLocation(Robotics.MODID, name), attr);
        return attr;
    }

    @SubscribeEvent
    public static void robotAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.ROBOT.get(), createRobotAttributes());
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeModificationEvent event) {
        event.add(ModEntityTypes.ROBOT.get(), ENERGY_CAPACITY);
        event.add(ModEntityTypes.ROBOT.get(), ENERGY_CONSUMPTION);
        event.add(ModEntityTypes.ROBOT.get(), MODIFIER_SLOTS);
    }

    public static AttributeSupplier createRobotAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.35f).build();
    }

    @Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    static class ModAttributeChanges {
        @SubscribeEvent
        public static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
            for(Attribute attribute : event.getTo().getAttributeModifiers(event.getSlot()).keys()) {
                onAttributeChanged(event.getEntity(), event.getEntity().getAttribute(attribute));
            }
        }
    }

    public static void onAttributeChanged(LivingEntity entity, @Nullable AttributeInstance instance) {
        if(instance == null) return;
		//Common attributes
		if(instance.getAttribute().equals(ENERGY_CAPACITY)) {
            entity.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
                if(storage instanceof ModifiableEnergyStorage energy) {
                    energy.setMaxEnergyStored((int) instance.getValue());
                }
            });
		}

		//Server side attributes
		if(entity.level.isClientSide()) return;

		if(instance.getAttribute().equals(MODIFIER_SLOTS)) {
            entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                robot.setMaxModules(EnumModuleSlot.DEFAULT, (int) instance.getValue());
            });
		}
    }
}
