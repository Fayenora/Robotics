package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.blockentity.FactoryBlockEntity;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import java.awt.*;

public class FactoryMenu extends BaseMenu {
    public final FactoryBlockEntity blockEntity;
    private final Level level;
    public final ContainerData data;

    public FactoryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public FactoryMenu(int id, Inventory playerInv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.FACTORY.get(), id);
        this.blockEntity = (FactoryBlockEntity) blockEntity;
        this.level = playerInv.player.level;
        this.data = data;

        addDataSlots(data);
        addPlayerInv(playerInv, 35, 136);
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            addSlot(new SlotItemHandler(handler, 0, 27, 17)); //Head
            addSlot(new SlotItemHandler(handler, 1, 186, 17)); //Body
            addSlot(new SlotItemHandler(handler, 2, 186, 63)); //Left arm
            addSlot(new SlotItemHandler(handler, 3, 27, 63)); //Right arm
            addSlot(new SlotItemHandler(handler, 4, 186, 109)); //Left leg
            addSlot(new SlotItemHandler(handler, 5, 27, 109)); //Right leg
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ROBOT_FACTORY.get());
    }
}
