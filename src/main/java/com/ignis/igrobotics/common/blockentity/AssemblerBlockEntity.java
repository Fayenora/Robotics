package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.client.menu.AssemblerMenu;
import com.ignis.igrobotics.client.menu.BaseMenu;
import com.ignis.igrobotics.definitions.ModMachines;
import com.ignis.igrobotics.definitions.ModSounds;
import com.ignis.igrobotics.network.container.SyncableByte;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AssemblerBlockEntity extends MachineBlockEntity {

    private byte activeArrows = 0;

    public AssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.ASSEMBLER, pos, state, 5, new int[]{0, 1, 2, 3}, new int[]{4});
        inventory.setOutputSlots(4);
        inventory.setDefaultAccessibleSlots(4);
        inventory.setValidSlotsForFace(Direction.UP, 0, 4);
        inventory.setValidSlotsForFace(Direction.WEST, 1, 4);
        inventory.setValidSlotsForFace(Direction.DOWN, 2, 4);
        inventory.setValidSlotsForFace(Direction.EAST, 3, 4);
    }

    @Override
    public void addTrackingContent(BaseMenu menu) {
        super.addTrackingContent(menu);
        menu.track(SyncableByte.create(() -> activeArrows, value -> activeArrows = value));
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

    @Override
    public @Nullable SoundEvent getRunningSound() {
        return ModSounds.ASSEMBLER.get();
    }

    @Override
    public float getVolume() {
        return 0.25f;
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

    public boolean isArrowActive(Direction dir) {
        if(!isRunning()) return false;
        return switch (dir) {
            case NORTH, UP -> activeArrows % 2 == 1;
            case EAST -> activeArrows % 4 >= 2;
            case SOUTH, DOWN -> activeArrows % 8 >= 4;
            case WEST -> activeArrows >= 8;
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.assembler");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new AssemblerMenu(id, inv, this);
    }

    @Override
    protected void onItemCrafted() {
        //NO-OP
    }
}
