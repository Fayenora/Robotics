package com.ignis.norabotics.common.content.menu;

import com.ignis.norabotics.Reference;
import com.ignis.norabotics.common.content.blockentity.WireCutterBlockEntity;
import com.ignis.norabotics.common.content.menu.slots.CustomSlot;
import com.ignis.norabotics.definitions.ModBlocks;
import com.ignis.norabotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class WireCutterMenu extends BaseMenu {

    public static final Point slot_upper = new Point(33, 12);
    public static final Point slot_main = new Point(62, 36);
    public static final Point slot_out = new Point(118, 36);

    public final WireCutterBlockEntity blockEntity;
    private final Level level;

    public WireCutterMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public WireCutterMenu(int id, Inventory playerInv, BlockEntity blockEntity) {
        super(ModMenuTypes.WIRE_CUTTER.get(), playerInv, id);
        this.blockEntity = (WireCutterBlockEntity) blockEntity;
        this.blockEntity.addTrackingContent(this);
        this.level = playerInv.player.level();

        addPlayerInv(Reference.GUI_DEFAULT_DIMENSIONS);

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            //Input-Slots
            this.addSlot(new SlotItemHandler(handler, 0, slot_upper.x, slot_upper.y));
            this.addSlot(new SlotItemHandler(handler, 1, slot_main.x, slot_main.y));

            //Output-Slot
            this.addSlot(new CustomSlot(handler, 2, slot_out.x, slot_out.y).setPlaceable(false));
        });
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.WIRE_CUTTER.get());
    }
}
