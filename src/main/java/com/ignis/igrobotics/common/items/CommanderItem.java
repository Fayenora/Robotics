package com.ignis.igrobotics.common.items;

import com.ignis.igrobotics.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class CommanderItem extends Item {

    public CommanderItem() {
        super(new Properties());
    }

    public static BlockPos getRememberedPos(ItemStack stack) {
        return BlockPos.ZERO;
    }

    public static UUID getRememberedEntity(ItemStack stack) {
        return Reference.DEFAULT_UUID;
    }
}
