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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

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
     * Solution: We alter the getCapability call to return the basic attached capabilities in case of robots
     * NOTE: THIS HAS TO BE KEPT UP TO DATE WITH VANILLA CODE
     * @param capability the capability requested
     * @param facing the direction to interface from
     * @param <T> capability interface
     */
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        //ROBOTICS START
        if(capability != ModCapabilities.ROBOT && getCapability(ModCapabilities.ROBOT).isPresent()) {
            return super.getCapability(capability, facing);
        }
        //ROBOTICS END
        if (this.isAlive() && capability == ForgeCapabilities.ITEM_HANDLER) {
            if (facing == null) return handlers[2].cast();
            else if (facing.getAxis().isVertical()) return handlers[0].cast();
            else if (facing.getAxis().isHorizontal()) return handlers[1].cast();
        }
        return super.getCapability(capability, facing);
    }

}
