package com.ignis.igrobotics.definitions;

import com.google.common.collect.Maps;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.Robotics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributes {

    public static final Attribute ENERGY_CAPACITY = new RangedAttribute("robot.energy_capacity", 0, 0, Double.MAX_VALUE).setSyncable(true);
    public static final Attribute ENERGY_CONSUMPTION = new RangedAttribute("robot.energy_consumption", 100, -Double.MAX_VALUE, Double.MAX_VALUE);
    public static final Attribute MODIFIER_SLOTS = new RangedAttribute("robot.module_slots", 1, 0, Reference.MAX_MODULES);
    public static final Attribute INVENTORY_SLOTS = new RangedAttribute("robot.inventory_slots", 12, 0, Reference.MAX_INVENTORY_SIZE).setSyncable(true);

    @SubscribeEvent
    public static void robotAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.ROBOT.get(), createRobotAttributes());
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeModificationEvent event) {
        event.add(ModEntityTypes.ROBOT.get(), ENERGY_CAPACITY);
        event.add(ModEntityTypes.ROBOT.get(), ENERGY_CONSUMPTION);
        event.add(ModEntityTypes.ROBOT.get(), MODIFIER_SLOTS);
        event.add(ModEntityTypes.ROBOT.get(), INVENTORY_SLOTS);
    }

    public static AttributeSupplier createRobotAttributes() {
        return LivingEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.3f).build();
    }

    @EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    class ModAttributeChanges {
        @SubscribeEvent
        public static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
            for(String attribute : event.getTo().getAttributeModifiers().keySet()) {
                onAttributeChanged(event.getEntity(), attribute);
            }
        }
    }

    public static void onAttributeChanged(LivingEntity entity, AttributeInstance attributeInstance) {
		//Common attributes
		if(instance.getAttribute().equals(ENERGY_CAPACITY)) {
            entity.getCapability(ForgeCapability.ENERGY_CAPABILITY).ifPresent(storage -> {
                    storage.setMaxEnergyStored(instance.getAttributeValue());
            });
		}

		if(instance.getAttribute().equals(INVENTORY_SLOTS)) {
            entity.getCapability(ForgeCapability.INVENTORY_CAPABILITY).ifPresent(inventory -> {
                    inventory.setInventorySize((int) instance.getAttributeValue());
            });
		}

		//Server side attributes
		if(entity.level.isClientSide()) return;

		if(instance.getAttribute().equals(MODIFIER_SLOTS)) {
            entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                    robot.setMaxModules((int) instance.getAttributeValue());
            });
		}
    }
}
