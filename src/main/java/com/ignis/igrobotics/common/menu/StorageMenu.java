package com.ignis.igrobotics.common.menu;

import com.ignis.igrobotics.common.blockentity.StorageBlockEntity;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class StorageMenu extends BaseMenu {

    public final StorageBlockEntity blockEntity;
    private final Level level;

    public StorageMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public StorageMenu(int id, Inventory playerInv, BlockEntity blockEntity) {
        super(ModMenuTypes.STORAGE.get(), playerInv, id);
        this.blockEntity = (StorageBlockEntity) blockEntity;
        this.blockEntity.addTrackingData(this);
        this.level = playerInv.player.level();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ROBOT_STORAGE.get());
    }
}
