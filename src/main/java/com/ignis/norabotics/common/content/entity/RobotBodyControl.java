package com.ignis.norabotics.common.content.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

/**
 * Because Mojang is stupid BodyRotationControl almost only has private fields and everything needs to be reimplemented
 * only to enable completely free body & head rotation.
 * NOTE: This code is copied from {@link BodyRotationControl}. Ensure Topicality.
 */
public class RobotBodyControl extends BodyRotationControl {

    private final Mob mob;

    public RobotBodyControl(Mob p_24879_) {
        super(p_24879_);
        this.mob = p_24879_;
    }

    @Override
    public void clientTick() {
        if (isMoving()) {
            this.mob.yBodyRot = this.mob.getYRot();
            this.rotateHeadIfNecessary();
        }
    }

    private void rotateHeadIfNecessary() {
        this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float)this.mob.getMaxHeadYRot());
    }

    private boolean isMoving() {
        double d0 = this.mob.getX() - this.mob.xo;
        double d1 = this.mob.getZ() - this.mob.zo;
        return d0 * d0 + d1 * d1 > (double)2.5000003E-7F;
    }
}
