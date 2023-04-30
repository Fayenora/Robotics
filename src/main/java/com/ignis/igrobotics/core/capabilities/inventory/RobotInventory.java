package com.ignis.igrobotics.core.capabilities.inventory;

import com.ignis.igrobotics.definitions.ModAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.EntityHandsInvWrapper;

public class RobotInventory extends CombinedInvWrapper implements INBTSerializable<CompoundTag> {

    private BaseInventory storageInventory;

    public RobotInventory(LivingEntity entity) {
        super(new EntityHandsInvWrapper(entity), new RobotArmorInvWrapper(entity), new BaseInventory(entity::blockPosition, (int) ModAttributes.INVENTORY_SLOTS.getDefaultValue()));
        storageInventory = (BaseInventory) itemHandler[2];
    }

    @Override
    public CompoundTag serializeNBT() {
        return storageInventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        storageInventory.deserializeNBT(nbt);
    }
}
