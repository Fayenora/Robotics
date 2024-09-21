package com.io.norabotics.mixins;

import com.io.norabotics.common.capabilities.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class EntityInventory extends Entity {

    public EntityInventory(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Shadow(remap = false)
    private net.minecraftforge.common.util.LazyOptional<?>[] handlers;
    @Shadow
    public abstract boolean isAlive();

    /**
     * Prevent vanilla from always returning the armor and hands inventory for all entities.
     * In the case of robots, we want to return a larger inventory, which is not possible with Forge, as attaching another inventory works,
     * but all getCapability calls return the default inventory due to the if(capability == ITEM_HANDLER) clause in Vanilla Forge.
     * Solution: We catch all getCapability calls asking for and Item Handler and - in case of robots - return the basic attached capabilities
     *
     * @param capability the capability requested
     * @param facing     the direction to interface from
     * @param cir        CallbackInfo provided by mixin
     * @param <T>        capability interface
     */
    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true, remap = false)
    public <T> void redirectCap(Capability<T> capability, Direction facing, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if(capability == ForgeCapabilities.ITEM_HANDLER && getCapability(ModCapabilities.ROBOT).isPresent()) {
            cir.setReturnValue(super.getCapability(capability, facing));
        }
    }

}
