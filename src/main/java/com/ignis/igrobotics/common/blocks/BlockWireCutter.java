package com.ignis.igrobotics.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class BlockWireCutter extends BlockMachine{
    public BlockWireCutter() {
        super(Properties.of(Material.HEAVY_METAL).strength(5f));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return null;
    }
}
