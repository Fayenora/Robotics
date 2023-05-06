package com.ignis.igrobotics.client.rendering;

import com.ignis.igrobotics.Robotics;
import com.ignis.igrobotics.common.entity.RobotEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class RobotModel extends DefaultedEntityGeoModel<RobotEntity> {
    public RobotModel() {
        super(new ResourceLocation(Robotics.MODID, "robot"), true);
    }

    @Override
    protected String subtype() {
        return "robot";
    }
}
