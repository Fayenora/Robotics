package com.ignis.igrobotics.common.capabilities.impl.inventory;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.items.wrapper.EntityArmorInvWrapper;
import org.jetbrains.annotations.NotNull;

public class RobotArmorInvWrapper extends EntityArmorInvWrapper {

    public RobotArmorInvWrapper(LivingEntity entity) {
        super(entity);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return switch(slot) {
            case 3 -> stack.canEquip(EquipmentSlot.HEAD, entity);
            case 2 -> stack.canEquip(EquipmentSlot.CHEST, entity);
            case 1 -> stack.canEquip(EquipmentSlot.LEGS, entity);
            case 0 -> stack.canEquip(EquipmentSlot.FEET, entity);
            default -> true;
        };
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(slot < 4 && EnchantmentHelper.hasBindingCurse(getStackInSlot(slot))) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }
}
