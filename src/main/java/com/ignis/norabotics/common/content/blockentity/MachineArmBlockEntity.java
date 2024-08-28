package com.ignis.norabotics.common.content.blockentity;

import com.ignis.norabotics.definitions.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MachineArmBlockEntity extends BlockEntity {
    public MachineArmBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModMachines.MACHINE_ARM.get(), pPos, pBlockState);
    }
}
