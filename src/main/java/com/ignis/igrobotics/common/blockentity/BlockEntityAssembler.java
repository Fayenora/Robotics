package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityAssembler extends BlockEntityMachine {

    private int activeArrows = 0;

    public BlockEntityAssembler(BlockPos pos, BlockState state) {
        super(ModMachines.ASSEMBLER, pos, state, 5, new int[]{0, 1, 2, 3}, new int[]{4});
        inventory.setOutputSlots(4);
        inventory.setDefaultAccessibleSlots(4);
        inventory.setValidSlotsForFace(Direction.UP, 0, 4);
        inventory.setValidSlotsForFace(Direction.WEST, 1, 4);
        inventory.setValidSlotsForFace(Direction.DOWN, 2, 4);
        inventory.setValidSlotsForFace(Direction.EAST, 3, 4);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        setActiveArrows();
    }

    @Override
    protected void onMachineStart() {
        setActiveArrows();
    }

    private void setActiveArrows() {
        if(hasRecipe()) {
            activeArrows = 0;
            for(int i = 0; i < 4; i++) {
                if(!getRecipeUsed().getIngredients().get(i).isEmpty()) {
                    activeArrows += Math.pow(2, i);
                }
            }
        }
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return inventory.getSlotsForFace(direction);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.assembler");
    }

    @Override
    protected AbstractContainerMenu createMenu(int p_58627_, Inventory p_58628_) {
        return null;
    }

    @Override
    protected void onItemCrafted() {
        //NO-OP
    }
}
