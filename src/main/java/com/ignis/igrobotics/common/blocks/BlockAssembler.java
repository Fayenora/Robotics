package com.ignis.igrobotics.common.blocks;

import com.ignis.igrobotics.common.blockentity.BlockEntityAssembler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class BlockAssembler extends BlockMachine {
    public BlockAssembler() {
        super(BlockBehaviour.Properties.of(Material.HEAVY_METAL).strength(5f));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAssembler(pos, state);
    }
}
