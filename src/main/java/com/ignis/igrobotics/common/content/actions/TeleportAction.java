package com.ignis.igrobotics.common.content.actions;

import com.ignis.igrobotics.definitions.ModActions;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class TeleportAction implements IAction {

    public static final int DISTANCE = 5;
    private final String name;
    private final TextColor color;

    public TeleportAction(String name, TextColor color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public boolean execute(LivingEntity caster, int duration) {
        Level level = caster.level();
        if(level.isClientSide || !caster.isAlive()) {
            return false;
        }
        if(caster instanceof Player) {
            Vec3 source = caster.getEyePosition();
            Vec3 target = caster.getEyePosition().add(caster.getLookAngle().normalize().scale(DISTANCE));
            Vec3 blockHit = level.clip(new ClipContext(source, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, caster)).getLocation();
            caster.teleportTo(blockHit.x, blockHit.y - caster.getEyeHeight(), blockHit.z);
            if (!caster.isSilent()) {
                level.playSound(null, caster.xo, caster.yo, caster.zo, SoundEvents.ENDERMAN_TELEPORT, caster.getSoundSource(), 1.0F, 1.0F);
                caster.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            }
        } else {
            double d0 = caster.getX() + (caster.getRandom().nextDouble() - 0.5D) * DISTANCE;
            double d1 = caster.getY() + (double)(caster.getRandom().nextInt(DISTANCE) - DISTANCE / 2);
            double d2 = caster.getZ() + (caster.getRandom().nextDouble() - 0.5D) * DISTANCE;
            teleport(caster, d0, d1, d2);
        }
        return true;
    }

    @Override
    public Codec<? extends IAction> codec() {
        return ModActions.TELEPORT.get();
    }

    public static boolean teleport(LivingEntity entity, double p_32544_, double p_32545_, double p_32546_) {
        Level level = entity.level();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(p_32544_, p_32545_, p_32546_);

        while(blockpos$mutableblockpos.getY() > level.getMinBuildHeight() && !level.getBlockState(blockpos$mutableblockpos).blocksMotion()) {
            blockpos$mutableblockpos.move(Direction.DOWN);
        }

        BlockState blockstate = level.getBlockState(blockpos$mutableblockpos);
        if (blockstate.blocksMotion() && !blockstate.getFluidState().is(FluidTags.WATER)) {
            net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(entity, p_32544_, p_32545_, p_32546_);
            if (event.isCanceled()) return false;
            Vec3 vec3 = entity.position();
            if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(entity));
                if (!entity.isSilent()) {
                    level.playSound(null, entity.xo, entity.yo, entity.zo, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
                    entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public TextColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}
