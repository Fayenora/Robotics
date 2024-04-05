package com.ignis.igrobotics.core.capabilities.inventory;

import com.ignis.igrobotics.core.util.InventoryUtil;
import com.ignis.igrobotics.definitions.ModAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.EntityHandsInvWrapper;

public class RobotInventory extends CombinedInvWrapper implements INBTSerializable<CompoundTag> {

    private final LivingEntity entity;
    private final BaseInventory storageInventory;

    public RobotInventory(LivingEntity entity) {
        super(new EntityHandsInvWrapper(entity), new RobotArmorInvWrapper(entity), new BaseInventory(entity::blockPosition, (int) ModAttributes.INVENTORY_SLOTS.getDefaultValue()));
        this.entity = entity;
        storageInventory = (BaseInventory) itemHandler[2];
    }

    public void dropItems() {
        //Apply Vanishing Curse
        for(int i = 0; i < getSlots(); ++i) {
            ItemStack itemstack = getStackInSlot(i);
            if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
                setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        for(int i = 0; i < getSlots(); i++) {
            InventoryUtil.dropItem(entity, getStackInSlot(i));
        }
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
