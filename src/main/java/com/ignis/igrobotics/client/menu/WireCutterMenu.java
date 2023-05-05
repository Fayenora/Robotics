package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.blockentity.WireCutterBlockEntity;
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

public class WireCutterMenu extends BaseMenu {

    public static final Point slot_upper = new Point(33, 12);
    public static final Point slot_main = new Point(62, 36);
    public static final Point slot_out = new Point(118, 36);

    public final WireCutterBlockEntity blockEntity;
    private final Level level;
    public final ContainerData data;

    public WireCutterMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(5));
    }

    public WireCutterMenu(int id, Inventory playerInv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.WIRE_CUTTER.get(), id);
        this.blockEntity = (WireCutterBlockEntity) blockEntity;
        this.level = playerInv.player.level;
        this.data = data;

        addPlayerInv(playerInv, Reference.GUI_DEFAULT_DIMENSIONS);
        addDataSlots(data);

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            //Input-Slots
            this.addSlot(new SlotItemHandler(handler, 0, slot_upper.x, slot_upper.y));
            this.addSlot(new SlotItemHandler(handler, 1, slot_main.x, slot_main.y));

            //Output-Slot
            this.addSlot(new SlotOutput(handler, 2, slot_out.x, slot_out.y));
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.WIRE_CUTTER.get());
    }
}
