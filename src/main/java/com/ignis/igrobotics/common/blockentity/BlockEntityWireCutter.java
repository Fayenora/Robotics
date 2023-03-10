package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.ModMachines;
import com.ignis.igrobotics.client.menu.WireCutterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityWireCutter extends BlockEntityMachine {

    public BlockEntityWireCutter(BlockPos pos, BlockState state) {
        super(ModMachines.WIRE_CUTTER, pos, state, 3, new int[] {0, 1}, new int[] {2});
        inventory.setOutputSlots(2);
        inventory.setDefaultAccessibleSlots(0, 2);
        inventory.setValidSlotsForFace(Direction.UP, 1, 2);
    }

    @Override
    protected void onItemCrafted() {
        //NO-OP
    }

    @Override
    protected void onMachineStart() {
        //NO-OP
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.wire_cutter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new WireCutterMenu(id, inv, this, this.dataAccess);
    }
}
