package com.ignis.igrobotics.common.content.blockentity;

import com.ignis.igrobotics.common.content.menu.WireCutterMenu;
import com.ignis.igrobotics.definitions.ModMachines;
import com.ignis.igrobotics.definitions.ModSounds;
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
public class WireCutterBlockEntity extends MachineBlockEntity {

    public WireCutterBlockEntity(BlockPos pos, BlockState state) {
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
    public @Nullable SoundEvent getRunningSound() {
        return ModSounds.WIRE_CUTTER.get();
    }

    @Override
    public float getVolume() {
        return 0.5f;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.wire_cutter");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new WireCutterMenu(id, inv, this);
    }
}
