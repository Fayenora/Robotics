package com.ignis.norabotics.client.rendering;

import com.ignis.norabotics.Robotics;
import com.ignis.norabotics.common.content.entity.RobotEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class RobotModel extends DefaultedEntityGeoModel<RobotEntity> {
    public RobotModel() {
        super(new ResourceLocation(Robotics.MODID, "robot"), true);
    }

    @Override
    protected String subtype() {
        return "robot";
    }
}
