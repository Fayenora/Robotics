package com.ignis.igrobotics.common.blocks;

import com.ignis.igrobotics.ModMachines;
import com.ignis.igrobotics.common.blockentity.BlockEntityMachine;
import com.ignis.igrobotics.common.blockentity.BlockEntityWireCutter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class BlockWireCutter extends BlockMachine {
    public BlockWireCutter() {
        super(Properties.of(Material.HEAVY_METAL).strength(5f));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityWireCutter(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if(level.isClientSide()) return null;
        return createTickerHelper(type, ModMachines.WIRE_CUTTER.getBlockEntityType(), BlockEntityMachine::serverTick);
    }
}
