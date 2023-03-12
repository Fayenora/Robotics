package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.ModMachines;
import com.ignis.igrobotics.Reference;
import com.ignis.igrobotics.client.menu.FactoryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityFactory extends BlockEntityMachine {

    public BlockEntityFactory(BlockPos pos, BlockState state) {
        super(ModMachines.ROBOT_FACTORY, pos, state, 6 + Reference.MAX_MODULES, new int[] {}, new int[] {});
    }

    @Override
    protected void onItemCrafted() {

    }

    @Override
    protected void onMachineStart() {

    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.robot_factory");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new FactoryMenu(id, inv, this, this.dataAccess);
    }
}
