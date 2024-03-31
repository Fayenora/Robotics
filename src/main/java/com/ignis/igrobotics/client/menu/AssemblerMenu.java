package com.ignis.igrobotics.client.menu;

import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.common.blockentity.AssemblerBlockEntity;
import com.ignis.igrobotics.definitions.ModBlocks;
import com.ignis.igrobotics.definitions.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class AssemblerMenu extends BaseMenu {
    public final AssemblerBlockEntity blockEntity;
    private final Level level;
    public final ContainerData data;

    public static final Point slot_top = new Point(80, 11);
    public static final Point slot_right = new Point(128, 59);
    public static final Point slot_bot = new Point(80, 107);
    public static final Point slot_left = new Point(32, 59);
    public static final Point slot_out = new Point(80, 59);

    public AssemblerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(6));
    }

    public AssemblerMenu(int id, Inventory playerInv, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.ASSEMBLER.get(), id);
        this.blockEntity = (AssemblerBlockEntity) blockEntity;
        this.level = playerInv.player.level;
        this.data = data;

        addPlayerInv(playerInv, Reference.GUI_ASSEMBLER_DIMENSIONS);
        addDataSlots(data);

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            //Input-Slots
            this.addSlot(new SlotItemHandler(handler, 0, slot_top.x, slot_top.y));
            this.addSlot(new SlotItemHandler(handler, 1, slot_right.x, slot_right.y));
            this.addSlot(new SlotItemHandler(handler, 2, slot_bot.x, slot_bot.y));
            this.addSlot(new SlotItemHandler(handler, 3, slot_left.x, slot_left.y));

            //Output-Slot
            this.addSlot(new SlotCustom(handler, 4, slot_out.x, slot_out.y).setPlaceable(false));
        });
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.ASSEMBLER.get());
    }
}
