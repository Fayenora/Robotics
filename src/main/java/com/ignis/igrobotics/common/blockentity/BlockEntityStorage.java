package com.ignis.igrobotics.common.blockentity;

import com.ignis.igrobotics.ModMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityStorage extends BlockEntity {
    public BlockEntityStorage(BlockPos pPos, BlockState pBlockState) {
        super(ModMachines.ROBOT_STORAGE.get(), pPos, pBlockState);
    }
}
