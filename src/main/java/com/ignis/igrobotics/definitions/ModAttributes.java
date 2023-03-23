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
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0f)
                .add(Attributes.ATTACK_SPEED, 1.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.3f).build();
    }

    public static Builder createMobAttributes() {
        return new Builder()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(ForgeMod.SWIM_SPEED.get())
                .add(ForgeMod.NAMETAG_DISTANCE.get())
                .add(ForgeMod.ENTITY_GRAVITY.get())
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get())
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ATTACK_KNOCKBACK);
    }

    public static void onAttributeChanged(AttributeInstance attributeInstance) {

    }

    public static class Builder {
        private final Map<Attribute, AttributeInstance> builder = Maps.newHashMap();
        private boolean instanceFrozen;
        private final java.util.List<AttributeSupplier.Builder> others = new java.util.ArrayList<>();

        public boolean hasAttribute(Attribute attribute) {
            return this.builder.containsKey(attribute);
        }

        private AttributeInstance create(Attribute pAttribute) {
            AttributeInstance attributeinstance = new AttributeInstance(pAttribute, (p_258260_) -> {
                if (this.instanceFrozen) {
                    throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + BuiltInRegistries.ATTRIBUTE.getKey(pAttribute));
                }
            });
            this.builder.put(pAttribute, attributeinstance);
            return attributeinstance;
        }

        public Builder add(Attribute pAttribute) {
            this.create(pAttribute);
            return this;
        }

        public Builder add(Attribute pAttribute, double pValue) {
            AttributeInstance attributeinstance = this.create(pAttribute);
            attributeinstance.setBaseValue(pValue);
            return this;
        }

        public AttributeSupplier build() {
            this.instanceFrozen = true;
            return new AttributeSupplier(this.builder) {
                @Nullable
                @Override
                public AttributeInstance createInstance(Consumer<AttributeInstance> pOnChangedCallback, Attribute pAttribute) {
                    return super.createInstance(pOnChangedCallback.andThen(ModAttributes::onAttributeChanged), pAttribute);
                }
            };
        }
    }
}
