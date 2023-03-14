package com.ignis.igrobotics.client.entity;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RobotModel extends GeoModel<RobotEntity> {
    @Override
    public ResourceLocation getModelResource(RobotEntity animatable) {
        return new ResourceLocation(Robotics.MODID, "geo/robot.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RobotEntity animatable) {
        return new ResourceLocation(Robotics.MODID, "textures/robot/default_robot.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RobotEntity animatable) {
        return new ResourceLocation(Robotics.MODID, "animations/robot.animation.json");
    }
}
