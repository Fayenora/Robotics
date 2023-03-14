package com.ignis.igrobotics.common.entity;

import com.ignis.igrobotics.Robotics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Robotics.MODID);

    public static final RegistryObject<EntityType<RobotEntity>> ROBOT = ENTITY_TYPES.register("robot",
            () -> EntityType.Builder.of(RobotEntity::new, MobCategory.MISC)
                    .sized(0.4f, 2)
                    .build(new ResourceLocation(Robotics.MODID, "robot").toString()));
}
