package com.ignis.igrobotics.common.blocks;

import com.ignis.igrobotics.common.blockentity.BlockEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class BlockFactory extends BlockStorage {

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityFactory(pos, state);
    }
}
