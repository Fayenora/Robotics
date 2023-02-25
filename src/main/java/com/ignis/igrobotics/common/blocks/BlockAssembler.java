package com.ignis.igrobotics.common.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class BlockAssembler extends BlockMachine {
    public BlockAssembler() {
        super(BlockBehaviour.Properties.of(Material.HEAVY_METAL).strength(5f));

    }
}
