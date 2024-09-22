package com.io.norabotics.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerOpenersCounter.class)
public class OpenerCounter {

    @Shadow
    private int openCount;

    /**
     * Vanilla regularly rechecks how many players opened a container. However, it only considers players capable of opening things.
     * Solution: Prevent the rechecks by not actually calculating how many players are standing around the container and viewing the menu,
     * but simply returning the count gained from {@link ContainerOpenersCounter#incrementOpeners(Player, Level, BlockPos, BlockState)} & {@link ContainerOpenersCounter#decrementOpeners(Player, Level, BlockPos, BlockState)}
     * This prevents any actual rechecks in {@link ContainerOpenersCounter#recheckOpeners(Level, BlockPos, BlockState)}
     *
     * @param level the world
     * @param pos   the position of the container
     * @param cir   CallbackInfo provided by mixin
     * @author io.44
     */
    @Inject(method = "getOpenCount", at = @At("HEAD"), cancellable = true)
    private void robotics$getOpenCount(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(openCount);
    }
}
