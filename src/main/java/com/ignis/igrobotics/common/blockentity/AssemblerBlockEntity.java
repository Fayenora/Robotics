package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.ModMachines;
import com.ignis.igrobotics.client.menu.AssemblerMenu;
import com.ignis.igrobotics.core.util.ContainerDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;

public class AssemblerBlockEntity extends MachineBlockEntity {

    private int activeArrows = 0;

    public AssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.ASSEMBLER, pos, state, 5, new int[]{0, 1, 2, 3}, new int[]{4});
        inventory.setOutputSlots(4);
        inventory.setDefaultAccessibleSlots(4);
        inventory.setValidSlotsForFace(Direction.UP, 0, 4);
        inventory.setValidSlotsForFace(Direction.WEST, 1, 4);
        inventory.setValidSlotsForFace(Direction.DOWN, 2, 4);
        inventory.setValidSlotsForFace(Direction.EAST, 3, 4);

        dataAccess = ContainerDataUtil.merge(dataAccess, new ContainerData() {
            @Override
            public int get(int key) {
                if(key == 0) return activeArrows;
                return 0;
            }

            @Override
            public void set(int key, int value) {
                if(key == 0) activeArrows = value;
            }

            @Override
            public int getCount() {
                return 1;
            }
        });
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
    protected Component getDefaultName() {
        return Component.translatable("container.assembler");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new AssemblerMenu(id, inv, this, this.dataAccess);
    }

    @Override
    protected void onItemCrafted() {
        //NO-OP
    }
}
