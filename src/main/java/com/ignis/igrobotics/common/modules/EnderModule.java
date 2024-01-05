package com.ignis.igrobotics.common.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class EnderModule implements IModuleAction {

    public static final int DISTANCE = 5;

    @Override
    public void execute(LivingEntity caster, int energyCost, int duration) {
        if(caster.level.isClientSide || !caster.isAlive()) {
            return;
        }
        if(caster instanceof Player) {
            Vec3 source = caster.getEyePosition();
            Vec3 target = caster.getEyePosition().add(caster.getLookAngle().normalize().scale(DISTANCE));
            Vec3 blockHit = caster.level.clip(new ClipContext(source, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, caster)).getLocation();
            caster.teleportTo(blockHit.x, blockHit.y - caster.getEyeHeight(), blockHit.z);
            if (!caster.isSilent()) {
                caster.level.playSound(null, caster.xo, caster.yo, caster.zo, SoundEvents.ENDERMAN_TELEPORT, caster.getSoundSource(), 1.0F, 1.0F);
                caster.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
            return;
        } else {
            double d0 = caster.getX() + (caster.getRandom().nextDouble() - 0.5D) * 64.0D;
            double d1 = caster.getY() + (double)(caster.getRandom().nextInt(64) - 32);
            double d2 = caster.getZ() + (caster.getRandom().nextDouble() - 0.5D) * 64.0D;
            teleport(caster, d0, d1, d2);
        }
    }

    public static boolean teleport(LivingEntity entity, double p_32544_, double p_32545_, double p_32546_) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(p_32544_, p_32545_, p_32546_);

        while(blockpos$mutableblockpos.getY() > entity.level.getMinBuildHeight() && !entity.level.getBlockState(blockpos$mutableblockpos).getMaterial().blocksMotion()) {
            blockpos$mutableblockpos.move(Direction.DOWN);
        }

        BlockState blockstate = entity.level.getBlockState(blockpos$mutableblockpos);
        boolean flag = blockstate.getMaterial().blocksMotion();
        boolean flag1 = blockstate.getFluidState().is(FluidTags.WATER);
        if (flag && !flag1) {
            net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(entity, p_32544_, p_32545_, p_32546_);
            if (event.isCanceled()) return false;
            Vec3 vec3 = entity.position();
            boolean flag2 = entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
            if (flag2) {
                entity.level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(entity));
                if (!entity.isSilent()) {
                    entity.level.playSound(null, entity.xo, entity.yo, entity.zo, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
                    entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }
            return flag2;
        } else {
            return false;
        }
    }
}
