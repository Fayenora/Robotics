package com.ignis.igrobotics.definitions;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import com.ignis.igrobotics.common.entity.StompedUpBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Robotics.MODID);

    public static final RegistryObject<EntityType<RobotEntity>> ROBOT = ENTITY_TYPES.register("robot",
            () -> EntityType.Builder.of((EntityType.EntityFactory<RobotEntity>) (type, level) -> new RobotEntity(level), MobCategory.MISC)
                    .sized(0.4f, 2)
                    .build(new ResourceLocation(Robotics.MODID, "robot").toString()));


    public static final RegistryObject<EntityType<StompedUpBlockEntity>> STOMPED_BLOCK = ENTITY_TYPES.register("stomped_block",
            () -> EntityType.Builder.of((EntityType.EntityFactory<StompedUpBlockEntity>) (type, level) -> new StompedUpBlockEntity(level), MobCategory.MISC)
                    .sized(1, 1).build(new ResourceLocation(Robotics.MODID, "stomped_block").toString()));

}
