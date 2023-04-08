package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.common.blockentity.WireCutterBlockEntity;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class StorageMenu extends AbstractContainerMenu {

    public final StorageBlockEntity blockEntity;
    private final Level level;
    public final ContainerData data;

    public StorageMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public StorageMenu(int id, Inventory playerInv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.STORAGE.get(), id);
        this.blockEntity = (StorageBlockEntity) blockEntity;
        this.level = playerInv.player.level;
        this.data = data;
        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ROBOT_STORAGE.get());
    }
}
