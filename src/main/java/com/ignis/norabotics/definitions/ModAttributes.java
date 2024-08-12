package com.ignis.norabotics.definitions;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.capabilities.ModCapabilities;
import com.ignis.norabotics.common.capabilities.ModifiableEnergyStorage;
import com.ignis.norabotics.common.helpers.util.InventoryUtil;
import com.ignis.norabotics.common.robot.EnumModuleSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Robotics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributes {

    public static final Attribute ENERGY_CAPACITY = register("robot.energy_capacity", 1000000, 0, Double.MAX_VALUE, true);
    public static final Attribute ENERGY_CONSUMPTION = register("robot.energy_consumption", 30, -Double.MAX_VALUE, Double.MAX_VALUE, false);
    public static final List<Attribute> MODIFIER_SLOTS = new ArrayList<>(EnumModuleSlot.values().length);
    public static final Attribute STACK_SIZE = register("robot.stack_size", 1, 0, 6, true); // Multiplier value -> more is better
    public static final Attribute LOGISTICS_TIME = register("robot.logistics_time", 1, 0.2, 10, false); // Time multiplier for logistics operations -> less is better
    public static final Attribute MODULE_COST = register("robot.module_cost", 1, 0, 10, false); // Cost multiplier -> less is better
    public static final Attribute MODULE_COOLDOWN = register("robot.module_cooldown", 1, 0.2, 10, false); // Cooldown multiplier -> less is better
    public static final Attribute MODULE_DURATION = register("robot.module_duration", 1, 0, 10, false); // Duration multiplier -> more is better
    public static final Attribute INVENTORY_SLOTS = register("robot.inventory_slots", 12, 0, Reference.MAX_INVENTORY_SIZE, true);

    static {
        for(EnumModuleSlot slotType : EnumModuleSlot.values()) {
            MODIFIER_SLOTS.add(register("robot.slots.modules." + slotType.name().toLowerCase(), 0, 0, Reference.MAX_MODULES, false));
        }
    }

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
        event.add(ModEntityTypes.ROBOT.get(), STACK_SIZE);
        event.add(ModEntityTypes.ROBOT.get(), MODULE_COST);
        event.add(ModEntityTypes.ROBOT.get(), MODULE_COOLDOWN);
        event.add(ModEntityTypes.ROBOT.get(), MODULE_DURATION);
        for(Attribute attribute : MODIFIER_SLOTS) {
            event.add(ModEntityTypes.ROBOT.get(), attribute);
        }
    }

    public static AttributeSupplier createRobotAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.05f).build();
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
		if(entity.level().isClientSide()) return;

		if(MODIFIER_SLOTS.contains(instance.getAttribute())) {
            entity.getCapability(ModCapabilities.ROBOT).ifPresent(robot -> {
                String[] nameComponents = instance.getAttribute().getDescriptionId().split("\\.");
                EnumModuleSlot slotType = EnumModuleSlot.valueOf(nameComponents[nameComponents.length - 1].toUpperCase());
                robot.setMaxModules(slotType, (int) instance.getValue());
            });
		}

        if(instance.getAttribute().equals(STACK_SIZE)) {
            entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                for(int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if(stack.getCount() <= inventory.getSlotLimit(i)) continue;
                    ItemStack difference = inventory.extractItem(i, stack.getCount() - inventory.getSlotLimit(i), false);
                    InventoryUtil.dropItem(entity, difference);
                }
            });
        }
    }
}
