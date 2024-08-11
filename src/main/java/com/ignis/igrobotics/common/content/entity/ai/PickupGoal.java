package com.ignis.igrobotics.common.content.entity.ai;

import com.ignis.igrobotics.common.helpers.types.EntitySearch;
import com.ignis.igrobotics.common.helpers.util.InventoryUtil;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class PickupGoal extends FollowGoal {

    public PickupGoal(Mob follower, float area) {
        super(follower, EntitySearch.searchForNone(), 0);
        this.areaSize = area;
    }

    @Override
    public boolean canUse() {
        if(followingEntityCache == null) {
            Collection<ItemEntity> itemsInArea = entity.level().getEntitiesOfClass(ItemEntity.class, entity.getBoundingBox().inflate(areaSize));
            double closest = Double.MAX_VALUE;

            for(ItemEntity item : itemsInArea) {
                double distance = entity.distanceTo(item);
                if(distance < closest && canPickUp(entity, item.getItem())) {
                    closest = distance;
                    followingEntityCache = item;
                }
            }
        }

        if(followingEntityCache != null && entity.distanceTo(followingEntityCache) > areaSize) {
            followingEntityCache = null; //Invalidate the cache if the robot/item has for some reason left the vicinity
        }
        return followingEntityCache != null;
    }

    private boolean canPickUp(Mob mob, ItemStack stack) {
        if(!mob.wantsToPickUp(stack)) return false;
        AtomicBoolean hasSpace = new AtomicBoolean(false);
        entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
            hasSpace.set(InventoryUtil.insert(inventory, stack, true).isEmpty());
        });
        return hasSpace.get();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PickupGoal pickupGoal)) return false;
        return entity.equals(pickupGoal.entity);
    }
}
